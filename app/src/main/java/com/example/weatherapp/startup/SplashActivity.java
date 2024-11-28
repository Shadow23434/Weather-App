package com.example.weatherapp.startup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int TIMER = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        RelativeLayout btngetstart = findViewById(R.id.btnGetstart);
        TextView textGetStart = findViewById(R.id.getStartText);
        LottieAnimationView getStartAnimation = findViewById(R.id.getStartAnimation);
        btngetstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStartAnimation.setVisibility(View.VISIBLE);
//                getStartAnimation.playAnimation();
                textGetStart.setVisibility(View.GONE);
                new Handler().postDelayed(this::resetButton, TIMER);
            }

            private void resetButton() {
//                getStartAnimation.pauseAnimation();
                Intent intent = new Intent(SplashActivity.this, SignIn.class);
                startActivity(intent);
                getStartAnimation.setVisibility(View.GONE);
                textGetStart.setVisibility(View.VISIBLE);
            }
        });
        ImageView gif = findViewById(R.id.fallingStars);
        Glide.with(this)
                .load(R.drawable.snow_shower)
                .into(gif);

    }
}
