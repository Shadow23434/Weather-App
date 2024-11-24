package com.example.weatherapp.daily;


public class WeatherData {
    private String currentLocation;
    private String currentTemperature;
    private String weatherCondition;
    private boolean isNight;

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public boolean isNight() {
        return isNight;
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }
}