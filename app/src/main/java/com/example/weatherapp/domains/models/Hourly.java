package com.example.weatherapp.domains.models;

public class Hourly {
    private String hour;
    private int temp;
    private String picPath;

    public Hourly(String hour, int temp, String picPath) {
        this.hour = hour;
        this.temp = temp;
        this.picPath = picPath;
    }

    public String getHour() {
        return hour;
    }

    public int getTemp() {
        return temp;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
