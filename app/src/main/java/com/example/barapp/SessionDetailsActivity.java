package com.example.barapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SessionDetailsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int sessionId;
    private SessionModel session;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private TextView tvTitle, tvStartTime, tvPeople, tvDuration, tvTimeCost, tvBarList, tvBarTotal, tvGrandTotal;
    private Button btnCloseSession, btnHome;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private BarSelectorAdapter adapter;
    private List<BarItemModel> allBarItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details); // Themes applied

        sessionId = getIntent().getIntExtra("SESSION_ID", -1);
        if (sessionId == -1) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        tvTitle = findViewById(R.id.tv_session_title);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvPeople = findViewById(R.id.tv_people_count);
        tvDuration = findViewById(R.id.tv_duration);
        tvTimeCost = findViewById(R.id.tv_time_cost);
        tvBarList = findViewById(R.id.tv_bar_items_list);
        tvBarTotal = findViewById(R.id.tv_bar_total);
        tvGrandTotal = findViewById(R.id.tv_grand_total);
        btnCloseSession = findViewById(R.id.btn_close_session);
        btnHome = findViewById(R.id.btn_home);
        tabLayout = findViewById(R.id.tab_layout_categories);
        recyclerView = findViewById(R.id.recycler_bar_items);

        // Red Styling & Animation
        tvGrandTotal.setTextColor(ContextCompat.getColor(this, R.color.neon_green)); 
        tvGrandTotal.setShadowLayer(10, 0, 0, Color.GREEN); // Glow

        startPulseAnimation(btnCloseSession);

        setupTabs();
        setupRecyclerView();

        loadSessionData();
        
        btnCloseSession.setOnClickListener(v -> closeSession());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeCalculations();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    private void startPulseAnimation(Button btn) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(btn, "alpha", 1f, 0.7f, 1f);
        pulse.setDuration(1500);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }

    private void loadSessionData() {
        session = dbHelper.getSession(sessionId);
        if (session == null) {
            finish();
            return;
        }

        tvTitle.setText("Зал " + session.getHallId());
        tvPeople.setText("Гостей: " + session.getPeopleCount());

        try {
            JSONArray items = new JSONArray(session.getBarItemsJson());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.length(); i++) {
                JSONObject obj = items.getJSONObject(i);
                sb.append(obj.getString("name"))
                        .append(" x").append(obj.getInt("quantity"))
                        .append(" = ").append(obj.getDouble("price") * obj.getInt("quantity")).append(" c\n");
            }
            if (sb.length() == 0)
                sb.append("Нет товаров");
            tvBarList.setText(sb.toString());
        } catch (Exception e) {
            tvBarList.setText("Ошибка данных");
        }

        tvBarTotal.setText(String.format("%.1f c", session.getTotalAmount()));
        updateTimeCalculations();
    }

    private void updateTimeCalculations() {
        if (session == null || "CLOSED".equals(session.getStatus()))
            return;

        long now = System.currentTimeMillis();
        long durationMillis = now - session.getStartTime();

        long hours = durationMillis / 3600000;
        long minutes = (durationMillis % 3600000) / 60000;

        tvDuration.setText(String.format("Время: %02d:%02d", hours, minutes));

        String rateStr = dbHelper.getSetting(DatabaseHelper.SETTING_RATE_PER_PERSON_HOUR);
        double rate = (rateStr != null) ? Double.parseDouble(rateStr) : 400.0;

        double timeCost = (durationMillis / 3600000.0) * rate * session.getPeopleCount();
        tvTimeCost.setText(String.format("По времени: %.1f c", timeCost));

        double grandTotal = session.getTotalAmount() + timeCost;
        tvGrandTotal.setText(String.format("%.1f c", grandTotal));
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("ВСЕ"));
        tabLayout.addTab(tabLayout.newTab().setText("ПИВО"));
        tabLayout.addTab(tabLayout.newTab().setText("НАПИТКИ"));
        tabLayout.addTab(tabLayout.newTab().setText("КАЛЬЯН"));
        tabLayout.addTab(tabLayout.newTab().setText("КУХНЯ"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (adapter != null) {
                    adapter.filter(tab.getText().toString());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allBarItems = dbHelper.getAllBarItems();
        
        adapter = new BarSelectorAdapter(allBarItems, item -> {
             if (item.getCurrentStock() > 0) {
                dbHelper.addBarItemToSession(sessionId, item.getId(), 1);
                loadSessionData();
                Toast.makeText(this, "Добавлено: " + item.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нет в наличии!", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void closeSession() {
        new AlertDialog.Builder(this)
                .setTitle("Закрыть сессию?")
                .setMessage("Подтвердите оплату и закрытие счета.")
                .setPositiveButton("ЗАКРЫТЬ", (dialog, which) -> {
                    // Animation Validation
                    tvGrandTotal.animate().scaleX(1.3f).scaleY(1.3f).setDuration(300).withEndAction(() -> {
                         tvGrandTotal.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200);
                         dbHelper.closeSession(sessionId);
                         Toast.makeText(this, "Сессия закрыта", Toast.LENGTH_SHORT).show();
                         finish();
                         overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }).start();

                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
