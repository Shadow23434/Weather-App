package com.example.weatherapp.locationSuggestion;

public class Suggestion {
    private String cityName;
    private String displayName;
    private String cityLatitude;
    private String cityLongitude;
    private String countryCode;

    public Suggestion(String cityName, String displayName, String cityLatitude, String cityLongitude, String countryCode) {
        this.cityName = cityName;
        this.displayName = displayName;
        this.cityLatitude = cityLatitude;
        this.cityLongitude = cityLongitude;
        this.countryCode = countryCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityLatitude() {
        return cityLatitude;
    }

    public void setCityLatitude(String cityLatitude) {
        this.cityLatitude = cityLatitude;
    }

    public String getCityLongitude() {
        return cityLongitude;
    }

    public void setCityLongitude(String cityLongitude) {
        this.cityLongitude = cityLongitude;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
