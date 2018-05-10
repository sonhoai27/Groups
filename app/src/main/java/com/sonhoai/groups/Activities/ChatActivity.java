package com.sonhoai.groups.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sonhoai.groups.Adapter.FileAdapter;
import com.sonhoai.groups.Adapter.MessageAdapter;
import com.sonhoai.groups.Adapter.UserChatAdapter;
import com.sonhoai.groups.DataModels.FileShare;
import com.sonhoai.groups.DataModels.Group;
import com.sonhoai.groups.DataModels.GroupUser;
import com.sonhoai.groups.DataModels.Message;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.CallBack;
import com.sonhoai.groups.Uti.HandleFBAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity{
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private static String groupKey;
    private String groupName;
    private ListView lvMessages;
    private EditText edtInput;
    private ImageButton addFile;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private Button btnSend;
    private ImageButton imgSendFile;
    private static int PICK_FILE = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //nhận dữ liệu intent từ class group
        Bundle bundle = getIntent().getExtras();
        groupKey = bundle.getString("keyGroup");
        groupName = bundle.getString("keyName");
        init();
        //lấy ra ds tin nhấn và thêm tin nhắn qua 2 hàm bên dưới
        addMessage();
        getMessages();
        pickFile();
    }

    private void init(){
        setTitle("Nhóm: "+groupName);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.bg_toolbar);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
        btnSend = findViewById(R.id.btnSend);
        lvMessages = findViewById(R.id.lvMessages);
        edtInput = findViewById(R.id.edtInputMessage);
        addFile = findViewById(R.id.imgSendFile);
        imgSendFile = findViewById(R.id.imgSendFile);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages,ChatActivity.this);
        lvMessages.setAdapter(messageAdapter);
        deleteMessage(lvMessages);

    }

    private void deleteMessage(ListView lv){
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
                builder.setTitle("Bạn muốn xóa tin nhắn này?");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mReference = mDatabase.getReference("groups/"+groupKey+"/");
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("DATATAT", String.valueOf(dataSnapshot));
                                if(dataSnapshot !=null){
                                    Group group = dataSnapshot.getValue(Group.class);
                                    if(group.getIdUser().equals(HandleFBAuth.firebaseAuth.getUid()) ){
                                        mReference = mDatabase.getReference("messages/" + groupKey + "/" + messages.get(po).getId());
                                        mReference.removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else if(messages.get(po).getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())) {
                                        mReference = mDatabase.getReference("messages/" + groupKey + "/" + messages.get(po).getId());
                                        mReference.removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Bạn không có quyền xóa, chỉ có Leader của nhóm thuyết trình mới được xóa!.", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(getApplicationContext(), "Bạn không có quyền xóa, chỉ có Leader của nhóm thuyết trình mới được xóa!.", Toast.LENGTH_SHORT).show();
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
                return false;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_show_user: {
                showUser();
                return true;
            }
            case R.id.action_file: {
                showFile();
                return true;
            }
            case R.id.action_edit_group: {
                editGroup();
                return true;
            }
        }
        return false;
    }

    private void editGroup() {
        //tạo 1 dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_add_group, null);
        builder.setTitle("Sửa nhóm");
        builder.setView(view);
        Dialog dialog = builder.create();
        final EditText edtName = view.findViewById(R.id.edtGroupName);
        final EditText edtContent = view.findViewById(R.id.edtGroupContent);
        final EditText edtDate = view.findViewById(R.id.edtGroupDate);
        chooseDate(edtDate);
        getInfoGroupToEdit(edtName, edtContent, edtDate, dialog);
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Sửa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mReference = mDatabase.getReference("groups/"+groupKey);
                Group group = new Group(
                        null,
                        edtName.getTag().toString(),//idclass
                        edtName.getText().toString(),
                        edtContent.getText().toString(),
                        edtDate.getText().toString(),
                        edtContent.getTag().toString()//iduser
                );
                mReference.setValue(group, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.show();
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

    private void getInfoGroupToEdit(final EditText edtName, final EditText edtContent, final EditText edtDate, final Dialog  dialog){
        mReference = mDatabase.getReference("groups/"+groupKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    Group group = dataSnapshot.getValue(Group.class);
                    if(group.getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())){
                        edtContent.setText(group.getContent());
                        edtName.setText(group.getName());
                        edtDate.setText(group.getDate());
                        edtName.setTag(group.getIdClass());
                        edtContent.setTag(group.getIdUser());
                    }else {
                        Toast.makeText(getApplicationContext(), "Bạn không có quyền sửa", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //lấy ra ds các file mà thành viên đã chia sẽ
    private void showFile() {

        //tạo 1 dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_manage_files, null);

        builder.setView(view);
        final Dialog dialog = builder.show();


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window dialogWindow = dialog.getWindow();
        dialog.getWindow().getAttributes().windowAnimations = R.style.Slide_Up_Down;
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.x = 0;
        lp.y = 16; // The new position of the Y coordinates
        dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        dialogWindow.setAttributes(lp);
        final ListView lvListFile = view.findViewById(R.id.lvListFile);

        //hàm load file, sử dụng 1 callback, nguyên nhân do có thể chạy bất đồng bộ, bất đồng bộm là chương trình sẽ không thực thi từ trên xuống dưới
        //ví dụ mạng chậm thì sẽ load file chậm....
        //callback này nếu mà load ra ds file thành công thì nó sẽ trả về 1 ds các file, từ ds này sẽ hiện thị ra listview
        loadListFiles(new CallBack<List<FileShare>>() {
            @Override
            public void isCompleted(List<FileShare> obj) {
                FileAdapter fileAdapter = new FileAdapter(ChatActivity.this, obj);
                lvListFile.setAdapter(fileAdapter);
                //hàm xử lý click vào 1 file nhất định, khi click vào thì sẽ tải file về máy
                onClickItemFile(lvListFile, obj);
                deleteFile(lvListFile, obj);
            }

            @Override
            public void isFail(List<FileShare> obj) {

            }
        });
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
    }

    //lấy ra ds ti nhắn
    private void getMessages(){
        //lấy ra tin nhắn của  group, mỗi group có 1 key riêng
        mReference = mDatabase.getReference("messages/"+groupKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //reset lại mảng tin nhắn
                messages.clear();
                //nếu có tin nhắn thì duyệt vòng for và thêm tin nhắn vào list, sau đó update lại adapter
                if(dataSnapshot.getValue() != null){
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        Message message = item.getValue(Message.class);
                        message.setId(item.getKey());
                        messages.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void addMessage(){
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNameUser(new CallBack<String>() {
                    @Override
                    public void isCompleted(String obj) {
                        if(obj!= null){

                            //định dạng giờ
                            Date currentTime = Calendar.getInstance().getTime();
                            SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a, dd-MM-yyyy");
                            String currentDateTimeString = sdf.format(currentTime);
                            mReference = mDatabase.getReference("messages");

                            //nếu có nhập thì mới thêm
                            if(edtInput.getText().toString().length() > 0){
                                Message message = new Message(
                                        null,
                                        HandleFBAuth.firebaseAuth.getUid(),
                                        obj,
                                        edtInput.getText().toString(),
                                        currentDateTimeString
                                );
                                edtInput.setText("");
                                mReference.child(groupKey).push().setValue(message, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        edtInput.setText("");
                                    }
                                });
                            }else {
                                Toast.makeText(getApplicationContext(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void isFail(String obj) {

                    }
                });
            }
        });
    }
    private void getNameUser(final CallBack<String> userName){
        mReference = mDatabase.getReference("users/"+ HandleFBAuth.firebaseAuth.getUid()+"/name");
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

    //hiển thị ds user có trong group thuyết trình này
    private void showUser(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_show_user, null);

        builder.setView(view);
        final Dialog dialog = builder.show();


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window dialogWindow = dialog.getWindow();
        dialog.getWindow().getAttributes().windowAnimations = R.style.Slide_Up_Down;
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.x = 0;
        lp.y = 16; // The new position of the Y coordinates
        dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        dialogWindow.setAttributes(lp);
        final ListView lvListUserClass = view.findViewById(R.id.lvListUserClass);
        //hàm load ds user cho dialog này
        loadUserClass(new CallBack<List<GroupUser>>() {
            @Override
            public void isCompleted(final List<GroupUser> obj) {
                UserChatAdapter userAdapter = new UserChatAdapter(ChatActivity.this, obj);
                lvListUserClass.setAdapter(userAdapter);

                lvListUserClass.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                        final AlertDialog.Builder xoaBuilder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
                        xoaBuilder.setTitle("Bạn có muốn xóa thành viên này");
                        xoaBuilder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mReference = mDatabase.getReference("memberGroup/"+groupKey+"/"+obj.get(po).getId());
                                mReference.removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        xoaBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        xoaBuilder.show();
                        return false;
                    }
                });
            }

            @Override
            public void isFail(List<GroupUser> obj) {

            }
        });
        builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
    }

    //hàm load ra ds user
    private void loadUserClass(final CallBack<List<GroupUser>> listCallBack) {
        mReference = mDatabase.getReference("memberGroup/"+groupKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    List<GroupUser> users = new ArrayList<>();
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        GroupUser user1 = item.getValue(GroupUser.class);
                        users.add(user1);
                    }
                    listCallBack.isCompleted(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //lấy ra ds file, sử dụng callback cho dialog hiển thị ds file
    private void loadListFiles(final CallBack<List<FileShare>> listCallBack) {
        mReference = mDatabase.getReference("fileGroup/"+groupKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    List<FileShare> fileShares = new ArrayList<>();
                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        FileShare fileShare = item.getValue(FileShare.class);
                        fileShare.setId(item.getKey());
                        fileShares.add(fileShare);
                    }
                    listCallBack.isCompleted(fileShares);
                    Log.i("DATATAT", String.valueOf(fileShares));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //upload file
    private void pickFile(){
        imgSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));

                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dialog_upload_file, null);

                builder.setView(view);

                TextView txtPickFile = view.findViewById(R.id.txtPickFile);

                final Dialog dialog = builder.show();


                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Window dialogWindow = dialog.getWindow();
                dialog.getWindow().getAttributes().windowAnimations = R.style.Slide_Up_Down;
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();

                lp.x = 0;
                lp.y = 16; // The new position of the Y coordinates
                dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                dialogWindow.setAttributes(lp);

                txtPickFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //nhảy qua màn hình chọn file, sau đó trả về, file vừa lấy qua hàm onActivityResult và up lên firebase
                        Intent intent = new Intent();
                        intent.setType("application/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select FileShare"), PICK_FILE);
                        dialog.cancel();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FILE && data != null){
            final StorageReference storageReference = storageRef.child(data.getData().getLastPathSegment());
            storageReference.putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    getNameUser(new CallBack<String>() {
                        @Override
                        public void isCompleted(final String obj) {
                            if(obj!= null){

                                //lấy ra ngày
                                Date currentTime = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a, dd-MM-yyyy");
                                final String currentDateTimeString = sdf.format(currentTime);

                                //upload lên fie=rebase
                                mReference = mDatabase.getReference("fileGroup");
                                FileShare fileShare = new FileShare(
                                        null,
                                        HandleFBAuth.firebaseAuth.getUid(),
                                        obj,
                                        taskSnapshot.getDownloadUrl().toString(),
                                        currentDateTimeString,
                                        data.getData().getLastPathSegment()
                                );
                                mReference.child(groupKey).push().setValue(fileShare, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        //nếu thành công thì thêm vào list tin nhắn cho mọi người biết có người vừa tải file lên
                                        mReference = mDatabase.getReference("messages");
                                        Message message = new Message(
                                                null,
                                                HandleFBAuth.firebaseAuth.getUid(),
                                                obj,
                                                "<font color='white'><span>Tài liệu: </span>"+data.getData().getLastPathSegment().split(":")[1]+"</font>",
                                                currentDateTimeString
                                        );
                                        mReference.child(groupKey).push().setValue(message, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
        }
    }

    //hàm xử lý kh click 1 item file nhất định, tải file về
    private void onClickItemFile(ListView lv, final List<FileShare> fileShares){
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
                builder.setTitle("Tải tập tin");
                builder.setPositiveButton("Tải", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), fileShares.get(po).getContent(), Toast.LENGTH_SHORT).show();
                        StorageReference islandRef = storageRef.child(fileShares.get(po).getNameFile());
                        int size = fileShares.get(po).getNameFile().split(Pattern.quote(".")).length;
                        Log.i("SIZEEEE", String.valueOf(size));
                        String duoi = fileShares.get(po).getNameFile().split(Pattern.quote("."))[size - 1];
                        File dir = new File(Environment.getExternalStorageDirectory() + "/Download");
                        final File file = new File(dir,fileShares.get(po).getNameFile());
                        try {
                            if(!dir.exists()){
                                //nếu thư mục không tồn tạo thì tạo
                                dir.mkdir();
                            }
                            file.createNewFile();
                            //lấy file
                            islandRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Local temp file has been created
                                    Toast.makeText(getApplicationContext(), "Tải file "+taskSnapshot.getStorage().getName()+" thành công!",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return false;
            }
        });
    }

    private void deleteFile(ListView lv, final List<FileShare> fileShares){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int po, long l) {
                AlertDialog.Builder  builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));
                builder.setTitle("Bạn có muốn xóa?");
                builder.setMessage("Vui lòng nhấn nút OK để xóa, hủy để thoát.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        // Create a storage reference from our app
                        StorageReference desertRef = storageRef.child(fileShares.get(po).getNameFile());
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mReference = mDatabase.getReference("fileGroup/"+groupKey+"/"+fileShares.get(po).getId());
                                mReference.removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                                                dialogInterface.dismiss();
                                            }
                                        }, 600);
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
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
        });
    }
}
