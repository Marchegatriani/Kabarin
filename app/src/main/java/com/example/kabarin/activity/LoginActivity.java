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

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Login Button Logic
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Get input from EditText
                String inputEmailOrUser = etEmail.getText().toString().trim();
                String inputPassword = etPassword.getText().toString().trim();

                // 2. Get saved account data from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("KabarinPrefs", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("savedUsername", "");
                String savedEmail = sharedPreferences.getString("savedEmail", "");
                String savedPassword = sharedPreferences.getString("savedPassword", "");

                // Validate non-empty fields
                if (inputEmailOrUser.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username/Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Compare input with saved data
                // Login successful if: (email OR username match) AND password match
                boolean isUserMatch = inputEmailOrUser.equals(savedUsername) || inputEmailOrUser.equals(savedEmail);
                boolean isPasswordMatch = inputPassword.equals(savedPassword);

                if (isUserMatch && isPasswordMatch) {
                    // 4. If Correct: Save login status & navigate
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // 5. If Wrong: Show error Toast
                    Toast.makeText(LoginActivity.this, "Account not found or password incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigate to Register page
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
