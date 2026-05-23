package com.example.kabarin.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kabarin.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Handler untuk delay 2 detik
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cek session login dari SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("KabarinPrefs", MODE_PRIVATE);
                boolean isLogin = sharedPreferences.getBoolean("isLogin", false);

                Intent intent;
                if (isLogin) {
                    // Jika sudah login, lanjut ke MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // Jika belum login, ke WelcomeActivity
                    intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                }
                
                startActivity(intent);
                finish(); // Agar user tidak bisa kembali ke Splash
            }
        }, 2000); // 2000 milidetik = 2 detik
    }
}
