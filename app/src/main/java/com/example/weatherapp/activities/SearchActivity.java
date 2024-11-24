package com.example.weatherapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

        onEnterEditSearch();

        disableFocusEditText();
        onClickBottomAppBar();
        onClickFloatingButton();
    }

    private void onEnterEditSearch() {
        EditText editText = (EditText) findViewById(R.id.edit_txt_search);
        if (editText.getText() != null) {
            editText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                        // Perform action on key press
                        Log.e("Search: ", "Search for " + String.valueOf(editText.getText()));
                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.putExtra("editTextSearch", editText.getText().toString());
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        } else {
            Log.e("Search: ", "EditText is null");
        }
    }

    private void disableFocusEditText() {
        EditText editText = findViewById(R.id.edit_txt_search);
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