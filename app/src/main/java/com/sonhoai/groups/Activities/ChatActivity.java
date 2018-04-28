package com.sonhoai.groups.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import android.widget.Button;
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
        Bundle bundle = getIntent().getExtras();
        groupKey = bundle.getString("keyGroup");
        groupName = bundle.getString("keyName");
        init();
        addMessage();
        getMessages();
        pickFile();
    }

    private void init(){
        setTitle("Nhóm: "+groupName);
        btnSend = findViewById(R.id.btnSend);
        lvMessages = findViewById(R.id.lvMessages);
        edtInput = findViewById(R.id.edtInputMessage);
        addFile = findViewById(R.id.imgSendFile);
        imgSendFile = findViewById(R.id.imgSendFile);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages,ChatActivity.this);
        lvMessages.setAdapter(messageAdapter);
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
        }
        return false;
    }

    private void showFile() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_manage_files, null);

        builder.setView(view);
        builder.setCancelable(true);

        final ListView lvListFile = view.findViewById(R.id.lvListFile);
        loadListFiles(new CallBack<List<FileShare>>() {
            @Override
            public void isCompleted(List<FileShare> obj) {
                FileAdapter fileAdapter = new FileAdapter(ChatActivity.this, obj);
                lvListFile.setAdapter(fileAdapter);
                onClickItemFile(lvListFile, obj);
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
        builder.show();
    }

    private void getMessages(){
        mReference = mDatabase.getReference("messages/"+groupKey);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
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
                            Date currentTime = Calendar.getInstance().getTime();
                            SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a, dd-MM-yyyy");
                            String currentDateTimeString = sdf.format(currentTime);
                            mReference = mDatabase.getReference("messages");
                            Message message = new Message(
                                    null,
                                    HandleFBAuth.firebaseAuth.getUid(),
                                    obj,
                                    edtInput.getText().toString(),
                                    currentDateTimeString
                            );
                            mReference.child(groupKey).push().setValue(message, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    edtInput.setText("");
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

    private void showUser(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_show_user, null);

        builder.setView(view);
        builder.setCancelable(false);

        final ListView lvListUserClass = view.findViewById(R.id.lvListUserClass);
        loadUserClass(new CallBack<List<GroupUser>>() {
            @Override
            public void isCompleted(List<GroupUser> obj) {
                UserChatAdapter userAdapter = new UserChatAdapter(ChatActivity.this, obj);
                lvListUserClass.setAdapter(userAdapter);
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

        builder.show();
    }

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
    private void pickFile(){
        imgSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ChatActivity.this, R.style.myDialog));

                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dialog_upload_file, null);

                builder.setView(view);
                builder.setCancelable(true);

                TextView txtPickFile = view.findViewById(R.id.txtPickFile);

                final AlertDialog dialog = builder.show();
                txtPickFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                                Date currentTime = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a, dd-MM-yyyy");
                                final String currentDateTimeString = sdf.format(currentTime);
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
                                        mReference = mDatabase.getReference("messages");
                                        Message message = new Message(
                                                null,
                                                HandleFBAuth.firebaseAuth.getUid(),
                                                obj,
                                                "<font color='#08f'><b>Tài liệu: </b>"+data.getData().getLastPathSegment().split(":")[1]+"</font>",
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

    private void onClickItemFile(ListView lv, final List<FileShare> fileShares){
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int po, long l) {
                Toast.makeText(getApplicationContext(), fileShares.get(po).getContent(), Toast.LENGTH_SHORT).show();
                StorageReference islandRef = storageRef.child(fileShares.get(po).getNameFile());
                int size = fileShares.get(po).getNameFile().split(Pattern.quote(".")).length;
                Log.i("SIZEEEE", String.valueOf(size));
                String duoi = fileShares.get(po).getNameFile().split(Pattern.quote("."))[size - 1];
                File dir = new File(Environment.getExternalStorageDirectory() + "/Download");
                final File file = new File(dir,fileShares.get(po).getNameFile());
                try {
                    if(!dir.exists()){
                        dir.mkdir();
                    }
                    file.createNewFile();
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
                return false;
            }
        });
    }
}
