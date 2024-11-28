package com.example.weatherapp.activities;

import static java.lang.Math.round;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.domains.WeatherData;
import com.example.weatherapp.domains.WeatherDataCallback;
import com.example.weatherapp.adapters.HourlyAdapter;
import com.example.weatherapp.domains.models.Hourly;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nitish.typewriterview.TypeWriterView;

import org.json.JSONArray;
import org.json.JSONObject;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements LocationListener, WeatherDataCallback {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView rvHourly;
    private LocationManager locationManager; // request location
    private AlertDialog enableGPSDialog; // prompt user to enable GPS service
    private int retryCount = 0;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private String latitude;
    private String longitude;
    private WeatherData weatherData;
    private String currentLocation;
    private String currentTemperature;
    private String weatherCondition;
    private boolean isNight;
    private TypeWriterView shortWeatherDescription;
    private ImageView dropDownIcon;
    private ImageView dropUpIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dropDownIcon = (ImageView) findViewById(R.id.ic_drop_down);
        dropUpIcon = (ImageView) findViewById(R.id.ic_drop_up);
        shortWeatherDescription = findViewById(R.id.short_weather_description);
        weatherData = new WeatherData();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        String intentLatitude = getIntent().getStringExtra("latitude");
        String intentLongitude = getIntent().getStringExtra("longitude");

        if (intentLatitude != null && intentLongitude != null) {
            latitude = intentLatitude;
            longitude = intentLongitude;
            initRvHourly();
        }
        else {
            Log.e("Intent: ", "Latitude and Longitude is null");
            getCurrentLocation();
        }

        goAQI();
        onClickDropUpIcon();
        onClickDropDownIcon();
        onClickLocationIcon();
        onClickBottomAppBar();
        onClickFloatingButton();
        goNext7Day();
    }

    private void goAQI() {
        LinearLayout aqiLayout = findViewById(R.id.aqi_layout);
        aqiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AQIActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    private void onClickDropDownIcon() {
        dropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortWeatherDescription.setVisibility(View.VISIBLE);
                dropDownIcon.setVisibility(View.INVISIBLE);
                dropUpIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickDropUpIcon() {
        dropUpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortWeatherDescription.setVisibility(View.GONE);
                dropUpIcon.setVisibility(View.INVISIBLE);
                dropDownIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    private void generateShortWeatherDescription() {
        SafetySetting safetySetting = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH);
        GenerationConfig.Builder builder = new GenerationConfig.Builder();
        builder.temperature = 1.0f;
        builder.topK = 40;
        builder.topP = 0.95f;
        GenerationConfig generationConfig = builder.build();
        RequestOptions requestOptions = new RequestOptions();

        Content systemInstruction = new Content.Builder()
                .addText("You are a weather assistant. Generate a very short, but whimsical description of the weather, offer practical tips based on the weather conditions, such as clothing suggestions, travel precautions, or outdoor activity recommendations, based on the given information")
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
        Content content = new Content.Builder().addText(
                        String.format("location = %s;\n", currentLocation) +
                        String.format("currentTemperature = %s;\n", currentTemperature) +
                        String.format("weatherCondition = %s;\n", weatherCondition) +
                        String.format("isNight = %b", isNight)
        ).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        Log.e("ShortWeatherDescription:  ", "Fetching successful");
                        runOnUiThread(() -> shortWeatherDescription.animateText(result.getText()));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("ShortWeatherDescription: ", "Fetching Fail!!!");
                        Log.e("ShortWeatherDescription: ", t.toString());
                        runOnUiThread(() -> shortWeatherDescription.animateText("Sorry, something went wrong"));
                    }
                },
                executor);
    }

    private void initRvHourly() {
        ArrayList<Hourly> items = new ArrayList<>();
        rvHourly = findViewById(R.id.rv_hourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterHourly = new HourlyAdapter(items);
        rvHourly.setAdapter(adapterHourly);

        if (latitude != null && longitude != null) {
            fetchDailyWeatherData();
            fetchHourlyWeatherData(items, () -> {
                    if (currentLocation != null &&  currentTemperature!= null && weatherCondition != null) {
                        generateShortWeatherDescription();
                    }
                    else Log.e("short weather description", "error");
            });
        }
        else {
            Log.e("City: ", "city not found");
            Toast.makeText(this, "Please enter the right name and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchHourlyWeatherData(ArrayList<Hourly> items, Runnable onComplete) {
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
                    JSONArray data = jsonObject.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject hourlyData = data.getJSONObject(i);
                        String time = hourlyData.getString("timestamp_local");
                        time = time.substring(11, 16);
                        Double d_temperature = hourlyData.getDouble("temp");
                        int temperature = (int) Math.round(d_temperature);
                        String icon = hourlyData.getJSONObject("weather").getString("icon");
                        String description = hourlyData.getJSONObject("weather").getString("description");

                        // Current hour
                        if (i == 0) {
                            String cityName = jsonObject.getString("city_name");
                            int resId = getResources().getIdentifier(icon, "drawable", getPackageName());
                            ImageView weatherIcon = (ImageView) findViewById(R.id.weather_icon);
                            TextView temp = (TextView) findViewById(R.id.temp);
                            TextView weatherDescription = (TextView) findViewById(R.id.weather_description);

                            weatherData.setCurrentLocation(cityName);
                            weatherData.setCurrentTemperature(String.valueOf(temperature));
                            weatherData.setWeatherCondition(description);
                            weatherData.setNight((icon.charAt(icon.length()-1) == 'n')? true : false);

                            runOnUiThread( () -> {
                                onWeatherDataFetched(weatherData);
                                onComplete.run();
                                Glide.with(this).load(resId).into(weatherIcon);
                                temp.setText(temperature + "℃");
                                weatherDescription.setText(description);
                            });
                        }

                        // Next 11-hour
                        if (i>0) items.add(new Hourly(time, temperature, icon));
                    }
                    if (items != null) runOnUiThread(() -> adapterHourly.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching hourly API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchDailyWeatherData() {
        String url = String.format("https://api.weatherbit.io/v2.0/forecast/daily?&lat=%s&lon=%s&key=%s", latitude, longitude, BuildConfig.weather_api);
        Log.e("Fetching daily API: ", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject currentDayData = jsonObject.getJSONArray("data").getJSONObject(0);

                    String cityName = jsonObject.getString("city_name");
                    String countryName = getCountryName(jsonObject.getString("country_code"));
                    Double d_max_temp = currentDayData.getDouble("high_temp");
                    Double d_min_temp = currentDayData.getDouble("low_temp");
                    int max_temp = (int) round(d_max_temp);
                    int min_temp = (int) round(d_min_temp);

                    Double d_precipitation = currentDayData.getDouble("precip");
                    int precipitation = (int) Math.round(d_precipitation);
                    String humidity = currentDayData.getString("rh");
                    Double wind_spd_ms = currentDayData.getDouble("wind_spd");
                    Double wind_spd_kmh = round(wind_spd_ms * 3.6) / 100.0;
                    String windSpeed = wind_spd_kmh.toString();

                    Double d_visibility = currentDayData.getDouble("vis");
                    int visibility = (int) Math.round(d_visibility);
                    String pressure = currentDayData.getString("pres");
                    Long l_sunset = currentDayData.getLong("sunset_ts");
                    Long l_sunrise = currentDayData.getLong("sunrise_ts");
                    String sunset = convertTimestamp(l_sunset);
                    String sunrise = convertTimestamp(l_sunrise);

                    TextView tvCity = (TextView) findViewById(R.id.city);
                    TextView tvCountry = (TextView) findViewById(R.id.country);
                    TextView maxTemp = (TextView) findViewById(R.id.max_temp);
                    TextView minTemp = (TextView) findViewById(R.id.min_temp);

                    TextView tvPrecipitation = findViewById(R.id.precipitation);
                    TextView tvHumidity = findViewById(R.id.humidity);
                    TextView tvWindSpeed = findViewById(R.id.wind_speed);

                    TextView tvVisibility = findViewById(R.id.visibility);
                    TextView tvPressure = findViewById(R.id.pressure);
                    TextView tvSunset = findViewById(R.id.sunset);
                    TextView tvSunrise = findViewById(R.id.sunrise);

                    runOnUiThread(() -> {
                        tvCity.setText(cityName + ", ");
                        tvCountry.setText(countryName);
                        maxTemp.setText(max_temp + "°/");
                        minTemp.setText(min_temp + "°");

                        tvPrecipitation.setText(precipitation + "%");
                        tvHumidity.setText(humidity + "%");
                        tvWindSpeed.setText(windSpeed + " Km");

                        tvVisibility.setText(visibility + " Km");
                        tvPressure.setText(pressure + " Mb");
                        tvSunset.setText(sunset);
                        tvSunrise.setText(sunrise);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching daily API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching daily weather data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void onClickLocationIcon() {
        ImageView locationIcon = (ImageView) findViewById(R.id.location_icon);
        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
    }

    private void checkLocationPermissionAndGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            checkGPSEnabledAndProceed();
        }
    }

    private void requestLocationPermission() {
        Log.e("request: ", "location request");
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                Log.e("Location", "ACCESS_FINE_LOCATION granted");
                checkGPSEnabledAndProceed();
            } else {
                Log.e("Location", "Location permission denied");
                // User selected "Deny"
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showLocationPermissionDialog();
                } else {
                    // User selected "Don't ask again" or close app
                    // App don't run here
                    new AlertDialog.Builder(this)
                            .setMessage("Location permission is permanently denied. Please go to app settings to enable it.")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                Toast.makeText(this, "Permission is necessary for the app to work", Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                }
            }
        }
    }

    private void showLocationPermissionDialog() {
        ConstraintLayout locationPermissionLayout = findViewById(R.id.location_permission_layout);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.location_permission_dialog, locationPermissionLayout);
        Button grantPermissionBtn = view.findViewById(R.id.grant_permission_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        grantPermissionBtn.findViewById(R.id.grant_permission_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                requestLocationPermission();
            }
        });

        cancelBtn.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, "Permission is necessary for the app to work", Toast.LENGTH_SHORT).show();
                finishAffinity(); // Closes all activities and exits the app
                System.exit(0);
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void checkGPSEnabledAndProceed() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            promptEnableGPS();
        } else {
            proceedWithLocationEnabled();
        }
    }
    private void promptEnableGPS() {
        ConstraintLayout gpsServiceLayout = findViewById(R.id.gps_service_layout);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.gps_service_dialog, gpsServiceLayout);
        Button openSettingsBtn = view.findViewById(R.id.open_settings_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        enableGPSDialog = builder.create();

        openSettingsBtn.findViewById(R.id.open_settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 101);
            }
        });

        cancelBtn.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableGPSDialog.dismiss();
                Toast.makeText(MainActivity.this, "GPS service not enabled", Toast.LENGTH_SHORT).show();
                finishAffinity(); // Closes all activities and exits the app
                System.exit(0);
            }
        });

        if (enableGPSDialog.getWindow() != null) {
            enableGPSDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        enableGPSDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this, "GPS is now enabled", Toast.LENGTH_SHORT).show();
                Log.e("Location", "GPS enabled");
                proceedWithLocationEnabled();
            } else {
                Toast.makeText(this, "GPS is still disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void proceedWithLocationEnabled() {
        Log.e("Location", "Proceeding with location services enabled");
        if (enableGPSDialog != null) enableGPSDialog.dismiss();
        getCurrentLocation();
    }

    @NonNull
    private String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }

    @NonNull
    private String convertTimestamp(long time) {
            // Convert to milliseconds
            Date date = new Date(time * 1000);

            // 12-hour clock with AM/PM
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

            String formattedTime = formatter.format(date);
            return formattedTime;
    }


    private void onClickBottomAppBar() {
        ImageView search_appBar_icon = findViewById(R.id.search_appBar_icon);
        ImageView favor_appBar_icon = findViewById(R.id.favor_appBar_icon);
        ImageView account_appBar_icon = findViewById(R.id.account_appBar_icon);

        search_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
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
                Intent intent = new Intent(MainActivity.this, AssistantActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    private void goNext7Day() {
        TextView next7Day = findViewById(R.id.next7Day);
        next7Day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

    @NonNull
    private String removeDiacritics(String string) {
        String normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
        String withoutDiacritics = normalized.replaceAll("\\p{M}", "");
        // Replace special Vietnamese letters
        return withoutDiacritics.replace("Đ", "D").replace("đ", "d");
    }

    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndGPS();
                return;
            }

            Log.e("GetCurrentLocation: ", "OK, fine :)");
            // Fallback for last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                Log.e("Location: ", "GetLastKnownLocation");
                onLocationChanged(lastKnownLocation);
            } else {
                // Request location update
                Log.e("GetCurrentLocation", "Requesting location...");
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        retryCount = 0;
        Toast.makeText(this, "Your current location", Toast.LENGTH_SHORT).show();
        try {
            // Get cityName from the latitude and longitude
            Log.e("Location: ", "Location received: " + String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            initRvHourly();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Deprecated
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("Location: ", "Provider status changed: " + provider + " Status: " + status);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.e("Location: ", "Provider enabled: " + provider);
        if (enableGPSDialog != null) enableGPSDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e("Location: ", "Provider disabled: " + provider);
        checkLocationPermissionAndGPS();
//        retryRequest();
    }

    private void retryRequest() {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++;
            Log.d("GetCurrentLocation", "Retrying location request. Attempt: " + retryCount);

            // Retry after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> getCurrentLocation(), 2000);
        } else {
            Log.d("GetCurrentLocation", "Max retry attempts reached. Giving up.");
            Toast.makeText(this, "Failed to obtain location. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWeatherDataFetched(@NonNull WeatherData weatherData) {
        synchronized (this) {
            currentLocation = weatherData.getCurrentLocation();
            currentTemperature = weatherData.getCurrentTemperature();
            weatherCondition = weatherData.getWeatherCondition();
            isNight = weatherData.isNight();
        }
    }
}
