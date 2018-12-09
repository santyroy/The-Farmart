package com.farmart.thefarmart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email_et = findViewById(R.id.signInPage_email_et_id);
        password_et = findViewById(R.id.signInPage_password_et_id);
        Button signin_btn = findViewById(R.id.signInPage_signin_btn_id);
        Button register_btn = findViewById(R.id.signInPage_register_btn_id);
        TextView forgotPassword = findViewById(R.id.signInPage_forot_password_tv_id);
        progressDialog = new ProgressDialog(this);

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
                startActivity(new Intent(SignInActivity.this, RegistrationActivity.class));
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, ForgetPasswordActivity.class));
                finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(SignInActivity.this, "Home Activity", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
            finish();
        }
    }


    /*
     * This method is used to validate the email and password within the app
     * before connecting to the database.
     * It just checks the negative cases for email and password
     *
     * @param email
     * @param password
     * @return boolean
     */
    private boolean validate(String email, String password) {

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

    /*
     * This method signs in the user to its firebase account.
     * If the sign in is successful the user jumps into Home Activity.
     * Else and error toast is displayed
     *
     * @param email
     * @param password
     */

    private void signInToFirebase(String email, String password) {
        progressDialog.setMessage("Verifying...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            emailVerificationCheck();
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void emailVerificationCheck() {
        boolean verifyEmailFlag = mAuth.getCurrentUser().isEmailVerified();
        if (verifyEmailFlag) {
            Toast.makeText(SignInActivity.this, "Authentication Successful.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Verify Email", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }


}