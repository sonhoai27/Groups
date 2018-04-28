package com.sonhoai.groups.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.HandleFBAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtEmail, edtPass;
    private Button btnRegister;
    HandleFBAuth handleFBAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String passwrod = edtPass.getText().toString();
                signup(email,passwrod);
            }
        });
    }

    private void signup(String email, String password){
        HandleFBAuth.firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(getApplicationContext(), HandleFBAuth.firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Notify", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void init(){
        edtEmail = findViewById(R.id.edtRgEmail);
        edtPass = findViewById(R.id.edtRgPass);
        btnRegister = findViewById(R.id.btnRegister);
        handleFBAuth = new HandleFBAuth(getApplicationContext());
    }
}
