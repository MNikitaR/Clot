package com.example.clot.ui.profile.wishlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Product;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private List<Product> products;
    private final OnWishlistClickListener listener;
    private OnItemClickListener onItemClickListener;

    public interface OnWishlistClickListener {
        void onWishlistClick(Product product);
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public WishlistAdapter(List<Product> products, OnWishlistClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateList(List<Product> newList) {
        products = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final ImageButton wishlistButton;
        private final ImageView productImageView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            wishlistButton = itemView.findViewById(R.id.btn_favorite);
            productImageView = itemView.findViewById(R.id.product_image);
        }

        void bind(Product product) {
            nameTextView.setText(product.getName());
            // Форматирование цены
            if (product.getPrice() % 1 == 0) {
                priceTextView.setText(String.format("%d₽", (int) product.getPrice()));
            } else {
                priceTextView.setText(String.format("%.2f₽", product.getPrice()));
            }

            // Загрузка изображения
            String url = SupabaseClient.STORAGE_PRODUCT + product.getPrimaryImageUrl();
            Glide.with(itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.error)
                    .error(R.drawable.error)
                    .into(productImageView);

            // Всегда показываем заполненное сердечко в wishlist
            wishlistButton.setImageResource(R.drawable.ic_heart_filled);

            // Обработчик удаления из Wishlist
            wishlistButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWishlistClick(product);
                }
            });

            // Обработчик клика на товар
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(product);
                }
            });
        }
    }
}