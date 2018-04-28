package com.sonhoai.groups.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sonhoai.groups.Adapter.ClassesAdapter;
import com.sonhoai.groups.DataModels.Class;
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
                        public void isCompleted(String obj) {
                            if(obj!= null){
                                mClasses = mDatabase.getReference("classes");
                                Class aClass = new Class(null,edtName.getText().toString(), edtInfo.getText().toString(), HandleFBAuth.firebaseAuth.getUid(), obj);
                                mClasses.push().setValue(aClass, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Log.i("AAAA", "Thanh cong");
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
        }
        return true;
    }

    private void getClassLists(){
        mClasses = mDatabase.getReference("classes");
        mClasses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("AAAA", String.valueOf(dataSnapshot.getValue()));
                classes.clear();
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
    private void detailClass(ListView lv){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
                intent.putExtra("idClass", classes.get(i).getId());
                startActivity(intent);
            }
        });
    }
}
