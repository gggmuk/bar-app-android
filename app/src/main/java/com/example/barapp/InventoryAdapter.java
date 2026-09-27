package com.example.barapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public enum Mode {
        STOCK, PRICE
    }

    private List<BarItemModel> items;
    private Mode currentMode = Mode.STOCK;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onAction(BarItemModel item);
    }

    public InventoryAdapter(List<BarItemModel> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<BarItemModel> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bar_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarItemModel item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvName.setTextColor(android.graphics.Color.WHITE);
        holder.tvCategory.setText(item.getCategory());
        holder.tvCategory.setTextColor(android.graphics.Color.LTGRAY);

        if (currentMode == Mode.STOCK) {
            holder.tvValue.setText(item.getCurrentStock() + " шт");
            holder.btnAction.setText("+");
            holder.btnAction.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Green for Add
        } else {
            holder.tvValue.setText(item.getPrice() + " с");
            holder.btnAction.setText("✎");
            holder.btnAction.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")); // Orange for Edit
        }
        holder.tvValue.setTextColor(android.graphics.Color.parseColor("#FFC107")); // Amber

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAction(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvValue;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvValue = itemView.findViewById(R.id.tv_item_value);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
