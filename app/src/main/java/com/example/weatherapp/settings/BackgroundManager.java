package com.example.weatherapp.settings;

public class BackgroundManager {
    private static BackgroundManager instance;
    private boolean isBackgroundChanged;

    private BackgroundManager() {
        // Khởi tạo trạng thái ban đầu
        isBackgroundChanged = false;
    }

    public static BackgroundManager getInstance() {
        if (instance == null) {
            instance = new BackgroundManager();
        }
        return instance;
    }

    public boolean isBackgroundChanged() {
        return isBackgroundChanged;
    }

    public void setBackgroundChanged(boolean backgroundChanged) {
        isBackgroundChanged = backgroundChanged;
    }
}
