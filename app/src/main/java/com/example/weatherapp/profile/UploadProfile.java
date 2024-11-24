package com.example.weatherapp.profile;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.weatherapp.R;
import com.example.weatherapp.activities.AccountActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadProfile extends AppCompatActivity {
    private static int IMG_REQ = 1;
    private Uri imagePath;
    private ImageView imageUser;
    private static boolean isMediaManagerInitialized = false;
    private String linkPic;
    FirebaseAuth fAuth;
    FirebaseUser user;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_profile);
        if(!isMediaManagerInitialized) {
            Map config = new HashMap();
            config.put("cloud_name", "cloud name");
            config.put("api_key", "api key");
            config.put("api_secret", "api_secret");
//        config.put("secure", true);
            MediaManager.init(this, config);
            isMediaManagerInitialized = true;
        }
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        user = fAuth.getCurrentUser();
        ImageView btnBack = findViewById(R.id.btnBackupload);
        imageUser = findViewById(R.id.imageUser_upload);
        Button btnSelectPic = findViewById(R.id.btnselectPic);
        Button btnUploadPic = findViewById(R.id.btnupdatePic);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadProfile.this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnSelectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPic();
            }
        });
        btnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath != null){
                    MediaManager.get().upload(imagePath).callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("Weather app", "onStarted" + "Starting");
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            Log.d("Weather app", "onStarted" + "Uploading" + totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Log.d("Weather app", "onStarted" + "Successfully" + resultData.get("url"));
                            linkPic = resultData.get("url").toString();
                            if (linkPic.startsWith("http://")) {
                                linkPic = linkPic.replace("http://", "https://");
                            }
                            uploadProfile(user.getUid(), linkPic);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.d("Weather app", "onStarted" + error);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d("Weather app", "onStarted" + error);
                        }
                    }).dispatch();
                }else {
                    Toast.makeText(UploadProfile.this, "Please select picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadProfile(String uid, String linkPic) {
        if(linkPic != null && !linkPic.isEmpty()){
            Map<String, Object> upload = new HashMap<>();
            upload.put("profile", linkPic);
            database.child("user").child(uid).updateChildren(upload).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(UploadProfile.this, "Pic updated successfully", Toast.LENGTH_SHORT).show();
                    }   else{
                        Toast.makeText(UploadProfile.this, "Pic updated failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void selectPic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQ);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMG_REQ && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Glide.with(this)
                    .load(imagePath)
                    .apply(new RequestOptions().centerCrop().circleCrop())
                    .into(imageUser);
        }
    }
}