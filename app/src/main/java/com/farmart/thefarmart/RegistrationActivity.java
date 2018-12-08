package com.farmart.thefarmart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.farmart.thefarmart.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private String name;
    private String email;
    private String password;
    private FirebaseAuth mAuth;

    private EditText name_et;
    private EditText email_et;
    private EditText password_et;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        name_et = findViewById(R.id.registrationPage_name_et);
        email_et = findViewById(R.id.registrationPage_email_et);
        password_et = findViewById(R.id.registrationPage_password_et);
        Button registerButton_btn = findViewById(R.id.registrationPage_register_btn);
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        registerButton_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = name_et.getText().toString();
                email = email_et.getText().toString();
                password = password_et.getText().toString();
                boolean isValid = validate(name, email, password);
                if (isValid) {
                    createAccount();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegistrationActivity.this, SignInActivity.class));
        finish();
    }

    private boolean validate(String name, String email, String password) {
        if (name == null || name.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (email == null || email.isEmpty()) {
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

    private void createAccount() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                sendVerificationEmail();
                            }

                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegistrationActivity.this, "Registration Not Successful",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadUser();
                                mAuth.signOut();
                                Toast.makeText(RegistrationActivity.this, "Registration Successful. Verification email sent", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrationActivity.this, SignInActivity.class));
                                finish();

                            }
                        }
                    });
        }
    }

    private void uploadUser() {
        //Creating an instance of FirebaseDatabase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        //Creating a database reference for the current user using its UUID
        DatabaseReference databaseReference = firebaseDatabase.getReference(mAuth.getUid());
        //Passing the user as a parameter to database reference
        databaseReference.setValue(new User(name, email));
    }
}
