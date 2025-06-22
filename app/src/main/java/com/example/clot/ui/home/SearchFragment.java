package com.example.clot.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Category;
import com.example.clot.models.Product;
import com.example.clot.models.Filter;
import com.example.clot.ui.home.adapters.FilterAdapter;
import com.example.clot.ui.home.adapters.ProductGridAdapter;
import com.example.clot.ui.home.adapters.ShopcategoryAdapter;
import com.example.clot.ui.home.filters.ColorFilterFragment;
import com.example.clot.ui.home.filters.SizeFilterFragment;
import com.example.clot.ui.home.filters.GenderFilterFragment;
import com.example.clot.ui.home.filters.PriceFilterFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment implements
        ColorFilterFragment.FilterListener,
        GenderFilterFragment.FilterListener,
        PriceFilterFragment.FilterListener,
        SizeFilterFragment.FilterListener {

    private static final int STATE_EMPTY = 0;
    private static final int STATE_NO_RESULTS = 1;
    private static final int STATE_RESULTS = 2;

    // UI элементы
    private TextInputEditText etSearch;
    private RecyclerView categoriesRecyclerView, productsRecyclerView, filtersRecyclerView;
    private LinearLayout noResultsLayout;
    private Button btnExploreCategories;
    private ImageButton btnBack;

    // Состояния
    private int currentState = STATE_EMPTY;
    private String searchQuery = "";

    // Фильтры
    private Set<String> colorFilters = new HashSet<>();
    private List<String> genderFilters = new ArrayList<>();
    private double minPrice = 0;
    private double maxPrice = Double.MAX_VALUE;
    private Set<String> sizeFilters = new HashSet<>();

    // Адаптеры
    private FilterAdapter filterAdapter;
    private List<Filter> filterList = new ArrayList<>();
    private ProductGridAdapter productAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        initViews(view);
        setupAdapters();
        setupListeners();
        loadInitialData();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        filtersRecyclerView = view.findViewById(R.id.filtersRecyclerView);
        noResultsLayout = view.findViewById(R.id.noResultsLayout);
        btnExploreCategories = view.findViewById(R.id.btnExploreCategories);
        btnBack = view.findViewById(R.id.btnBackHome);

        // Настройка RecyclerView для продуктов
        productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupAdapters() {
        // Адаптер для фильтров
        filterList = Arrays.asList(
                new Filter("Color", false),
                new Filter("Gender", false),
                new Filter("Price", false),
                new Filter("Size", false)
        );

        filterAdapter = new FilterAdapter(filterList, this::showFilterBottomSheet);
        filtersRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        filtersRecyclerView.setAdapter(filterAdapter);

        // Адаптер для продуктов
        productAdapter = new ProductGridAdapter(requireContext(), new ArrayList<>());
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // Слушатель ввода поискового запроса
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                performSearch();
            }
        });

        // Кнопка "Explore Categories"
        btnExploreCategories.setOnClickListener(v -> {
            NavHostFragment.findNavController(SearchFragment.this)
                    .navigate(R.id.action_searchFragment_to_categoryFragment);
        });

        // Кнопка назад
        btnBack.setOnClickListener(v -> navigateBack());

        // Фокус на поле поиска
        etSearch.requestFocus();
        showKeyboard();
    }

    private void loadInitialData() {
        // Загрузка категорий для начального состояния
        loadCategories();
    }

    private void showFilterBottomSheet(Filter filter) {
        BottomSheetDialogFragment bottomSheet = null;

        switch (filter.getName()) {
            case "Category":
                bottomSheet = new ColorFilterFragment();
                break;
            case "Gender":
                bottomSheet = new GenderFilterFragment();
                break;
            case "Price":
                bottomSheet = new PriceFilterFragment();
                break;
            case "Deals":
                bottomSheet = new SizeFilterFragment();
                break;
        }

        if (bottomSheet != null) {
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        }
    }

    private void setState(int state) {
        currentState = state;
        categoriesRecyclerView.setVisibility(state == STATE_EMPTY ? View.VISIBLE : View.GONE);
        noResultsLayout.setVisibility(state == STATE_NO_RESULTS ? View.VISIBLE : View.GONE);
        productsRecyclerView.setVisibility(state == STATE_RESULTS ? View.VISIBLE : View.GONE);
        filtersRecyclerView.setVisibility(state == STATE_RESULTS ? View.VISIBLE : View.GONE);
    }

    private void performSearch() {
        if (searchQuery.isEmpty()) {
            setState(STATE_EMPTY);
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.searchProducts(
                searchQuery,
/*                colorFilters,
                genderFilters,
                minPrice,
                maxPrice,
                sizeFilters,*/
                new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e("performSearch:onFailure", e.getLocalizedMessage());
                        });
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        requireActivity().runOnUiThread(() -> {
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Product>>(){}.getType();
                            List<Product> productList = gson.fromJson(responseBody, type);

                            if (productList == null || productList.isEmpty()) {
                                setState(STATE_NO_RESULTS);
                            } else {
                                setState(STATE_RESULTS);
                                ProductGridAdapter adapter = new ProductGridAdapter(requireContext(), productList);

                                // Установка обработчика кликов
                                adapter.setOnItemClickListener(product -> {
                                    // Создаем фрагмент детализации
                                    DetailFragment detailFragment = new DetailFragment();

                                    // Передаем данные через Bundle
                                    Bundle args = new Bundle();
                                    args.putInt("product_id", product.getId());
                                    detailFragment.setArguments(args);

                                    // Переход к фрагменту
                                    NavHostFragment.findNavController(SearchFragment.this)
                                            .navigate(R.id.action_searchFragment_to_productDetailsFragment, args);
                                });

                                productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                                productsRecyclerView.setAdapter(adapter);
                            }
                        });
                    }
                }
        );
    }

    private void loadCategories() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchCategories(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("SearchFragment", "Failed to load categories", e);
            }

            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Category>>(){}.getType();
                List<Category> categories = gson.fromJson(responseBody, type);

                requireActivity().runOnUiThread(() -> {
                    ShopcategoryAdapter adapter = new ShopcategoryAdapter(requireContext(), categories, SearchFragment.this::navigateToProducts);
                    categoriesRecyclerView.setAdapter(adapter);
                    categoriesRecyclerView.setLayoutManager(
                            new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                });
            }
        });
    }

    // Реализация интерфейсов фильтров
    @Override
    public void applyColorFilter(Set<String> colors) {
        colorFilters  = colors;
        updateFilterState("Color", !colors.isEmpty(), colors.size());
        performSearch();
    }

    @Override
    public void applyGenderFilter(List<String> genders) {
        genderFilters = genders;
        updateFilterState("Gender", !genders.isEmpty(), genders.size());
        performSearch();
    }

    @Override
    public void applyPriceFilter(double min, double max) {
        minPrice = min;
        maxPrice = max;
        boolean active = min > 0 || max < Double.MAX_VALUE;
        updateFilterState("Price", active, active ? 1 : 0);
        performSearch();
    }

    @Override
    public void applySizeFilter(Set<String> sizes) {
        sizeFilters = sizes;
        updateFilterState("Size", !sizes.isEmpty(), sizes.size());
        performSearch();
    }


    private void updateFilterState(String filterName, boolean isActive, int count) {
        for (Filter filter : filterList) {
            if (filter.getName().equals(filterName)) {
                filter.setActive(isActive);
                break;
            }
        }
        filterAdapter.notifyDataSetChanged();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
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
        NavHostFragment.findNavController(SearchFragment.this)
                .navigate(R.id.action_searchFragment_to_productListFragment, args);
    }
}
