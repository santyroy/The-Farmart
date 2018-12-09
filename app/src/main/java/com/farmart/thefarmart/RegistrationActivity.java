package com.farmart.thefarmart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.farmart.thefarmart.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private String name;
    private String email;
    private String password;
    private Uri mImageUri;

    private FirebaseAuth mAuthRef;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    private EditText name_et;
    private EditText email_et;
    private EditText password_et;
    private ImageView profile_picture_iv;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

    public static final int PICK_IMAGE_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        name_et = findViewById(R.id.registrationPage_name_et);
        email_et = findViewById(R.id.registrationPage_email_et);
        password_et = findViewById(R.id.registrationPage_password_et);
        profile_picture_iv = findViewById(R.id.registrationPage_profile_picture_iv);
        Button registerButton_btn = findViewById(R.id.registrationPage_register_btn);
        progressDialog = new ProgressDialog(this);
        progressBar = findViewById(R.id.registrationPage_ImageUploadProgressBar_id);

        mAuthRef = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();


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

        profile_picture_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
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
        mAuthRef.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (!(mUploadTask != null && (mUploadTask.isInProgress()))) {
                                sendVerificationEmail();
                            }
                            progressDialog.dismiss();
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
        FirebaseUser user = mAuthRef.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadUserImage();
                                mAuthRef.signOut();
                                Toast.makeText(RegistrationActivity.this, "Registration Successful. Verification email sent", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrationActivity.this, SignInActivity.class));
                                finish();
                            }
                        }
                    });
        }
    }

    private void uploadUserImage() {

        Uri file = mImageUri;
        StorageReference childRef = mStorageRef.child("images/" + mAuthRef.getUid() + "/profile_picture.jpg");
        mUploadTask = childRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0);
                    }
                }, 500);

                String downloadImageURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                User user = new User(name, email, downloadImageURL);
                String uploadID = mDatabaseRef.push().getKey();
                mDatabaseRef.child(uploadID).setValue(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressBar.setProgress((int) progress);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQ_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(profile_picture_iv);
        }
    }
}
