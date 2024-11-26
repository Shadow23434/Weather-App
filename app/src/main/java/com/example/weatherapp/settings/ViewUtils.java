package com.example.weatherapp.settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewUtils {

    public static void setTextColor(ViewGroup root, int color) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                setTextColor((ViewGroup) child, color); // Đệ quy cho ViewGroup
            }
        }
    }
}
