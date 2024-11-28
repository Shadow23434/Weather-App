package com.example.weatherapp.activities;

import static java.lang.Math.round;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.domains.models.Daily;
import com.example.weatherapp.adapters.DailyAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForecastActivity extends AppCompatActivity {
    private RecyclerView.Adapter dailyAdapter;
    private RecyclerView rvDaily;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        initRvDaily();
        goMain();
    }

    private void initRvDaily() {
       ArrayList<Daily> dailyArrayList = new ArrayList<>();
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

       if (latitude != null && longitude != null) {
           Log.e("Main Intent: ", "latitude = " + latitude);
            fetchWeatherData(dailyArrayList);
       } else {
           Log.e("Main Intent: ", "city is null");
       }

       rvDaily = findViewById(R.id.rv_7days);
       rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

       dailyAdapter = new DailyAdapter(dailyArrayList);
       rvDaily.setAdapter(dailyAdapter);
    }

    private void fetchWeatherData(ArrayList<Daily> dailyArrayList) {
        String url = String.format("https://api.weatherbit.io/v2.0/forecast/daily?&lat=%s&lon=%s&key=%s", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetching API: ", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray data = jsonObject.getJSONArray("data");

                    for (int i = 1; i < 8; i++) {
                        JSONObject dailyData = data.getJSONObject(i);
                        String day = dailyData.getString("datetime");
                        day = dateToDay(day);

                        String icon = dailyData.getJSONObject("weather").getString("icon");
                        String description = dailyData.getJSONObject("weather").getString("description");
                        Double d_max_temp = dailyData.getDouble("high_temp");
                        Double d_min_temp = dailyData.getDouble("low_temp");
                        int max_temp = (int) round(d_max_temp);
                        int min_temp = (int) round(d_min_temp);

                        int pop = dailyData.getInt("pop" );
                        int rh = dailyData.getInt("rh");
                        Double wind_spd_ms = dailyData.getDouble("wind_spd");
                        Double wind_spd_kmh = round(wind_spd_ms * 3.6) / 100.0;
                        int resId = getResources().getIdentifier(icon, "drawable", getPackageName());

                        // Tommorow
                        if (i == 1) {
                            ImageView weatherIcon = (ImageView) findViewById(R.id.weather_icon);
                            TextView weatherDescription = (TextView) findViewById(R.id.weather_description);
                            TextView maxTemp = (TextView) findViewById(R.id.max_temp);
                            TextView minTemp = (TextView) findViewById(R.id.min_temp);
                            TextView precipitation = (TextView) findViewById(R.id.precipitation);
                            TextView humidity = (TextView) findViewById(R.id.humidity);
                            TextView windSpeed = (TextView) findViewById(R.id.wind_speed);

                            runOnUiThread(() -> {
                                Glide.with(this).load(resId).into(weatherIcon);
                                weatherDescription.setText(description);
                                maxTemp.setText(max_temp + "°/");
                                minTemp.setText(min_temp + "°");

                                precipitation.setText(pop + "%");
                                humidity.setText(rh + "%");
                                windSpeed.setText(wind_spd_kmh.toString() + " km/h");
                            });
                        }

                        // Next 6-day
                        if (i>1) dailyArrayList.add(new Daily(day, icon, description, max_temp, min_temp));
                    }
                    runOnUiThread(() -> dailyAdapter.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(ForecastActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void goMain() {
        ImageView backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForecastActivity.this, MainActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    private String dateToDay(String datetime) {
        LocalDate date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = LocalDate.parse(datetime);
        }
        String dayOfWeek = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
        return dayOfWeek;
    }
}
