package com.example.weatherapp.domains;

import com.example.weatherapp.domains.WeatherData;

public interface WeatherDataCallback {
    void onWeatherDataFetched(WeatherData weatherData);
}
