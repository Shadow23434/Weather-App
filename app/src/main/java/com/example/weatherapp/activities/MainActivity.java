package com.example.weatherapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.adapters.HourlyAdapter;
import com.example.weatherapp.domains.Hourly;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecycleView();
        onClickBottomAppBar();
        onClickFloatingButton();
        goNext7Day();
    }

    private void initRecycleView() {
        ArrayList<Hourly> items = new ArrayList<>();

        // Fetching api here
        fetchWeatherData(items);

        recyclerView = findViewById(R.id.recycleViewTest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapterHourly = new HourlyAdapter(items);
        recyclerView.setAdapter(adapterHourly);
    }

   private void fetchWeatherData(ArrayList<Hourly> items) {
    String url = "https://api.weatherbit.io/v2.0/forecast/hourly?city=Hanoi&key=62728dd219764b80b15b71c0ca79d79c&hours=24";

    OkHttpClient client = new OkHttpClient();

    Request request = new Request.Builder()
            .url(url)
            .build();

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
                    double temperature = hourlyData.getDouble("temp");
                    String weather = hourlyData.getJSONObject("weather").getString("description");

                    items.add(new Hourly(time, (int) temperature, weather));
                }

                runOnUiThread(() -> adapterHourly.notifyDataSetChanged());
            }
        } catch (Exception e) {
            e.printStackTrace();
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

