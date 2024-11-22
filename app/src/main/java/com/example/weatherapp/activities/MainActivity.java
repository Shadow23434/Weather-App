package com.example.weatherapp.activities;

import static java.lang.Math.round;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.daily.Daily;
import com.example.weatherapp.hourly.HourlyAdapter;
import com.example.weatherapp.hourly.Hourly;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
<<<<<<< HEAD
=======
import org.w3c.dom.Text;
>>>>>>> cebf38352e86bea8f75435cdc5a51ce844dc777d

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView rvHourly;
    LocationManager locationManager; // request location
    private AlertDialog enableGPSDialog; // prompt user to enable GPS service
    private AlertDialog openSettingDialog; // prompt user to open Setting for location service
    private int retryCount = 0;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        // Go back from forecast
        String cityForecast = getIntent().getStringExtra("city");
        // Enter editText
        String citySearch = formatCityName(getIntent().getStringExtra("editTextSearch"));

        if (cityForecast != null) {
            city = cityForecast;
            fetchDailyWeatherData();
            initRvHourly();
        }
        else if (citySearch != null) {
            Log.e("Forcast Intent: ", "City is null");
            city = citySearch;
            fetchDailyWeatherData();
            initRvHourly();
        }
        else {
            Log.e("Search Intent: ", "City is null");
            getCurrentLocation();
        }

        onClickLocationIcon();
        onClickBottomAppBar();
        onClickFloatingButton();
        goNext7Day();
    }

    private void fetchDailyWeatherData() {
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
                    JSONObject currentDayData = jsonObject.getJSONArray("data").getJSONObject(0);

                    String cityName = jsonObject.getString("city_name");
                    String countryName = getCountryName(jsonObject.getString("country_code"));
                    Double d_max_temp = currentDayData.getDouble("high_temp");
                    Double d_min_temp = currentDayData.getDouble("low_temp");
                    int max_temp = (int) round(d_max_temp);
                    int min_temp = (int) round(d_min_temp);

                    TextView tvCity = (TextView) findViewById(R.id.city);
                    TextView tvCountry = (TextView) findViewById(R.id.country);
                    TextView maxTemp = (TextView) findViewById(R.id.max_temp);
                    TextView minTemp = (TextView) findViewById(R.id.min_temp);

                    runOnUiThread(() -> {
                        tvCity.setText(cityName + ", ");
                        tvCountry.setText(countryName);
                        maxTemp.setText(max_temp + "°/");
                        minTemp.setText(min_temp + "°");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching API: ", e.getMessage());
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
                // User selected "Denined"
                // App don't run here
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    new AlertDialog.Builder(this)
                            .setMessage("This app needs location access to function properly. Please grant the permission.")
                            .setPositiveButton("Grant Permission", (dialog, which) -> requestLocationPermission())
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                Toast.makeText(this, "Permission is necessary for the app to work", Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                } else {
                    // User selected "Don't ask again"
                    // App run here
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

    private void checkGPSEnabledAndProceed() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            promptEnableGPS();
        } else {
            proceedWithLocationEnabled();
        }
    }

    private void promptEnableGPS() {
        enableGPSDialog = new AlertDialog.Builder(this)
                .setMessage("Location services are disabled. Please enable them for the app to function correctly.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 101);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "GPS service not enabled", Toast.LENGTH_SHORT).show();
                })
                .create();
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

    private void initRvHourly() {
        ArrayList<Hourly> items = new ArrayList<>();

        // Fetching api here
        if (city != "" && city != null) fetchHourlyWeatherData(items);
        else {
            Log.e("City: ", "city not found");
            Toast.makeText(this, "Please enter the right name and try again.", Toast.LENGTH_SHORT).show();
        }

        rvHourly = findViewById(R.id.rv_hourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapterHourly = new HourlyAdapter(items);
        rvHourly.setAdapter(adapterHourly);
    }

    private void fetchHourlyWeatherData(ArrayList<Hourly> items) {
        // Hourly weather data
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
                        Double d_temperature = hourlyData.getDouble("temp");
                        int temperature = (int) Math.round(d_temperature);
                        String icon = hourlyData.getJSONObject("weather").getString("icon");
                        String description = hourlyData.getJSONObject("weather").getString("description");

                        // Current hour
                        if (i == 0) {
                            int resId = getResources().getIdentifier(icon, "drawable", getPackageName());
                            ImageView weatherIcon = (ImageView) findViewById(R.id.weather_icon);
                            TextView temp = (TextView) findViewById(R.id.temp);
                            TextView weatherDescription = (TextView) findViewById(R.id.weather_description);

                            runOnUiThread( () -> {
                                Glide.with(this).load(resId).into(weatherIcon);
                                temp.setText(temperature + "℃");
                                weatherDescription.setText(description);
                            });
                        }

                        // Next 11-hour
                        if (i>0) items.add(new Hourly(time, temperature, icon));
                    }
                    runOnUiThread(() -> adapterHourly.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetching API: ", e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @NonNull
    private String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
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
                Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                intent.putExtra("city", city);
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

    private String formatCityName(String address) {
        if (address != null) {
            address = removeDiacritics(address);
            String[] arrayCity = address.trim().split("\\s");
            String cityName = "";
            for (String word : arrayCity) cityName += word;
            Log.e("Current city: ", cityName);
            return cityName;
        }
        return null;
    }

    public void getCurrentLocation() {
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
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAdminArea();
            Log.e("address: ", "address = " + address);
            city = formatCityName(address);

            fetchDailyWeatherData();
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
//        if (openSettingDialog != null) openSettingDialog.dismiss();
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
}
