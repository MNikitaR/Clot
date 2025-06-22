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
import com.example.clot.models.Sizes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.ViewHolder> {

    private List<Sizes> sizes;
    private OnSizeClickListener listener;
    private Set<String> selectedIds = new HashSet<>();

    public interface OnSizeClickListener {
        void onSizeClick(Sizes size);
    }

    public SizeAdapter(List<Sizes> sizes, OnSizeClickListener listener) {
        this.sizes = sizes;
        this.listener = listener;
    }

    public void updateData(List<Sizes> newSizes) {
        this.sizes = newSizes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_size, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sizes size = sizes.get(position);
        boolean isSelected = selectedIds.contains(size.getId());
        holder.bind(size, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSizeClick(size);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sizeText;
        private ImageView checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sizeText = itemView.findViewById(R.id.sizeText);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public void bind(Sizes size, boolean isSelected) {
            sizeText.setText(size.getName());

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_500));
                sizeText.setTextColor(Color.WHITE);
                checkBox.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                sizeText.setTextColor(Color.BLACK);
                checkBox.setVisibility(View.INVISIBLE);
            }
        }
    }
}