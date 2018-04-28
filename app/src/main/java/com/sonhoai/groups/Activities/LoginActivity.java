package com.sonhoai.groups.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.HandleFBAuth;

public class LoginActivity extends AppCompatActivity {
    TextView txtHandleRegister;
    HandleFBAuth handleFBAuth;
    private Button btnLogin;
    private EditText edtEmail, edtPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        handleReisterButton();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signin(edtEmail.getText().toString(),edtPass.getText().toString());
            }
        });
    }

    private void init(){
        txtHandleRegister = findViewById(R.id.txtHandleRegister);
        handleFBAuth = new HandleFBAuth(LoginActivity.this);
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtLiEmail);
        edtPass = findViewById(R.id.edtLiPass);
    }
    private void handleReisterButton(){
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

    private void signin(String email, String password){
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
