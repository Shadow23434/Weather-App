package com.example.weatherapp.activities;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.weatherapp.R;
import com.example.weatherapp.settings.BackgroundManager;
import com.example.weatherapp.profile.ChangePassword;
import com.example.weatherapp.profile.UploadProfile;
import com.example.weatherapp.settings.ViewUtils;
import com.example.weatherapp.startup.SignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {
    private ImageView imageUser;
    private FirebaseAuth fAuth;
    private FirebaseUser user;
    private DatabaseReference database;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "myPreferences";
    private static final String SWITCH_KEY = "switchState";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ImageView btnBack = findViewById(R.id.btnBack);
        imageUser = findViewById(R.id.imageUser);
        RelativeLayout btnLogOut = findViewById(R.id.btnLogOut);
        RelativeLayout btnChangePassword = findViewById(R.id.btnChangePassword);
        RelativeLayout btnSenUs= findViewById(R.id.btnSendUs);
        RelativeLayout btnAboutUs = findViewById(R.id.btnAboutUs);
        SwitchCompat lightModeSw = findViewById(R.id.switchLightMode);
        LinearLayout backGround = findViewById(R.id.SettingBackground);
        LinearLayout settingLayout1 = findViewById(R.id.settingLinearLayout1);
        LinearLayout settingLayout2 = findViewById(R.id.settingLinearLayout2);
        LinearLayout settingLayout3 = findViewById(R.id.settingLinearLayout3);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
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
        setUserPic(user.getUid());

//      Apply BackGround now
        applyBackground(backGround);
        applyLinearBackground(settingLayout1);
        applyLinearBackground(settingLayout2);
        applyLinearBackground(settingLayout3);
//      Events
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
        btnSenUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100021954124639"));
                startActivity(intent);
                finish();
            }
        });
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100021954124639"));
                startActivity(intent);
                finish();
            }
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean switchState = sharedPreferences.getBoolean(SWITCH_KEY, false);
        lightModeSw.setChecked(switchState);
        lightModeSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BackgroundManager.getInstance().setBackgroundChanged(isChecked);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SWITCH_KEY, isChecked);
                editor.apply();
                applyBackground(backGround);
                applyLinearBackground(settingLayout1);
                applyLinearBackground(settingLayout2);
                applyLinearBackground(settingLayout3);
//                Set Text color
                if(isChecked){
                    ViewUtils.setTextColor(backGround, getResources().getColor(R.color.black));
                    btnBack.setColorFilter(R.color.black);
                }else {
                    ViewUtils.setTextColor(backGround, getResources().getColor(R.color.white));
                    btnBack.setColorFilter(R.color.white);
                }
            }
        });
    }

    private void applyLinearBackground(LinearLayout settingLayout1) {
        if(BackgroundManager.getInstance().isBackgroundChanged()){
            settingLayout1.setBackgroundResource(R.drawable.rounded_white);
        } else{
            settingLayout1.setBackgroundResource(R.drawable.rounded_purple);
        }
    }

    private void applyBackground(LinearLayout backGround) {
        if(BackgroundManager.getInstance().isBackgroundChanged()){
            backGround.setBackgroundResource(R.drawable.blue_background);
        } else{
            backGround.setBackgroundResource(R.drawable.purple_background);
        }
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