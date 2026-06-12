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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmailOrUser = etEmail.getText().toString().trim();
                String inputPassword = etPassword.getText().toString().trim();

                SharedPreferences sharedPreferences = getSharedPreferences("KabarinPrefs", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("savedUsername", "");
                String savedEmail = sharedPreferences.getString("savedEmail", "");
                String savedPassword = sharedPreferences.getString("savedPassword", "");

                if (inputEmailOrUser.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username/Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isUserMatch = inputEmailOrUser.equals(savedUsername) || inputEmailOrUser.equals(savedEmail);
                boolean isPasswordMatch = inputPassword.equals(savedPassword);

                if (isUserMatch && isPasswordMatch) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Account not found or password incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
