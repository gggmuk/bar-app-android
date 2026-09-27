package com.example.barapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private Button btnModeStock, btnModePrice, btnHome;
    private TextView tvModeTitle;

    private InventoryAdapter.Mode currentMode = InventoryAdapter.Mode.STOCK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recycler_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnModeStock = findViewById(R.id.btn_mode_stock);
        btnModePrice = findViewById(R.id.btn_mode_price);
        btnHome = findViewById(R.id.btn_home);
        tvModeTitle = findViewById(R.id.tv_mode_title);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        adapter = new InventoryAdapter(new ArrayList<>(), item -> {
            if (currentMode == InventoryAdapter.Mode.STOCK) {
                showAddStockDialog(item);
            } else {
                showEditPriceDialog(item);
            }
        });
        recyclerView.setAdapter(adapter);

        btnModeStock.setOnClickListener(v -> setMode(InventoryAdapter.Mode.STOCK));
        btnModePrice.setOnClickListener(v -> setMode(InventoryAdapter.Mode.PRICE));

        loadData();
    }

    private void setMode(InventoryAdapter.Mode mode) {
        currentMode = mode;
        adapter.setMode(mode);
        if (mode == InventoryAdapter.Mode.STOCK) {
            tvModeTitle.setText("Склад: Остатки и Приход");
            btnModeStock.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            btnModePrice.setBackgroundColor(Color.parseColor("#757575")); // Grey
        } else {
            tvModeTitle.setText("Управление Ценами");
            btnModeStock.setBackgroundColor(Color.parseColor("#757575")); // Grey
            btnModePrice.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
        }
    }

    private void loadData() {
        List<BarItemModel> items = dbHelper.getAllBarItems();
        adapter.updateData(items);
    }

    private void showAddStockDialog(BarItemModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Приход: " + item.getName());
        builder.setMessage("Текущий остаток: " + item.getCurrentStock());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Количество");
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String qtyStr = input.getText().toString();
            if (!qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    dbHelper.updateStock(item.getId(), qty, "INCOME");
                    Toast.makeText(this, "Оприходовано: " + qty, Toast.LENGTH_SHORT).show();
                    loadData();
                }
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showEditPriceDialog(BarItemModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Цена: " + item.getName());
        builder.setMessage("Текущая цена: " + item.getPrice());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(item.getPrice()));
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String priceStr = input.getText().toString();
            if (!priceStr.isEmpty()) {
                double newPrice = Double.parseDouble(priceStr);
                dbHelper.updateBarItemPrice(item.getId(), newPrice);
                Toast.makeText(this, "Цена обновлена", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}
