package com.sonhoai.groups.Uti;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sonhoai.groups.Activities.MainActivity;

public class HandleFBAuth {
    public static FirebaseAuth.AuthStateListener authStateListener;
    private static Context context;
    public static FirebaseAuth firebaseAuth;

    public HandleFBAuth(Context context){
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }
    public static FirebaseAuth.AuthStateListener fbListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //success
                    Toast.makeText(context,user.getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                } else {
                    //error
                    Toast.makeText(context,"Chưa có", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return authStateListener;
    }
}
