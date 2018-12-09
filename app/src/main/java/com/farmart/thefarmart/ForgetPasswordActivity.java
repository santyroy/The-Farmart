package com.farmart.thefarmart;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPasswordActivity extends AppCompatActivity {

    private String emailAddress;
    private FirebaseAuth mAuth;

    private EditText email_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email_et = findViewById(R.id.forgetPasswordPage_update_password_et_id);
        Button update_btn = findViewById(R.id.forgetPasswordPage_update_password_btn_id);

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailAddress = email_et.getText().toString().trim();
                if (!emailAddress.isEmpty()) {
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mAuth.signOut();
                                        startActivity(new Intent(ForgetPasswordActivity.this, SignInActivity.class));
                                        finish();
                                        Toast.makeText(ForgetPasswordActivity.this, "Password Reset Email sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ForgetPasswordActivity.this, "Not a valid email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForgetPasswordActivity.this, SignInActivity.class));
        finish();
    }
}
