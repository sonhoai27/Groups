package com.sonhoai.groups.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sonhoai.groups.Adapter.ClassesAdapter;
import com.sonhoai.groups.DataModels.Class;
import com.sonhoai.groups.DataModels.User;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.CallBack;
import com.sonhoai.groups.Uti.HandleFBAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mClasses;
    private ListView lvClasses;
    private List<Class> classes;
    private ClassesAdapter classesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getClassLists();
        detailClass(lvClasses);
    }

    private void init(){
        setTitle("Danh sách lớp");
        classes = new ArrayList<>();
        lvClasses = findViewById(R.id.lvClasses);
        mClasses = mDatabase.getReference("classes");
        classesAdapter = new ClassesAdapter(getApplicationContext(), classes);
        lvClasses.setAdapter(classesAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main,menu);
        return true;
    }

    //dialog thêm một lốp mới vào db
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add_class){
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog));

            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_add_class, null);

            builder.setView(view);
            builder.setCancelable(true);

            final EditText edtName = view.findViewById(R.id.edtClassName);
            final EditText edtInfo = view.findViewById(R.id.edtInfoClass);

            builder.setNegativeButton("Thêm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getNameUser(new CallBack<String>() {
                        @Override
                        public void isCompleted(final String obj) {
                            if(obj!= null){
                                mClasses = mDatabase.getReference("classes");
                                Class aClass = new Class(null,edtName.getText().toString(), edtInfo.getText().toString(), HandleFBAuth.firebaseAuth.getUid(), obj);
                                mClasses.push().setValue(aClass, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        mClasses = mDatabase.getReference("memberClass/"+databaseReference.getKey());
                                        User user = new User(HandleFBAuth.firebaseAuth.getUid(), obj);
                                        mClasses.child(HandleFBAuth.firebaseAuth.getUid()).setValue(user);
                                    }
                                });
                            }
                        }

                        @Override
                        public void isFail(String obj) {

                        }
                    });
                }
            });

            builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.show();
        }else if(item.getItemId() == R.id.action_logout){
            HandleFBAuth.firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }, 1000);
        }
        return true;
    }

    //lấy ra danh sách lớp đã tạo
    private void getClassLists(){
        mClasses = mDatabase.getReference("classes");
        mClasses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("AAAA", String.valueOf(dataSnapshot.getValue()));
                classes.clear();
                //nếu khác null, là có lớp thì thêm lớp vào ds, sau đó notify lại adapter
                if(dataSnapshot.getValue() != null){
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        Class aClass = item.getValue(Class.class);
                        aClass.setId(item.getKey());
                        classes.add(aClass);
                    }
                    classesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //lấy ra tên của account đã đăng nhập vào ứng dụng
    private void getNameUser(final CallBack<String> userName){
        mClasses = mDatabase.getReference("users/"+HandleFBAuth.firebaseAuth.getUid()+"/name");
        mClasses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.isCompleted(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //iten, để qua màn hình group, nơi chứa các nhóm thuyết trình của lớp này
    private void detailClass(ListView lv){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int po, long l) {
                mClasses = mDatabase.getReference("memberClass/"+classes.get(po).getId()+"/"+HandleFBAuth.firebaseAuth.getUid());
                mClasses.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null){
                            if(dataSnapshot.getValue(User.class).getId().equals(HandleFBAuth.firebaseAuth.getUid())){
                                Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
                                intent.putExtra("idClass", classes.get(po).getId());
                                startActivity(intent);
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog));
                                builder.setTitle("Cảnh báo!");
                                builder.setMessage("Bạn không phải là thành viên lớp này, vui lòng liên hệ leader.");
                                builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });

                                builder.show();
                            }
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog));
                            builder.setTitle("Cảnh báo!");
                            builder.setMessage("Bạn không phải là thành viên lớp này, vui lòng liên hệ leader.");
                            builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                            builder.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }
    private void deleteClass(ListView lv){
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                mClasses = mDatabase.getReference("memberClass/"+classes.get(po).getId()+"/"+HandleFBAuth.firebaseAuth.getUid());
                mClasses.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            if(dataSnapshot.getValue(User.class).getId().equals(HandleFBAuth.firebaseAuth.getUid())){

                            }else {
                                Toast.makeText(getApplicationContext(), "Bạn không phải là thành viên lớp này, vui lòng liên hệ leader.", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return false;
            }
        });
    }
    //xử lý nhấn nút back, nếu là nút back, thì thoát khỏi app và trở về màn hình home
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_BACK){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
