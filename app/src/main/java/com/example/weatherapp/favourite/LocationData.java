package com.example.weatherapp.database;

public class LocationData {
    private String latitude;
    private String longitude;
    private String timestamp;
    private String cityName;
    private int temperature;
    private String countryName;
    private String description;

    public LocationData() {
    }

    public LocationData(String cityName,  String countryName ,String description, int temperature, String latitude, String longitude, String timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.cityName = cityName;
        this.temperature = temperature;
        this.countryName = countryName;
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCityName() {
        return cityName;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getDescription() {
        return description;
    }
}

