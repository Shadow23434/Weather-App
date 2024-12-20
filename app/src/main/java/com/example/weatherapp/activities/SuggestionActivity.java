package com.example.weatherapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.domains.RvLocationSuggestionInterface;
import com.example.weatherapp.domains.models.Suggestion;
import com.example.weatherapp.adapters.SuggestionAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SuggestionActivity extends AppCompatActivity implements RvLocationSuggestionInterface {
    private SearchView searchView;
    private RecyclerView rvLocationSuggestion;
    private RecyclerView.Adapter locationSuggestAdapter;
    private ArrayList<Suggestion> suggestionArrayList;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        searchView = findViewById(R.id.search_bar);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.BLACK);

        onChangeSearchView();
        onClickBackIcon();
    }

    private void onChangeSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                latitude = suggestionArrayList.get(0).getCityLatitude();
                longitude = suggestionArrayList.get(0).getCityLongitude();

                if (latitude != null && !latitude.isEmpty() &&
                    longitude != null && !longitude.isEmpty()) {
                    goSearch();
                }
                else {
                    Log.e("onQueryTextSubmit: ", "latitude is null");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    initRvLocationSuggestion(newText);
                }
                else Log.e("OnQueryTextChange: ", "newText is null");
                return false;
            }
        });
    }

    private void goSearch() {

        Intent intent = new Intent(SuggestionActivity.this, SearchActivity.class);
        intent.putExtra("sLatitude", latitude);
        intent.putExtra("sLongitude", longitude);
        startActivity(intent);
    }

    private void initRvLocationSuggestion(String newText) {
        suggestionArrayList = new ArrayList<>();
        rvLocationSuggestion = findViewById(R.id.rv_location_suggestion);
        rvLocationSuggestion.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        locationSuggestAdapter = new SuggestionAdapter(suggestionArrayList, this);
        rvLocationSuggestion.setAdapter(locationSuggestAdapter);

        fetchLocationData(suggestionArrayList, newText);
    }

    private void fetchLocationData(ArrayList<Suggestion> suggestionArrayList, String newText) {
        String url = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=10&appid=%s", newText, BuildConfig.location_api);
        Log.e("Fetch location data: ", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Fetch location suggestion data: ", "Success");
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    for (int i=0; i<jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        String cityName = data.getString("name");
                        String country_code = data.getString("country");
                        country_code = country_code.toLowerCase();
                        Log.e("Fetch location data: ", "country = " + country_code);
                        String displayName = cityName + ", " + ((data.has("state")) ? data.getString("state") : "") + ", " + getCountryName(country_code);
                        String cityLatitude = String.valueOf(data.getDouble("lat"));
                        String cityLongitude = String.valueOf(data.getDouble("lon"));

                        suggestionArrayList.add(new Suggestion(cityName, displayName, cityLatitude, cityLongitude, country_code));
                    }
                    runOnUiThread(() -> {
                        locationSuggestAdapter.notifyDataSetChanged();
                    });
                }
                else {
                    Log.e("Fetch location data: ", response.body().string());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Fetch location data: ", "Fetch Error");
            }
        }).start();
    }

    private void onClickBackIcon() {
        ImageView back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout = findViewById(R.id.history_location_layout);
                linearLayout.setVisibility(View.VISIBLE);
                startActivity(new Intent(SuggestionActivity.this, SearchActivity.class));
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        latitude = suggestionArrayList.get(position).getCityLatitude();
        longitude = suggestionArrayList.get(position).getCityLongitude();

        if (latitude != null && !latitude.isEmpty() &&
                longitude != null && !longitude.isEmpty()) {
            goSearch();
        }
    }

    @NonNull
    private String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }
}
