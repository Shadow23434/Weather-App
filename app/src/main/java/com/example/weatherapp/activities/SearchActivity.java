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
import com.example.weatherapp.favourite.LocationData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    private String latitude;
    private String longitude;
    private FirebaseUser user;
    private FirebaseAuth fAuth;
    private DatabaseReference databaseReference;
    private boolean favOn = false;
    private List<LocationData> locationDataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        if (latitude != null && !latitude.isEmpty() &&
                longitude != null && !longitude.isEmpty()) {
            Log.e("Suggestion Intent: ", "Latitude = " + latitude + ", " + "Longitude = " + longitude);
            fetchHourlyWeatherData();
        } else Log.e("Suggestion Intent: ", "latitude is null");

        if(user != null){
            databaseReference = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("history");
            loadHistoryLocation();
            checkLocationInFav();
        } else {
            Log.e("User", "user not log in");
        }

        onClickCurrentLocationLayout();
        onSearch();
        onClickBottomAppBar();
        onClickFloatingButton();
    }

    private void checkLocationInFav() {
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("favourite");
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
                    if (locationData != null && locationData.getLatitude().equals(latitude) && locationData.getLongitude().equals(longitude)) {
                        favOn = true;
                        ImageView favIc = findViewById(R.id.ic_fav);
                        favIc.setImageResource(R.drawable.ic_fav_fill);
                        break;
                    } else {
                        favOn = false;
                        ImageView favIc = findViewById(R.id.ic_fav);
                        favIc.setImageResource(R.drawable.ic_fav_border);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to check favourite status", error.toException());
            }
        });
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
                    saveLocationToHistory(city, country, description, temperature, latitude, longitude, icon);
                    locationDataList.add(new LocationData(null, city, country, description, temperature, latitude, longitude, icon, getCurrentTime()));

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
                        onClickFavIcon();
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

//  History Search

    private void saveLocationToHistory(String city, String country, String description, int temperature, String latitude, String longitude, String icon){
        LocationData locationData = new LocationData(null, city, country, description, temperature, latitude, longitude,icon, getCurrentTime());
        databaseReference.setValue(locationData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Firebase" , "Location saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firebase", "Failed save history");
            }
        });
    }

    @NonNull
    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void loadHistoryLocation(){
        LinearLayout historyLayout = findViewById(R.id.history_location_layout);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    LocationData locationData = snapshot.getValue(LocationData.class);
                    if(locationData != null){
                        updateLayout(locationData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load location history");
            }
        });
    }

    private void updateLayout(LocationData locationData) {
        LinearLayout historyLayout = findViewById(R.id.history_location_layout);
        TextView country = findViewById(R.id.history_country_name);
        TextView city = findViewById(R.id.history_city_name);
        TextView description = findViewById(R.id.history_weather_description);
        TextView temp = findViewById(R.id.history_temp);
        ImageView iconImage = findViewById(R.id.history_weather_icon);

        runOnUiThread( () -> {
            country.setText(locationData.getCountryName());
            city.setText(locationData.getCityName());
            description.setText(locationData.getDescription());
            temp.setText(String.format("%d°C", locationData.getTemperature()));
            int resId = getResources().getIdentifier(locationData.getIcon(), "drawable", getPackageName());
            Glide.with(this).load(resId).into(iconImage);
            historyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                    intent.putExtra("latitude", locationData.getLatitude());
                    intent.putExtra("longitude", locationData.getLongitude());
                    startActivity(intent);
                }
            });
        });
    }

//  Favourite Location
    private void onClickFavIcon() {
        ImageView favIc = findViewById(R.id.ic_fav);
        favIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favOn = !favOn;
                if(favOn) {
                    favIc.setImageResource(R.drawable.ic_fav_fill);
                    if(locationDataList.size()-1 >= 0) {
                        saveLocationToFav(locationDataList.get(locationDataList.size() - 1));
                        Log.d("locationDataList", "size" + locationDataList.size());
                    }
                    else
                        Log.e("locationDataList", "is empty");
                }
                else {
                    favIc.setImageResource(R.drawable.ic_fav_border);
                    removeLocationFromFav(latitude, longitude);
                }
            }
        });
    }

    private void removeLocationFromFav(String latitude, String longitude) {
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("favourite");
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
                    if (locationData != null && locationData.getLatitude().equals(latitude) && locationData.getLongitude().equals(longitude)) {
                        dataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Location removed from favourites");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Firebase", "Failed to remove location from favourites", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to remove location from favourites", error.toException());
            }
        });
    }

    private void saveLocationToFav(@NonNull LocationData locationData) {
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("favourite");
        String key = favRef.push().getKey();
        locationData.setKey(key);
        favRef.child(locationData.getKey()).setValue(locationData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Firebase", "Saved to fav");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firebase", "Failed save to fav");
            }
        });
    }
}
