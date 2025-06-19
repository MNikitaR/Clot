package com.example.clot.ui.profile.paymentmethod;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.models.PaymentMethod;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    private List<PaymentMethod> paymentMethods;
    private PaymentMethodClickListener clickListener;
    private PaymentMethodLongClickListener longClickListener;

    public interface PaymentMethodClickListener {
        void onEditClick(PaymentMethod paymentMethod);
    }

    public interface PaymentMethodLongClickListener {
        void onDeleteClick(PaymentMethod paymentMethod);
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods,
                                PaymentMethodClickListener clickListener,
                                PaymentMethodLongClickListener longClickListener) {
        this.paymentMethods = paymentMethods;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);

        // Установка данных
        holder.ivBrand.setImageResource(paymentMethod.getBrandIcon());
        holder.tvCardNumber.setText(paymentMethod.getFormattedCardNumber());

        // Обработчик редактирования
        holder.btnEdit.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onEditClick(paymentMethod);
            }
        });

        // Обработчик долгого нажатия для удаления
        holder.tvCardNumber.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDeleteClick(paymentMethod);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public void removePaymentMethod(PaymentMethod paymentMethod) {
        int position = paymentMethods.indexOf(paymentMethod);
        if (position != -1) {
            paymentMethods.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBrand;
        TextView tvCardNumber;
        Button btnEdit;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBrand = itemView.findViewById(R.id.ivBrand);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}