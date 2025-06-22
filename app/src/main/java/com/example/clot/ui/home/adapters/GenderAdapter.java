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
import com.example.clot.models.Gender;

import java.util.List;

public class GenderAdapter extends RecyclerView.Adapter<GenderAdapter.ViewHolder> {

    private final List<Gender> genders;
    private final OnGenderClickListener listener;
    private int selectedPosition = -1;

    public interface OnGenderClickListener {
        void onGenderClick(Gender gender);
    }

    public GenderAdapter(List<Gender> genders, OnGenderClickListener listener) {
        this.genders = genders;
        this.listener = listener;
    }

    public void updateData(List<Gender> newGenders) {
        this.genders.clear();
        this.genders.addAll(newGenders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gender, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gender gender = genders.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(gender, isSelected);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onGenderClick(gender);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView genderName;
        private final ImageView ivCheck;
        private final View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            genderName = itemView.findViewById(R.id.genderName);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }

        void bind(Gender gender, boolean isSelected) {
            genderName.setText(gender.getName());

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_500));
                genderName.setTextColor(Color.WHITE);
                ivCheck.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                genderName.setTextColor(Color.BLACK);
                ivCheck.setVisibility(View.INVISIBLE);
            }
        }
    }
}
