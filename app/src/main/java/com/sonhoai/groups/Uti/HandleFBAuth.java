package com.sonhoai.groups.Uti;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sonhoai.groups.Activities.LoginActivity;
import com.sonhoai.groups.Activities.MainActivity;
import com.sonhoai.groups.R;

import java.util.HashMap;

public class HandleFBAuth {
    public static FirebaseAuth.AuthStateListener authStateListener;
    private static Context context;
    public static FirebaseAuth firebaseAuth;
    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference mReference;

    //khởi tạo
    public HandleFBAuth(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();

    }

    //phương thức để kiểm tra xem đã đăng nhập vào app hay chưa
    public static FirebaseAuth.AuthStateListener fbListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                //lấy ra user hiện tại
                FirebaseUser user = firebaseAuth.getCurrentUser();
                //nếu có user đã đang nhập
                if (user != null) {
                    //success
                    //kiểm tra xem user này đã cập nhật tên hay chưa
                    mReference = mDatabase.getReference("users/" + firebaseAuth.getUid());
                    mReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //nếu đã cập nhật rồi thì vào màn hình startactivity
                            if (dataSnapshot.getValue() != null) {
                                //context này là truyền từ màn hình activity gọi tới class này
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            } else {
                                try {
                                    //nếu chưa có thêm tên, thì tạm thời ản đi cái view login này, ẩn đi các button và edittext
                                    LoginActivity.layoutLogin.setVisibility(View.GONE);

                                    //tạo một dialog để cho người dùng cập nhật tên
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));
                                    builder.setTitle("Vui lòng cập nhật tên");
                                    builder.setMessage("Bạn đã có tài khoản với Email là: " + firebaseAuth.getCurrentUser().getEmail() + ", " +
                                            "nhưng bạn chưa cập nhật tên, vui lòng nhập vào bên dưới và nhấn OK.");

                                    // Set up the input
                                    final EditText input = new EditText(context);
                                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                    input.setHint("Nhập tên của bạn");
                                    input.setTextColor(Color.BLACK);
                                    builder.setView(input);

                                    // Set up the buttons
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //khi người dùng nhấn vào nút ok thì cập nhật lại tên
                                            mReference = mDatabase.getReference("users/" + firebaseAuth.getUid());
                                            //tạo một object có name:username
                                            HashMap<String, String> user = new HashMap<>();
                                            user.put("name", input.getText().toString());
                                            //tiến hành cập nhật
                                            mReference.setValue(user, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    //nếu thành công thì toast ra và intent qua màn hình chính
                                                    Toast.makeText(context, "Thành công!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(context, MainActivity.class);
                                                    context.startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                    //nếu người dùng nhấn cancel thì ẩn đi dialog và cho hiện lại các button và edittext của loginactivity
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            LoginActivity.layoutLogin.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    builder.show();
                                } catch (Exception e) {
                                    LoginActivity.layoutLogin.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    //error
                    Toast.makeText(context, "Chưa có", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return authStateListener;
    }
}
