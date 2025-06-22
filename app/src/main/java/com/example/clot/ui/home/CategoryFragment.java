package com.example.clot.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Category;
import com.example.clot.ui.home.adapters.ShopcategoryAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CategoryFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private ImageButton btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shop_by_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        btnBack = view.findViewById(R.id.btnBack);

        // Загрузка данных
        loadCategories();

        // Обработчики кликов
        setupClickListeners();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(view -> navigateBack());
    }

    private void loadCategories() {
        // Получение данных пользователя из Supabase
        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchCategories( new SupabaseClient.SBC_Callback() {

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
                    Type type = new TypeToken<List<Category>>(){}.getType();
                    List<Category> categoryList = gson.fromJson(responseBody, type);
                    Log.e("loadCategories:onResponse", categoryList.toString());
                    ShopcategoryAdapter categoryAdapter = new ShopcategoryAdapter(requireContext(), categoryList, CategoryFragment.this::navigateToProducts);
                    categoriesRecyclerView.setAdapter(categoryAdapter);
                    categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                });
            }
        });
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(CategoryFragment.this).popBackStack();
    }

    // Метод для перехода к товарам категории
    private void navigateToProducts(Category category) {
        // Создаем новый фрагмент с товарами
        ProductListFragment fragment = new ProductListFragment();

        // Передаем ID категории через аргументы
        Bundle args = new Bundle();
        args.putString("category_id", String.valueOf(category.getId()));
        args.putString("category_name", category.getName());
        fragment.setArguments(args);

        // Выполняем переход
        NavHostFragment.findNavController(CategoryFragment.this)
                .navigate(R.id.action_categoryFragment_to_productListFragment, args);
    }
}