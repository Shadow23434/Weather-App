package com.example.weatherapp.chat;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Chat extends AsyncTask<Void, Void, String> {
    private String apiUrl = "https://your-gemini-api-endpoint";
    private String apiKey = "your-api-key";

    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e("GeminiAPI", "Request failed: " + response);
            }
        } catch (IOException e) {
            Log.e("GeminiAPI", "Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (response != null) {
            // Parse the JSON response using Gson or another JSON parsing library
            Gson gson = new Gson();
            // Assuming the response is a JSON object
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(response);
                // Extract data from the JSON object
                String data = jsonObject.getString("data");
                Log.d("GeminiAPI", "Data: " + data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
