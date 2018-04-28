package com.sonhoai.groups.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.HandleFBAuth;

public class LoginActivity extends AppCompatActivity {
    TextView txtHandleRegister;
    HandleFBAuth handleFBAuth;
    private Button btnLogin;
    private EditText edtEmail, edtPass;
    public static LinearLayout layoutLogin;
    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        handleReisterButton();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReference = mDatabase.getReference("users/" + HandleFBAuth.firebaseAuth.getUid());
                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            signin(edtEmail.getText().toString(), edtPass.getText().toString());
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.myDialog));
                            builder.setTitle("Vui lòng cập nhật tên");
                            builder.setMessage("Bạn đã có tài khoản với Email là: " + HandleFBAuth.firebaseAuth.getCurrentUser().getEmail() + ", " +
                                    "nhưng bạn chưa cập nhật tên, vui lòng nhập vào bên dưới và nhấn OK.");

                            // Set up the input
                            final EditText input = new EditText(getApplicationContext());
                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setHint("Nhập tên của bạn");
                            input.setTextColor(Color.BLACK);

                            builder.setView(input);
                            builder.setCancelable(false);
                            // Set up the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(), input.getText().toString(), Toast.LENGTH_SHORT).show();
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

    private void init() {
        layoutLogin = findViewById(R.id.layoutLogin);
        txtHandleRegister = findViewById(R.id.txtHandleRegister);
        handleFBAuth = new HandleFBAuth(LoginActivity.this);
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtLiEmail);
        edtPass = findViewById(R.id.edtLiPass);
    }

    private void handleReisterButton() {
        txtHandleRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        HandleFBAuth.firebaseAuth.addAuthStateListener(HandleFBAuth.fbListener());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (HandleFBAuth.authStateListener != null) {
            HandleFBAuth.firebaseAuth.removeAuthStateListener(HandleFBAuth.fbListener());
        }
    }

    private void signin(String email, String password) {
        HandleFBAuth.firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("AAAA", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
