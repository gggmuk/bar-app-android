package com.example.barapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        prefs = getSharedPreferences("BarAppPrefs", MODE_PRIVATE);
        if (prefs.contains("user_id")) {
            // User already logged in, go to main activity
            navigateToMain();
            return;
        }
        
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        UserModel user = dbHelper.authenticateUser(username, password);
        
        if (user != null) {
            // Save user session
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", user.getId());
            editor.putString("username", user.getUsername());
            editor.putString("role", user.getRole());
            editor.putString("full_name", user.getFullName());
            editor.apply();

            Toast.makeText(this, "Добро пожаловать, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
