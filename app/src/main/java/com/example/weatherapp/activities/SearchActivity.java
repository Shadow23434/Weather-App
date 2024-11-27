package com.example.weatherapp.activities;

import static java.lang.Math.round;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        if (latitude != null && !latitude.isEmpty() &&
                longitude != null && !longitude.isEmpty()) {
            Log.e("Suggestion Intent: ", "Latitude = " + latitude + ", " + "Longitude = " + longitude);
            fetchHourlyWeatherData();
        } else Log.e("Suggestion Intent: ", "latitude is null");

        onClickCurrentLocationLayout();
        onSearch();
        onClickBottomAppBar();
        onClickFloatingButton();
    }

    private void onClickCurrentLocationLayout() {
        LinearLayout currentLocationLayout = findViewById(R.id.current_location_layout);
        currentLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    private void fetchHourlyWeatherData() {
        // Hourly weather data
        String url = String.format("https://api.weatherbit.io/v2.0/forecast/hourly?&lat=%s&lon=%s&key=%s&hours=12", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetching hourly API: ", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);

                    String city = jsonObject.getString("city_name");
                    String country = getCountryName(jsonObject.getString("country_code"));
                    String description = data.getJSONObject("weather").getString("description");
                    int temperature = (int) Math.round(data.getDouble("temp"));
                    String icon = data.getJSONObject("weather").getString("icon");


                    TextView cityName = (TextView) findViewById(R.id.city_name);
                    TextView countryName = (TextView) findViewById(R.id.country_name);
                    TextView weatherDescription = (TextView) findViewById(R.id.weather_description);
                    TextView temp = (TextView) findViewById(R.id.temp);
                    ImageView weatherIcon = (ImageView) findViewById(R.id.weather_icon);
                    int resId = getResources().getIdentifier(icon, "drawable", getPackageName());

                    runOnUiThread( () -> {
                        cityName.setText(city);
                        countryName.setText(country);
                        weatherDescription.setText(description);
                        temp.setText(temperature + "℃");
                        Glide.with(this).load(resId).into(weatherIcon);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching hourly API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void onSearch() {
        EditText editText = (EditText) findViewById(R.id.edit_txt_search);
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, SuggestionActivity.class));
            }
        });
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

    @NonNull
    private String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }
}
