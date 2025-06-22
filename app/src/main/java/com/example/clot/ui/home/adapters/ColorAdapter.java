package com.example.clot.ui.home.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.models.Colors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {

    private List<Colors> colors;
    private OnColorClickListener listener;
    private Set<String> selectedIds = new HashSet<>();

    public interface OnColorClickListener {
        void onColorClick(Colors color);
    }

    public ColorAdapter(List<Colors> colors, OnColorClickListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    public void updateData(List<Colors> newColors) {
        this.colors = newColors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Colors color = colors.get(position);
        boolean isSelected = selectedIds.contains(color.getId());
        holder.bind(color, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorClick(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View colorView;
        private TextView colorName;
        private ImageView checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.colorView);
            colorName = itemView.findViewById(R.id.colorName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public void bind(Colors color, boolean isSelected) {
            // Установка цвета
            try {
                colorView.setBackgroundColor(Color.parseColor(color.getHex_code()));
            } catch (Exception e) {
                colorView.setBackgroundColor(Color.GRAY);
            }

            colorName.setText(color.getName());
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_500));
                colorName.setTextColor(Color.WHITE);
                checkBox.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                colorName.setTextColor(Color.BLACK);
                checkBox.setVisibility(View.INVISIBLE);
            }
        }
    }
}