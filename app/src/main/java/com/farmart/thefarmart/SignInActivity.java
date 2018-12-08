package com.farmart.thefarmart;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private EditText email_et;
    private EditText password_et;
    private FirebaseAuth mAuth;
    public static final String TAG = "SIGN_IN_ACTIVITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email_et = findViewById(R.id.email_et_id);
        password_et = findViewById(R.id.password_et_id);
        Button signin_btn = findViewById(R.id.signin_btn_id);
        Button register_btn = findViewById(R.id.register_btn_id);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_et.getText().toString().trim();
                String password = password_et.getText().toString().trim();
                boolean flag = validate(email, password);
                if (flag) {
                    signInToFirebase(email, password);
                }
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO go to Registration Page
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null && currentUser.isEmailVerified()) {
            Toast.makeText(SignInActivity.this, "Home Activity", Toast.LENGTH_SHORT).show();
            //TODO GOTO HOME ACTIVITY
        } else {
            Toast.makeText(SignInActivity.this, "Please Verify Email", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean validate(String email, String password) {

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password == null || password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 6) {
            Toast.makeText(this, "Minimum password length is 6", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            Pattern p = Pattern.compile(ePattern);
            Matcher m = p.matcher(email);
            if (!m.matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void signInToFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignInActivity.this, "Authentication Successful.",
                                    Toast.LENGTH_SHORT).show();
                            if (user!=null && user.isEmailVerified()) {
                                //TODO GOTO HOME ACTIVITY
                            } else {
                                Toast.makeText(SignInActivity.this, "Please Verify Email", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}