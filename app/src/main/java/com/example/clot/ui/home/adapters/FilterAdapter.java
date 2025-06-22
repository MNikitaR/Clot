package com.example.clot.ui.home.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.models.Filter;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private final List<Filter> filters;
    private final OnFilterClickListener listener;

    public FilterAdapter(List<Filter> filters, OnFilterClickListener listener) {
        this.filters = filters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Filter filter = filters.get(position);

        // Настройка внешнего вида в зависимости от состояния
        int bgColor = filter.isActive() ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_500) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.gray);

        int textColor = filter.isActive() ? Color.WHITE : Color.BLACK;

        holder.button.setText(filter.getName());
        holder.button.setBackgroundColor(bgColor);
        holder.button.setTextColor(textColor);

        holder.itemView.setOnClickListener(v -> listener.onFilterClick(filter));
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Button button;

        ViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btnFilter);
        }
    }

    public interface OnFilterClickListener {
        void onFilterClick(Filter filter);
    }
}
