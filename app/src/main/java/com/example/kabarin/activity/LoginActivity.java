package com.example.kabarin.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kabarin.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi View menggunakan findViewById
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Logika Tombol Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Ambil input dari EditText
                String inputEmailOrUser = etEmail.getText().toString().trim();
                String inputPassword = etPassword.getText().toString().trim();

                // 2. Ambil data akun yang tersimpan dari SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("KabarinPrefs", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("savedUsername", "");
                String savedEmail = sharedPreferences.getString("savedEmail", "");
                String savedPassword = sharedPreferences.getString("savedPassword", "");

                // Validasi field tidak kosong
                if (inputEmailOrUser.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username/Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Bandingkan input dengan data tersimpan
                // Login berhasil jika: (email ATAU username cocok) DAN password cocok
                boolean isUserMatch = inputEmailOrUser.equals(savedUsername) || inputEmailOrUser.equals(savedEmail);
                boolean isPasswordMatch = inputPassword.equals(savedPassword);

                if (isUserMatch && isPasswordMatch) {
                    // 4. Jika Benar: Simpan status login & navigasi
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // 5. Jika Salah: Tampilkan Toast error
                    Toast.makeText(LoginActivity.this, "Account not found or password incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigasi ke halaman Register
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
