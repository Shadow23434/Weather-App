package com.example.weatherapp.chat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.weatherapp.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

public class Chat {
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta";

    // Access your API key as a Build Configuration variable
    private final String apiKey = BuildConfig.apiKey;

    // ExecutorService for background task
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Handler to post results to the main thread
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void fetchChatData() {
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    handler.post(() -> handleResponse(responseBody));
                } else {
                    Log.e("GeminiAPI", "Request failed: " + response);
                }
            } catch (IOException e) {
                Log.e("GeminiAPI", "Error: " + e.getMessage());
            }
        });
    }

    private void handleResponse(String response) {
        if (response != null) {
            // Parse the JSON response using Gson or another JSON parsing library
            try {
                JSONObject jsonObject = new JSONObject(response);
                // Extract data from the JSON object
                String data = jsonObject.getString("data");
                Log.d("GeminiAPI", "Data: " + data);
            } catch (JSONException e) {
                Log.e("GeminiAPI", "JSON Parsing error: " + e.getMessage());
            }
        }
    }
}
