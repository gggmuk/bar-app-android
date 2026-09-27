package com.example.barapp;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvTotalRevenue, tvPieLegend;
    private SimpleLineChart chartRevenue;
    private SimplePieChart chartPie;
    private Button btnSync;
    private CardView cardTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DatabaseHelper(this);

        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalRevenue.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.neon_green));
        tvPieLegend = findViewById(R.id.tv_pie_legend);
        chartRevenue = findViewById(R.id.chart_revenue);
        chartPie = findViewById(R.id.chart_pie);
        btnSync = findViewById(R.id.btn_sync);

        // Find parent card of total revenue if possible to animate it
        // Or we just animate the TextView.
        // For simplicity, let's animate the TextView entry
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvTotalRevenue, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.start();

        loadAnalytics();

        btnSync.setOnClickListener(v -> {
            Toast.makeText(this, "Синхронизация с облаком...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAnalytics() {
        // 1. Total Revenue
        double totalRevenue = dbHelper.getTotalRevenue();
        tvTotalRevenue.setText(String.format("%.1f с", totalRevenue));

        // 2. Line Chart
        Map<String, Double> dailyData = dbHelper.getDailyRevenue(7);
        List<Double> points = new ArrayList<>(dailyData.values());
        if (points.isEmpty()) {
            points.add(0.0);
            points.add(0.0);
        }
        chartRevenue.setData(points);

        // 3. Pie Chart - Revenue Breakdown (Time vs Bar)
        double timeRevenue = dbHelper.getTimeBasedRevenue();
        double barRevenue = dbHelper.getBarRevenue();
        
        Map<String, Float> revenueBreakdown = new HashMap<>();
        revenueBreakdown.put("Тарифы (время)", (float) timeRevenue);
        revenueBreakdown.put("Бар", (float) barRevenue);
        
        if (timeRevenue == 0 && barRevenue == 0) {
            revenueBreakdown.put("Нет данных", 1f);
        }
        
        chartPie.setData(revenueBreakdown);

        // Calculate percentages for legend
        double total = timeRevenue + barRevenue;
        StringBuilder legend = new StringBuilder();
        
        if (total > 0) {
            double timePercent = (timeRevenue / total) * 100;
            double barPercent = (barRevenue / total) * 100;
            
            legend.append("• Тарифы: ")
                  .append(String.format("%.1f с (%.1f%%)", timeRevenue, timePercent))
                  .append("\n");
            legend.append("• Бар: ")
                  .append(String.format("%.1f с (%.1f%%)", barRevenue, barPercent))
                  .append("\n");
        } else {
            legend.append("Нет данных");
        }
        
        tvPieLegend.setText(legend.toString());
    }
}
