package com.example.barapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etRate;
    private Button btnSave, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        etRate = findViewById(R.id.et_rate);
        btnSave = findViewById(R.id.btn_save_settings);
        btnHome = findViewById(R.id.btn_home);

        loadSettings();

        btnSave.setOnClickListener(v -> saveSettings());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadSettings() {
        String val = dbHelper.getSetting(DatabaseHelper.SETTING_RATE_PER_PERSON_HOUR);
        if (val != null) {
            etRate.setText(val);
        } else {
            etRate.setText("400.0");
        }
    }

    private void saveSettings() {
        String newVal = etRate.getText().toString();
        if (!newVal.isEmpty()) {
            dbHelper.setSetting(DatabaseHelper.SETTING_RATE_PER_PERSON_HOUR, newVal);
            Toast.makeText(this, "Тариф сохранен!", Toast.LENGTH_SHORT).show();
            // Don't finish, let them go home or stay
        }
    }
}
