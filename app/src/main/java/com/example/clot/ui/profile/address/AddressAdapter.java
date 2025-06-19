package com.example.clot.ui.profile.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private AddressClickListener clickListener;
    private AddressLongClickListener longClickListener;

    public interface AddressClickListener {
        void onEditClick(Address address);
    }

    public interface AddressLongClickListener {
        void onDeleteClick(Address address);
    }

    public AddressAdapter(List<Address> addressList,
                          AddressClickListener clickListener,
                          AddressLongClickListener longClickListener) {
        this.addressList = addressList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);

        // Форматируем адрес
        String formattedAddress = address.getStreet() + "\n" +
                address.getCity() + ", " +
                address.getState() + " " +
                address.getZipCode();

        holder.tvAddress.setText(formattedAddress);

        // Обработчик редактирования
        holder.btnEdit.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onEditClick(address);
            }
        });

        // Обработчик долгого нажатия для удаления
        holder.tvAddress.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDeleteClick(address);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public void removeAddress(Address address) {
        int position = addressList.indexOf(address);
        if (position != -1) {
            addressList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress;
        Button btnEdit;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}