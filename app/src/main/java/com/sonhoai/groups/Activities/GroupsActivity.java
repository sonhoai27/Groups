package com.sonhoai.groups.Activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sonhoai.groups.Adapter.GroupAdapter;
import com.sonhoai.groups.Adapter.UserAdapter;
import com.sonhoai.groups.DataModels.Class;
import com.sonhoai.groups.DataModels.Group;
import com.sonhoai.groups.DataModels.GroupUser;
import com.sonhoai.groups.DataModels.User;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.CallBack;
import com.sonhoai.groups.Uti.HandleFBAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupsActivity extends AppCompatActivity {
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;
    private static String classKey;
    private ListView lvGroups;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private BottomSheetBehavior sheetBehavior;
    private LinearLayout bottomSheet;
    private UserAdapter userAdapter;
    private ListView lvUsers;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        Bundle bundle = getIntent().getExtras();
        classKey = bundle.getString("idClass");
        init();
        getClassLists();
    }

    //classes grroup này là nơi chứa tất cả các group thuộc classes nào đó
    private void init() {
        lvGroups = findViewById(R.id.lvGroups);
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(groupList, getApplicationContext());
        lvGroups.setAdapter(groupAdapter);
        bottomSheet = findViewById(R.id.layoutBottomSheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        users = new ArrayList<>();
        lvUsers = findViewById(R.id.lvUsers);
        userAdapter = new UserAdapter(users, getApplicationContext());
        lvUsers.setAdapter(userAdapter);

        //nhấn vào thì ra class chat của group vừa nhấn
        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(GroupsActivity.this, ChatActivity.class);
                intent.putExtra("keyGroup", groupList.get(i).getId());
                intent.putExtra("keyName", groupList.get(i).getName());
                startActivity(intent);
            }
        });

        //nhấn lâu thì ds user của class này, chọn user để thêm vào lớp
        lvGroups.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));

                LayoutInflater layoutInflater = getLayoutInflater();
                View v = layoutInflater.inflate(R.layout.dialog_add_user_to_group, null);

                builder.setView(v);
                builder.setCancelable(true);

                final ListView lv = v.findViewById(R.id.lvListMemberOfClass);
                final TextView leader = v.findViewById(R.id.txtLeaderGroup);

                final List<User> users = new ArrayList<>();
                UserAdapter userAdapter = new UserAdapter(users, GroupsActivity.this);
                lv.setAdapter(userAdapter);

                getNameUser(groupList.get(po).getIdUser(), new CallBack<String>() {
                    @Override
                    public void isCompleted(String obj) {
                        leader.setText("Leader: " + obj);
                    }

                    @Override
                    public void isFail(String obj) {

                    }
                });
                //hiển thị ra ds các thành viên trong lớp có thể thêm vào nhóm này
                showMemberOfClassToAddGroup(userAdapter, users, groupList.get(po));
                //khi click vào 1 user thì thêm vào nhóm
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int p, long l) {
                        GroupUser groupUser = new GroupUser(
                                users.get(p).getId(),
                                users.get(p).getName(),
                                "0",
                                "0",
                                groupList.get(po).getId()
                        );
                        addUserToGroup(groupUser, groupList.get(po));
                    }
                });

                builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setNegativeButton("Xóa nhóm này", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMemberGroup(groupList.get(po).getId(), new CallBack<String>() {
                            @Override
                            public void isCompleted(String obj) {
                                Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void isFail(String obj) {

                            }
                        });
                    }
                });

                builder.show();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_class: {
                addGroup();
                return true;
            }
            case R.id.action_more: {
                toggleBottomSheet();
                return true;
            }
            case R.id.action_show_user: {
                listUser();
                return true;
            }
        }
        return false;
    }

    private void chooseDate(final EditText picker) {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String calenderFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(calenderFormat, Locale.ENGLISH);
                picker.setText(sdf.format(calendar.getTime()));
            }
        };
        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(
                        view.getContext(),
                        date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }


    //classes grroup này là nơi chứa tất cả các group thuộc classes nào đó
    //lấy ra ds user toàn bộ hệ thống có và user của lớp, so trùng, nếu trùng thì ko hiện ra tránh thêm vào trùng
    //trả về callback để kiểm tra dữ liệu đã sẵn ràng hay chưa
    private void getUserLists(final CallBack<String> userCallBack) {
        final List<User> temUsers = new ArrayList<>();

        //ds user của lớp
        mReference = mDatabase.getReference("memberClass/" + classKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                temUsers.clear();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user1 = item.getValue(User.class);
                        temUsers.add(user1);
                    }
                    if (temUsers.size() > 0) {
                        //lấy ra ds user lớn, bắt đầu so trùng
                        mReference = mDatabase.getReference("users");
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    users.clear();
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        Boolean flag = false;
                                        User user = item.getValue(User.class);
                                        user.setId(item.getKey());
                                        for (int i = 0; i < temUsers.size(); i++) {
                                            if (temUsers.get(i).getId().equals(user.getId())) {
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            users.add(user);
                                        }
                                    }
                                    userCallBack.isCompleted("OK");
                                    userAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    mReference = mDatabase.getReference("users");
                    mReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                users.clear();
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    User user = item.getValue(User.class);
                                    user.setId(item.getKey());
                                    users.add(user);
                                }
                                userCallBack.isCompleted("OK");
                                userAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //lấy ds class
    private void getClassLists() {
        Query recentPostsQuery = mDatabase.getReference("groups").orderByChild("idClass").startAt(classKey).endAt(classKey);
        recentPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("AAAA", String.valueOf(dataSnapshot.getValue()));
                groupList.clear();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        Group group = item.getValue(Group.class);
                        group.setId(item.getKey());
                        groupList.add(group);
                    }
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //màn hình ;ấy ra ds user lớn, chọn user thêm vào lớp học này bằng class addUser
    public void toggleBottomSheet() {
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        getUserLists(new CallBack<String>() {
                            @Override
                            public void isCompleted(String obj) {
                                if (obj.equals("OK")) {
                                    addUser();
                                }
                            }

                            @Override
                            public void isFail(String obj) {

                            }
                        });

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        users.clear();
                        userAdapter.notifyDataSetChanged();
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                Rect outRect = new Rect();
                bottomSheet.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY()))
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //hàm thêm user vào lớp học này
    private void addUser() {
        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                mReference = mDatabase.getReference("classes/" + classKey);
                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Class aClass = dataSnapshot.getValue(Class.class);
                        if (aClass.getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())) {
                            mReference = mDatabase.getReference("memberClass");
                            User user = new User(users.get(i).getId(), users.get(i).getName());
                            mReference.child(classKey).child(users.get(i).getId()).setValue(user);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));
                            builder.setTitle("Cảnh báo!");
                            builder.setMessage("Bạn không phải Leader lớp này nên không thể thêm thành viên vào lớp.");
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

    //thêm group mới vào class
    private void addGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_add_group, null);

        builder.setView(view);
        builder.setCancelable(true);

        final EditText edtName = view.findViewById(R.id.edtGroupName);
        final EditText edtContent = view.findViewById(R.id.edtGroupContent);
        final EditText edtDate = view.findViewById(R.id.edtGroupDate);
        chooseDate(edtDate);
        builder.setNegativeButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mReference = mDatabase.getReference("groups");
                Group group = new Group(
                        null,
                        classKey,
                        edtName.getText().toString(),
                        edtContent.getText().toString(),
                        edtDate.getText().toString(),
                        HandleFBAuth.firebaseAuth.getUid()
                );
                mReference.push().setValue(group, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Log.i("AAAA", "Thanh cong");
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

    //dialog hiển thĩ ra ds user
    private void listUser() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_show_user, null);

        builder.setView(view);
        builder.setCancelable(false);

        final ListView lvListUserClass = view.findViewById(R.id.lvListUserClass);
        loadUserClass(new CallBack<List<User>>() {
            @Override
            public void isCompleted(List<User> obj) {
                UserAdapter userAdapter = new UserAdapter(obj, GroupsActivity.this);
                lvListUserClass.setAdapter(userAdapter);
                deleteUserClass(lvListUserClass, obj);
            }

            @Override
            public void isFail(List<User> obj) {

            }
        });
        builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    //hàm có tác dụng giúp hàm listUser lấy ra ds user, kiểm tra chắc chắn đã lấy ra xong chưa
    private void loadUserClass(final CallBack<List<User>> callBack) {

        mReference = mDatabase.getReference("memberClass/" + classKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    List<User> users = new ArrayList<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user1 = item.getValue(User.class);
                        users.add(user1);
                    }
                    callBack.isCompleted(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //xóa user khỏi class
    private void deleteUserClass(ListView lv, final List<User> users) {
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int p, long l) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(GroupsActivity.this);
                dialog.setTitle("Xác nhận xóa!");
                dialog.setMessage("Vui lòng chọn xóa để xóa thành viên " + users.get(p).getName());
                dialog.setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        final Query recentPostsQuery = mDatabase.getReference("memberClass/" + classKey).child(users.get(p).getNodeKey());
//                        recentPostsQuery.getRef().removeValue();
                        mReference = mDatabase.getReference("memberClass/" + classKey+"/"+users.get(p).getId());
                        mReference.removeValue();
                    }
                });

                dialog.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dialog.show();
                return false;
            }
        });
    }

    //user ở đây thêm vào là các user thuyết trình
    //lấy ra ds user để add vào 1 group, so trug vs ds user trong lop chua group, nếu user đã thêm vào group thì ko thêm nữa
    private void showMemberOfClassToAddGroup(final UserAdapter userAdapter, final List<User> users, Group group) {
        final List<User> temUsers = new ArrayList<>();
        mReference = mDatabase.getReference("memberGroup/" + group.getId());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    temUsers.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user1 = item.getValue(User.class);
                        temUsers.add(user1);
                    }
                    Log.i("USERGROUP", temUsers.toString());
                    if (temUsers.size() > 0) {
                        mReference = mDatabase.getReference("memberClass/" + classKey);
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    users.clear();
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        Boolean flag = false;
                                        User user = item.getValue(User.class);
                                        Log.i("USER", user.toString());
                                        for (int i = 0; i < temUsers.size(); i++) {
                                            if (temUsers.get(i).getId().equals(user.getId())) {
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            users.add(user);
                                        }
                                    }
                                    userAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    mReference = mDatabase.getReference("memberClass/" + classKey);
                    mReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                users.clear();
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    User user1 = item.getValue(User.class);
                                    users.add(user1);
                                }
                                userAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getNameUser(String key, final CallBack<String> userName) {
        mReference = mDatabase.getReference("users/" + key + "/name");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.isCompleted(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //hàm thêm user vào 1 group nhất định
    private void addUserToGroup(final GroupUser user, final Group group) {

        mReference = mDatabase.getReference("groups/" + group.getId());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //có nghĩa là user hiện tại là leader
                if (dataSnapshot.getValue() != null) {
                    Group group1 = dataSnapshot.getValue(Group.class);
                    if (group1.getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())) {
                        mReference = mDatabase.getReference("memberGroup/");
                        mReference.child(group.getId()).child(user.getId()).setValue(user, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));
                        builder.setTitle("Cảnh báo!");
                        builder.setMessage("Bạn không phải Leader nhóm này, nên không có quyền thêm thành viên thuyết trình vào nhóm. Nhưng bạ được tham gia thảo luận.");
                        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        builder.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteMemberGroup(final String idGroup, final CallBack<String> callBack){
        AlertDialog.Builder  builder = new AlertDialog.Builder(new ContextThemeWrapper(GroupsActivity.this, R.style.myDialog));
        builder.setTitle("Bạn muốn xóa group này?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mReference = mDatabase.getReference("groups/"+idGroup);
                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if(dataSnapshot!=null){
                                Group group = dataSnapshot.getValue(Group.class);
                                if(group.getIdUser().equals(HandleFBAuth.firebaseAuth.getUid()) ){
                                    Log.i("AASDDD", String.valueOf(group.getIdUser()));
                                    mReference = mDatabase.getReference("memberGroup/"+idGroup);
                                    mReference.removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            deleteMessageGroup(idGroup);
                                        }
                                    });
                                }else {
                                    Toast.makeText(getApplicationContext(), "Bạn phải là leader nhóm này.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void deleteMessageGroup(final String idGroup){
        mReference = mDatabase.getReference("messages/"+idGroup);
        mReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
               deleteFileGroup(idGroup);
            }
        });
    }
    private void deleteFileGroup(final String idGroup){
        mReference = mDatabase.getReference("fileGroup/"+idGroup);
        mReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
              deleteGroup(idGroup);
            }
        });
    }
    private void deleteGroup(final String idGroup){
        mReference = mDatabase.getReference("groups/"+idGroup);
        mReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(getApplicationContext(), "Thanh cong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
