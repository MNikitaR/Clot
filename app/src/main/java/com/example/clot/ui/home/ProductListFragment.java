package com.example.clot.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Product;
import com.example.clot.ui.home.adapters.ProductGridAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ProductListFragment extends Fragment {
    private RecyclerView productsRecyclerView;
    private String categoryId, categoryName;
    private ImageButton btnBack;
    private TextView tvCategoryName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Получаем ID категории из аргументов
        if (getArguments() != null) {
            categoryId = getArguments().getString("category_id");
            categoryName = getArguments().getString("category_name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 колонки
        btnBack = view.findViewById(R.id.btnBack);
        tvCategoryName = view.findViewById(R.id.category_name);

        tvCategoryName.setText(categoryName);

        // Загружаем товары категории
        loadProductsByCategory();
        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void loadProductsByCategory() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchProductsByCategory(categoryId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadCategories:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadCategories:onResponse", responseBody);
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Product>>(){}.getType();
                    List<Product> productList = gson.fromJson(responseBody, type);
                    Log.e("loadCategories:onResponse", productList.toString());
                    ProductGridAdapter productGridAdapter = new ProductGridAdapter(requireContext(), productList);

                    // Установка обработчика кликов
                    productGridAdapter.setOnItemClickListener(product -> {
                        // Создаем фрагмент детализации
                        DetailFragment detailFragment = new DetailFragment();

                        // Передаем данные через Bundle
                        Bundle args = new Bundle();
                        args.putInt("product_id", product.getId());
                        detailFragment.setArguments(args);

                        // Переход к фрагменту
                        NavHostFragment.findNavController(ProductListFragment.this)
                                .navigate(R.id.action_productListFragment_to_productDetailsFragment, args);
                    });

                    productGridAdapter.setOnFavoriteClickListener((product, isFavorite) -> {

                        if (isFavorite) {
                            // Проверка наличия товара в избранном
                            supabaseClient.checkInWishlist(DataBinding.getUuidUser(), product.getId(),
                                    new SupabaseClient.SBC_Callback() {
                                        @Override
                                        public void onFailure(IOException e) {
                                            requireActivity().runOnUiThread(() ->
                                                    Toast.makeText(requireContext(), "Ошибка проверки избранного",
                                                            Toast.LENGTH_SHORT).show()
                                            );
                                        }

                                        @Override
                                        public void onResponse(String responseBody) {
                                            requireActivity().runOnUiThread(() -> {
                                                try {
                                                    JSONArray jsonArray = new JSONArray(responseBody);
                                                    if (jsonArray.length() > 0) {
                                                        // Товар уже в избранном
                                                        Toast.makeText(requireContext(),
                                                                "Товар уже в избранном",
                                                                Toast.LENGTH_SHORT).show();

                                                        // Сбрасываем состояние сердечка
                                                        int position = productGridAdapter.getPositionForProduct(product);
                                                        if (position != -1) {
                                                            product.setFavorite(false);
                                                            productGridAdapter.notifyItemChanged(position);
                                                        }
                                                    } else {
                                                        // Добавляем товар
                                                        supabaseClient.addToWishlist(DataBinding.getUuidUser(), product.getId(),
                                                                new SupabaseClient.SBC_Callback() {
                                                                    @Override
                                                                    public void onFailure(IOException e) {
                                                                        requireActivity().runOnUiThread(() -> {
                                                                            Toast.makeText(requireContext(), "Ошибка добавления в избранное",
                                                                                    Toast.LENGTH_SHORT).show();

                                                                            // Сбрасываем состояние сердечка при ошибке
                                                                            int position = productGridAdapter.getPositionForProduct(product);
                                                                            if (position != -1) {
                                                                                product.setFavorite(false);
                                                                                productGridAdapter.notifyItemChanged(position);
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onResponse(String responseBody) {
                                                                        requireActivity().runOnUiThread(() ->
                                                                                Toast.makeText(requireContext(), "Добавлено в избранное!",
                                                                                        Toast.LENGTH_SHORT).show()
                                                                        );
                                                                    }
                                                                });
                                                    }
                                                } catch (JSONException e) {
                                                    Toast.makeText(requireContext(),
                                                            "Ошибка обработки данных",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                        }
                    });

                    productsRecyclerView.setAdapter(productGridAdapter);
                    productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                });
            }
        });
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(ProductListFragment.this).popBackStack();
    }
}
