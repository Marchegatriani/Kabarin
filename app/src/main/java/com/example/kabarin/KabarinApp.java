package com.example.kabarin;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class KabarinApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        applySavedTheme();
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        
        // Menggunakan "FOLLOW_SYSTEM" jika user belum pernah mengatur tema secara manual
        // Agar konsisten dengan pengaturan HP user di awal.
        if (!prefs.contains("isDarkMode")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}
