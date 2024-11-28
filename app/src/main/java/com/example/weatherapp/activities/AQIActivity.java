package com.example.weatherapp.activities;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nitish.typewriterview.TypeWriterView;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AQIActivity extends AppCompatActivity {
    private String latitude;
    private String longitude;
    private String AQIData;
    private TypeWriterView shortAQIDescription;
    private ImageView dropDownIcon;
    private ImageView dropUpIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqiactivity);

        shortAQIDescription = findViewById(R.id.aqi_description);
        dropDownIcon = findViewById(R.id.ic_drop_down);
        dropUpIcon = findViewById(R.id.ic_drop_up);

        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        if (latitude != null && !latitude.isEmpty()
            && longitude != null && !longitude.isEmpty()) {
            fetchAQIData(() -> {
                generateShortAQIDescription();
            });
        }
        else {
            Log.e("Main Intent: ", "Latitude is null");
        }

        onClickFurtherInformation();
        onClickDropUpIcon();
        onClickDropDownIcon();
        goMain();
    }

    private void onClickFurtherInformation() {
        TextView furtherInformation = findViewById(R.id.further_information);
        furtherInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Air_quality_index"));
                startActivity(intent);
            }
        });
    }

    private void fetchAQIData(Runnable onComplete) {
        String url = String.format("https://api.weatherbit.io/v2.0/current/airquality?lat=%s&lon=%s&key=%s", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetch AQI data: ", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    AQIData = responseBody;
                    onComplete.run();
                    JSONObject data = new JSONObject(responseBody);

                    String cityName = data.getString("city_name");
                    String countryName = getCountryName(data.getString("country_code"));
                    int aqi = data.getJSONArray("data").getJSONObject(0).getInt("aqi");
                    Double d_pm25 = data.getJSONArray("data").getJSONObject(0).getDouble("pm25");
                    Double d_pm10 = data.getJSONArray("data").getJSONObject(0).getDouble("pm10");
                    Double d_so2 = data.getJSONArray("data").getJSONObject(0).getDouble("so2");
                    Double d_no2 = data.getJSONArray("data").getJSONObject(0).getDouble("no2");
                    Double d_o3= data.getJSONArray("data").getJSONObject(0).getDouble("o3");
                    Double d_co= data.getJSONArray("data").getJSONObject(0).getDouble("co");

                    String pm25 = String.valueOf(Math.round(d_pm25 * 10.0) / 10.0);
                    String pm10 = String.valueOf(Math.round(d_pm10 * 10.0) / 10.0);
                    String so2 = String.valueOf(Math.round(d_so2 * 10.0) / 10.0);
                    String no2 = String.valueOf(Math.round(d_no2 * 10.0) / 10.0);
                    String o3 = String.valueOf(Math.round(d_o3 * 10.0) / 10.0);
                    String co = String.valueOf(Math.round(d_co * 10.0) / 10.0);

                    TextView tvCityName = findViewById(R.id.city);
                    TextView tvCountryName = findViewById(R.id.country);
                    TextView tvPm25 = findViewById(R.id.pm25);
                    TextView tvPm10 = findViewById(R.id.pm10);
                    TextView tvSo2 = findViewById(R.id.so2);
                    TextView tvNo2 = findViewById(R.id.no2);
                    TextView tvO3= findViewById(R.id.o3);
                    TextView tvCo = findViewById(R.id.co);
                    TextView tvAqi = findViewById(R.id.aqi_index);
                    TextView tvCondition = findViewById(R.id.aqi_condition);

                    setAQIColor(aqi, tvAqi, tvCondition);

                    runOnUiThread(() -> {
                        tvCityName.setText(cityName + ", ");
                        tvCountryName.setText(countryName);
                        tvPm25.setText(pm25);
                        tvPm10.setText(pm10);
                        tvSo2.setText(so2);
                        tvNo2.setText(no2);
                        tvO3.setText(o3);
                        tvCo.setText(co);
                        tvAqi.setText(aqi + "");
                    });
                }
                else {
                    Log.e("Fetch AQI data: ", "response is null");
                }
            } catch (Exception e) {
                Log.e("Fetch AQI data: ", "Error");
                e.printStackTrace();
            }
        }).start();
    }

    private void generateShortAQIDescription() {
        SafetySetting safetySetting = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH);
        GenerationConfig.Builder builder = new GenerationConfig.Builder();
        builder.temperature = 1.0f;
        builder.topK = 40;
        builder.topP = 0.95f;
        GenerationConfig generationConfig = builder.build();
        RequestOptions requestOptions = new RequestOptions();

        Content systemInstruction = new Content.Builder()
                .addText("You are an AQI assistant providing clear, very concise, and accurate information about air quality. Your goal is to inform users about current air quality levels, explain the potential health impacts, and provide actionable advice based on AQI values. Include details such as AQI categories (Good, Moderate, Unhealthy, etc.), specific pollutants (PM2.5, PM10, ozone, etc.), and tips for sensitive groups or the general public. Be approachable, empathetic, and informative, adapting your tone to user needs while prioritizing health and safety.")
                .build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                BuildConfig.gemini_api,
                generationConfig,
                Collections.singletonList(safetySetting),
                requestOptions,
                null,
                null,
                systemInstruction
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(AQIData).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        Log.e("ShortWeatherDescription:  ", "Fetching successful");
                        runOnUiThread(() -> shortAQIDescription.animateText(result.getText()));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("ShortWeatherDescription: ", "Fetching Fail!!!");
                        Log.e("ShortWeatherDescription: ", t.toString());
                        runOnUiThread(() -> shortAQIDescription.animateText("Sorry, something went wrong"));
                    }
                },
                executor);
    }

    private void setAQIColor(int aqi, TextView tvAqi, TextView tvCondition) {
        try {
            int color;
            if (aqi >= 0 && aqi <= 50) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.good); // Green
                runOnUiThread(() -> tvCondition.setText("Good"));
            } else if (aqi >= 51 && aqi <= 100) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.moderate); // Yellow
                runOnUiThread(() -> tvCondition.setText("Moderate"));
            } else if (aqi >= 101 && aqi <= 150) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.unhealthyForSensitiveGroups); // Orange
                runOnUiThread(() -> tvCondition.setText("Unhealthy for sensitive groups"));
            } else if (aqi >= 151 && aqi <= 200) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.unhealthy); // Red
                runOnUiThread(() -> tvCondition.setText("Unhealthy"));
            } else if (aqi >= 201 && aqi <= 300) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.veryUnhealthy); // Purple
                runOnUiThread(() -> tvCondition.setText("Very unhealthy"));
            } else if (aqi >= 301 && aqi <= 500) {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.hazardous); // Maroon
                runOnUiThread(() -> tvCondition.setText("Hazardous"));
            } else {
                color = ContextCompat.getColor(tvAqi.getContext(), R.color.good); // Default color for invalid AQI
                runOnUiThread(() -> tvCondition.setText("Null"));
            }

            runOnUiThread(() -> {
                tvAqi.setTextColor(color);
                tvCondition.setTextColor(color);
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void onClickDropDownIcon() {
        dropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortAQIDescription.setVisibility(View.VISIBLE);
                dropDownIcon.setVisibility(View.INVISIBLE);
                dropUpIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickDropUpIcon() {
        dropUpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortAQIDescription.setVisibility(View.GONE);
                dropUpIcon.setVisibility(View.INVISIBLE);
                dropDownIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    private void goMain() {
        ImageView backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AQIActivity.this, MainActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    @NonNull
    private String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }
}
