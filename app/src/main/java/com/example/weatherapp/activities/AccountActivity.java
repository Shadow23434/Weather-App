package com.example.weatherapp.activities;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.weatherapp.R;
import com.example.weatherapp.profile.ChangePassword;
import com.example.weatherapp.profile.UploadProfile;
import com.example.weatherapp.startup.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private ImageView imageUser;
    FirebaseAuth fAuth;
    FirebaseUser user;
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ImageView btnBack = findViewById(R.id.btnBack);
        imageUser = findViewById(R.id.imageUser);
        RelativeLayout btnLogOut = findViewById(R.id.btnLogOut);
        RelativeLayout btnChangePassword = findViewById(R.id.btnChangePassword);
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        if(user == null){
            Intent intent = new Intent(AccountActivity.this, SignIn.class);
            startActivity(intent);
            finish();
        }
        setUserName(user.getUid());
        setUserEmail(user.getUid());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        imageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, UploadProfile.class);
                startActivity(intent);
                finish();
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ChangePassword.class);
                startActivity(intent);
            }
        });
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AccountActivity.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        });
        setUserPic(user.getUid());
    }



    private void setUserPic(String uid) {
        database.child("user").child(uid).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String urlPic = snapshot.getValue(String.class);
                if(urlPic != null && !urlPic.isEmpty()) {
                    Glide.with(AccountActivity.this)
                            .load(urlPic)
//                            .transform(new CircleCrop())
                            .apply(new RequestOptions().centerCrop().circleCrop())
                            .error(R.drawable.account_icon) // Đặt hình ảnh lỗi nếu không thể tải xuống
                            .into(imageUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserEmail(String uid) {
        database.child("user").child(uid).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.getValue(String.class);
                TextView user_email = findViewById(R.id.user_email);
                user_email.setText(email);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setUserName(String userID) {
        database.child("user").child(userID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                TextView user_name = findViewById(R.id.user_name);
                user_name.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}