package com.example.barapp;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private DatabaseHelper dbHelper;
    private TextView tvStatus1, tvStatus2, tvStatus3, tvDetails1, tvDetails2, tvDetails3;
    private MaterialCardView cardHall1, cardHall2, cardHall3;
    private AlertDialog quickDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure values/themes.xml is applied in Manifest

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Role-based access control
        android.content.SharedPreferences prefs = getSharedPreferences("BarAppPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("role", "");
        
        // Hide menu items for ADMIN (only MANAGER can see Reports and Settings)
        if ("ADMIN".equals(userRole)) {
            navigationView.getMenu().findItem(R.id.nav_reports).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_settings).setVisible(false);
        }

        tvStatus1 = findViewById(R.id.tv_status_1);
        tvDetails1 = findViewById(R.id.tv_details_1);
        cardHall1 = findViewById(R.id.card_hall_1);

        tvStatus2 = findViewById(R.id.tv_status_2);
        tvDetails2 = findViewById(R.id.tv_details_2);
        cardHall2 = findViewById(R.id.card_hall_2);

        tvStatus3 = findViewById(R.id.tv_status_3);
        tvDetails3 = findViewById(R.id.tv_details_3);
        cardHall3 = findViewById(R.id.card_hall_3);

        setupHallButton(cardHall1, 1);
        setupHallButton(cardHall2, 2);
        setupHallButton(cardHall3, 3);
    }

    private void setupHallButton(MaterialCardView card, int hallId) {
        // Simple scale animation on touch
        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false; // let click listener handle logic
        });

        card.setOnClickListener(v -> handleHallClick(hallId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHallStatus();
    }

    private void refreshHallStatus() {
        updateHallUI(1, tvStatus1, tvDetails1, cardHall1);
        updateHallUI(2, tvStatus2, tvDetails2, cardHall2);
        updateHallUI(3, tvStatus3, tvDetails3, cardHall3);
    }

    private void updateHallUI(int hallId, TextView statusView, TextView detailsView, MaterialCardView cardView) {
        SessionModel activeSession = getActiveSession(hallId);
        if (activeSession != null) {
            statusView.setText("ЗАНЯТО");
            statusView.setTextColor(ContextCompat.getColor(this, R.color.red_accent));

            long durationMins = (System.currentTimeMillis() - activeSession.getStartTime()) / (1000 * 60);
            detailsView.setText("Людей: " + activeSession.getPeopleCount() + "\nВремя: " + durationMins + " мин");
            detailsView.setTextColor(Color.LTGRAY);
            
            // Occupied Visuals: Green Border
            cardView.setStrokeColor(ContextCompat.getColor(this, R.color.neon_green));
            cardView.setStrokeWidth(6); // 2dp approx

            // Pulse Animation when occupied (only once or loop?)
            // Let's do a single attention grabber on load
            if (cardView.getScaleX() == 1f) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.02f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.02f, 1f);
                scaleX.setDuration(500);
                scaleY.setDuration(500);
                scaleX.start();
                scaleY.start();
            }

        } else {
            statusView.setText("СВОБОДНО");
            statusView.setTextColor(ContextCompat.getColor(this, R.color.neon_green));
            detailsView.setText("Нажмите, чтобы открыть");
            
            // Free Visuals: No Border
            cardView.setStrokeWidth(0);
        }
    }

    private SessionModel getActiveSession(int hallId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SESSIONS, null,
                DatabaseHelper.KEY_SESSION_HALL_ID + "=? AND " + DatabaseHelper.KEY_SESSION_STATUS + "=?",
                new String[] { String.valueOf(hallId), "OPEN" }, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            SessionModel session = new SessionModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_HALL_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_PEOPLE_COUNT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_START_TIME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_END_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_BAR_ITEMS_JSON)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_TOTAL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SESSION_IS_SYNCED)) == 1);
            cursor.close();
            return session;
        }
        return null;
    }

    private void handleHallClick(int hallId) {
        SessionModel activeSession = getActiveSession(hallId);
        if (activeSession != null) {
            Intent intent = new Intent(this, SessionDetailsActivity.class);
            intent.putExtra("SESSION_ID", activeSession.getId());
            startActivity(intent);
            // Activity Transition
            // Activity Transition
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            showOpenSessionDialog(hallId);
        }
    }

    private void showOpenSessionDialog(int hallId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_NoActionBar);
        // Using System dark dialog or explicit style?
        // To ensure Red/Black, cleaner is to use default builder and rely on AppTheme,
        // provided the dialog buttons are styled or we use custom view entirely.

        builder.setTitle("Открытие: Зал " + hallId);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));

        View.OnClickListener listener = v -> {
            // Button Click Animation
            v.animate().scaleX(1.1f).scaleY(1.1f).setInterpolator(new OvershootInterpolator()).setDuration(150)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        int count = (int) v.getTag();
                        startSession(hallId, count);
                    }).start();
        };

        layout.addView(createRow(listener, 1, 2));
        layout.addView(createRow(listener, 4, 6));
        layout.addView(createRow(listener, 8, 10));

        builder.setView(layout);
        // Negative button usually has text color from theme (accent -> red)
        builder.setNegativeButton("Отмена", null);

        quickDialog = builder.create();
        quickDialog.show();

        // Style Title (Optional hack if standard theme isn't applying well enough)
        // int titleId = getResources().getIdentifier("alertTitle", "id", "android");
        // TextView title = quickDialog.findViewById(titleId);
        // if (title != null) title.setTextColor(Color.WHITE);
    }

    private void startSession(int hallId, int count) {
        if (quickDialog != null && quickDialog.isShowing()) {
            quickDialog.dismiss();
        }
        dbHelper.openNewSession(hallId, count);
        Toast.makeText(this, "Сессия открыта", Toast.LENGTH_SHORT).show();

        // Simulating Notification (just toast for now, actual system notification
        // requires Permission & NotificationManager)
        sendOrderNotification(hallId, count);

        SessionModel newSession = getActiveSession(hallId);
        if (newSession != null) {
            Intent intent = new Intent(this, SessionDetailsActivity.class);
            intent.putExtra("SESSION_ID", newSession.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            refreshHallStatus();
        }
    }

    private void sendOrderNotification(int hallId, int count) {
        // Placeholder for System Notification
        // In real app: create NotificationChannel, build Notification, notify().
        // For now, simple logic as requested by prompt "Сгенерируй код...".
        // I will implement a basic stub method structure.

        /*
         * NotificationManager nm = (NotificationManager)
         * getSystemService(Context.NOTIFICATION_SERVICE);
         * String channelId = "orders_channel";
         * if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
         * NotificationChannel channel = new NotificationChannel(channelId, "Orders",
         * NotificationManager.IMPORTANCE_HIGH);
         * channel.enableLights(true);
         * channel.setLightColor(Color.RED);
         * nm.createNotificationChannel(channel);
         * }
         * 
         * NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
         * channelId)
         * .setSmallIcon(R.drawable.ic_launcher)
         * .setContentTitle("Новый Заказ: Зал " + hallId)
         * .setContentText("Гостей: " + count)
         * .setColor(ContextCompat.getColor(this, R.color.red_accent))
         * .setPriority(NotificationCompat.PRIORITY_HIGH);
         * 
         * nm.notify(hallId, builder.build());
         */
    }

    private LinearLayout createRow(View.OnClickListener listener, int c1, int c2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(createStyledButton(c1, listener));
        row.addView(createStyledButton(c2, listener));
        return row;
    }

    private Button createStyledButton(int count, View.OnClickListener listener) {
        Button btn = new Button(this);
        String text = count + " чел";
        if (count == 10)
            text = "Фулл (10)";
        btn.setText(text);
        btn.setTag(count);
        btn.setOnClickListener(listener);

        btn.setTextColor(Color.BLACK);
        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.neon_green));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(16, 8, 16, 8);
        btn.setLayoutParams(p);

        return btn;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_pos) {
            // Already here
        } else if (id == R.id.nav_inventory) {
            startActivity(new Intent(this, InventoryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(this, ReportsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_logout) {
            // Logout: clear session and go to login
            android.content.SharedPreferences prefs = getSharedPreferences("BarAppPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
