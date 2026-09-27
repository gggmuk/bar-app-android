package com.example.barapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BarSelectorAdapter extends RecyclerView.Adapter<BarSelectorAdapter.ViewHolder> {

    private List<BarItemModel> originalList;
    private List<BarItemModel> filteredList;
    private OnItemAddListener listener;

    public interface OnItemAddListener {
        void onItemAdd(BarItemModel item);
    }

    public BarSelectorAdapter(List<BarItemModel> items, OnItemAddListener listener) {
        this.originalList = items;
        this.filteredList = new ArrayList<>(items);
        this.listener = listener;
    }

    public void filter(String category) {
        filteredList.clear();
        if (category.equals("ВСЕ")) {
            filteredList.addAll(originalList);
        } else {
            // Map Russian tab names to English database categories
            String dbCategory = mapCategoryToDb(category);
            for (BarItemModel item : originalList) {
                if (item.getCategory().equalsIgnoreCase(dbCategory)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    private String mapCategoryToDb(String tabCategory) {
        switch (tabCategory) {
            case "ПИВО":
                return "Beer";
            case "НАПИТКИ":
                return "Drinks";
            case "КАЛЬЯН":
                return "Hookah";
            case "КУХНЯ":
                return "Snacks";
            default:
                return tabCategory;
        }
    }
    
    public void updateData(List<BarItemModel> newItems) {
        this.originalList = newItems;
        // Re-filter based on current need? simpler to just reset to all or keep current if I tracked it.
        // For now reset to all.
        this.filteredList = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the inventory item layout if looks compatible, or standard simple list item.
        // Assuming item_bar_inventory exists and has name, price views.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bar_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarItemModel item = filteredList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvName.setTextColor(Color.WHITE);
        
        holder.tvCategory.setText(item.getCategory());
        holder.tvCategory.setTextColor(Color.LTGRAY);

        holder.tvValue.setText(item.getPrice() + " c");
        holder.tvValue.setTextColor(Color.parseColor("#39FF14")); // Neon Green

        holder.btnAction.setText("+");
        holder.btnAction.setBackgroundColor(Color.parseColor("#39FF14")); // Neon Green
        holder.btnAction.setTextColor(Color.BLACK);
        
        holder.btnAction.setOnClickListener(v -> {
            // Click Effect
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50).withEndAction(() -> 
                v.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
            ).start();
            
            if (listener != null) listener.onItemAdd(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvValue;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            // Assuming these IDs exist in item_bar_inventory from InventoryAdapter
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvValue = itemView.findViewById(R.id.tv_item_value);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
