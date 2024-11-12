package com.example.weatherapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        disableFocusEditText();
        onClickBottomAppBar();
        onClickFloatingButton();
    }

    private void disableFocusEditText() {
        EditText editText = findViewById(R.id.editTextSearch);
        editText.clearFocus();
    }

    private void onClickBottomAppBar() {
        ImageView home_appBar_icon = findViewById(R.id.home_appBar_icon);
        ImageView favor_appBar_icon = findViewById(R.id.favor_appBar_icon);
        ImageView account_appBar_icon = findViewById(R.id.account_appBar_icon);

        home_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, MainActivity.class));
            }
        });

        favor_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, FavouriteActivity.class));
            }
        });

        account_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, AccountActivity.class));
            }
        });
    }

    private void onClickFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, AssistantActivity.class));
            }
        });
    }
}