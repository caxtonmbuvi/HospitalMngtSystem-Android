package com.example.hospitalmngtapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hospitalmngtapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientRegistrationActivity extends AppCompatActivity {

    private TextView regPageQuestion;

    private TextInputEditText registrationFullName, registrationIdNumber, registrationPhoneNumber, registrationEmail, registrationPassword;
    private Button regButton;
    private CircleImageView profileImage;

    private Uri resultUri;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_registration);

        regPageQuestion = findViewById(R.id.regPageQuestion);
        regPageQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registrationFullName = findViewById(R.id.registrationFullName);
        registrationIdNumber = findViewById(R.id.registrationIdNumber);
        registrationPhoneNumber = findViewById(R.id.registrationPhoneNumber);
        registrationEmail = findViewById(R.id.registrationEmail);
        registrationPassword = findViewById(R.id.registrationPassword);
        regButton = findViewById(R.id.regButton);
        profileImage = findViewById(R.id.profileImage);
        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = registrationEmail.getText().toString().trim();
                final String password = registrationPassword.getText().toString().trim();
                final String fullName = registrationFullName.getText().toString().trim();
                final String idNumber = registrationIdNumber.getText().toString().trim();
                final String phoneNumber = registrationPhoneNumber.getText().toString().trim();


                if (TextUtils.isEmpty(email)){
                    registrationEmail.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    registrationPassword.setError("Your password required");
                    return;
                }

                if (TextUtils.isEmpty(fullName)){
                    registrationFullName.setError("Full name is required");
                    return;
                }

                if (TextUtils.isEmpty(idNumber)){
                    registrationIdNumber.setError("ID number required");
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)){
                    registrationPhoneNumber.setError("Phone number is required");
                    return;
                }

                if (resultUri == null){
                    Toast.makeText(PatientRegistrationActivity.this, "Profile image is required", Toast.LENGTH_SHORT).show();
                } else {
                    loader.setMessage("Registration in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                String error = task.getException().toString();
                                Toast.makeText(PatientRegistrationActivity.this, "Error Occurred " +error, Toast.LENGTH_SHORT).show();
                            } else {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);

                                HashMap userInfo = new HashMap();
                                userInfo.put("id", currentUserId);
                                userInfo.put("name", fullName);
                                userInfo.put("email", email);
                                userInfo.put("idnumber", idNumber);
                                userInfo.put("phonenumber", phoneNumber);
                                userInfo.put("type", "patient");

                                //saving the data to firebase
                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(PatientRegistrationActivity.this, "Details Set Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PatientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                        loader.dismiss();
                                    }
                                });

                                //adding image to db
                                if (resultUri != null){
                                    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile pictures").child(currentUserId);
                                    Bitmap bitmap = null;

                                    try {
                                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                                    byte[] data = byteArrayOutputStream.toByteArray();

                                    //creating an upload task
                                    UploadTask uploadTask = filepath.putBytes(data);

                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            finish();
                                            return;
                                        }
                                    });
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            if (taskSnapshot.getMetadata() != null){
                                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imageUrl = uri.toString();
                                                        Map newImageMap = new HashMap();
                                                        newImageMap.put("profilepictureurl", imageUrl);

                                                        //saving image to db
                                                        userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(PatientRegistrationActivity.this, "Registration was successful", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(PatientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        finish();

                                                    }
                                                });
                                            }
                                        }
                                    });
                                    //directing user to new activity
                                    Intent intent = new Intent(PatientRegistrationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    loader.dismiss();
                                }
                            }
                        }
                    });
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null ){
            resultUri = data.getData();
            profileImage.setImageURI(resultUri);
        }
    }
}