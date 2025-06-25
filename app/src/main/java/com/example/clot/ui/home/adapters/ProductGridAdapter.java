package com.example.clot.ui.home.adapters;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

// Адаптер для сетки товаров (Popular Products)
public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> products = new ArrayList<>();

    public ProductGridAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    private OnItemClickListener onItemClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isFavorite);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }

    public int getPositionForProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == product.getId()) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.nameTextView.setText(product.getName());
        // Исправленное форматирование цены
        if (product.getPrice() % 1 == 0) {
            // Для целых чисел
            holder.priceTextView.setText(String.format("%d₽", (int) product.getPrice()));
        } else {
            // Для дробных чисел
            holder.priceTextView.setText(String.format("%.2f₽", product.getPrice()));
        }
        String url = SupabaseClient.STORAGE_PRODUCT  + product.getPrimaryImageUrl();

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.error)
                .error(R.drawable.error)
                .into(holder.imageView);

        // Установка состояния сердечка
        holder.btnFavorite.setSelected(product.isFavorite());

        // Обработчик клика на сердечко
        holder.btnFavorite.setOnClickListener(v -> {
            product.setFavorite(true);
            holder.btnFavorite.setSelected(true);

            if (onFavoriteClickListener != null) {
                onFavoriteClickListener.onFavoriteClick(product, true);
            }
        });

        // Обработка клика на весь элемент товара
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final ImageButton btnFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.product_image);
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}