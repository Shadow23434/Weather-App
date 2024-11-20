package com.example.weatherapp.activities;

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

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.hourly.HourlyAdapter;
import com.example.weatherapp.hourly.Hourly;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView rvHourly;
    private String city = "HaNoi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRvHourly();
        onClickBottomAppBar();
        onClickFloatingButton();
        goNext7Day();
    }

    private void initRvHourly() {
        ArrayList<Hourly> items = new ArrayList<>();

        // Fetching api here
        fetchWeatherData(items);

        rvHourly = findViewById(R.id.rv_hourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapterHourly = new HourlyAdapter(items);
        rvHourly.setAdapter(adapterHourly);
    }

   private void fetchWeatherData(ArrayList<Hourly> items) {
        String url = String.format("https://api.weatherbit.io/v2.0/forecast/hourly?city=%s&key=%s&hours=12", city, BuildConfig.weather_api);
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

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject hourlyData = data.getJSONObject(i);
                        String time = hourlyData.getString("timestamp_local");
                        time = time.substring(11, 16);

                        Double temperature = hourlyData.getDouble("temp");
                        String icon = hourlyData.getJSONObject("weather").getString("icon");

//                        Log.e("Time: ", time);
//                        Log.e("Temperature: ", String.valueOf(temperature));
//                        Log.e("Icon: ", icon);

                        items.add(new Hourly(time, (int) Math.round(temperature), icon));
                    }
                    runOnUiThread(() -> adapterHourly.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void onClickBottomAppBar() {
        ImageView search_appBar_icon = findViewById(R.id.search_appBar_icon);
        ImageView favor_appBar_icon = findViewById(R.id.favor_appBar_icon);
        ImageView account_appBar_icon = findViewById(R.id.account_appBar_icon);

        search_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        favor_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FavouriteActivity.class));
            }
        });

        account_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        });
    }

    private void onClickFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AssistantActivity.class));
            }
        });
    }

    private void goNext7Day() {
        TextView next7Day = findViewById(R.id.next7Day);
        next7Day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ForecastActivity.class));
            }
        });
    }
}

