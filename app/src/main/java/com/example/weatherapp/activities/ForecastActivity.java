package com.example.weatherapp.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.daily.Daily;
import com.example.weatherapp.daily.DailyAdapter;
import com.example.weatherapp.hourly.Hourly;

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

    private String city = "HaNoi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        initRvDaily();
        goMain();
    }

    private void initRvDaily() {
       ArrayList<Daily> dailyArrayList = new ArrayList<>();

       // Fetching API
        fetchWeatherData(dailyArrayList);

       rvDaily = findViewById(R.id.rv_7days);
       rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

       dailyAdapter = new DailyAdapter(dailyArrayList);
       rvDaily.setAdapter(dailyAdapter);
    }

    private void fetchWeatherData(ArrayList<Daily> dailyArrayList) {
        String url = String.format("https://api.weatherbit.io/v2.0/forecast/daily?city=%s&key=%s", city, BuildConfig.weather_api);
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
                        Double maxTemp = dailyData.getDouble("high_temp");
                        Double minTemp = dailyData.getDouble("low_temp");

                        // tommorow

                        // next 6-day
                        if (i>1) dailyArrayList.add(new Daily(day, icon, description, (int) Math.round(maxTemp), (int) Math.round(minTemp)));
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
                startActivity(new Intent(ForecastActivity.this, MainActivity.class));
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