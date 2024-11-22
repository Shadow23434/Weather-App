package com.example.weatherapp.daily;

public class Daily {
    private String day;
    private String icon;
    private String description;
    private int maxTemp;
    private int minTemp;

    public Daily(String day, String icon, String description, int maxTemp, int minTemp) {
        this.day = day;
        this.icon = icon;
        this.description = description;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }
}
