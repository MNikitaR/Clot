package com.example.clot.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Category;
import com.example.clot.models.Product;
import com.example.clot.models.Profile;
import com.example.clot.ui.home.adapters.CategoryAdapter;
import com.example.clot.ui.home.adapters.ProductGridAdapter;
import com.example.clot.ui.home.adapters.ProductHorizontalAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private RecyclerView topSellingRecyclerView;
    private RecyclerView popularProductsRecyclerView;
    private Button btnSeeAllCategories;
    private Spinner spiner;
    private ImageButton btnProfile;
//    private ImageButton btnCart;
    private TextInputEditText etSearch;
    private String avatar_url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        topSellingRecyclerView = view.findViewById(R.id.topSellingRecyclerView);
        popularProductsRecyclerView = view.findViewById(R.id.popularProductsRecyclerView);
        btnSeeAllCategories = view.findViewById(R.id.btnSeeAllCategories);
        btnProfile = view.findViewById(R.id.btnProfile);
        spiner = view.findViewById(R.id.genderSpinner);
        etSearch = view.findViewById(R.id.etSearch);

        // Загрузка данных
        loadCategories();
        loadTopSellingProducts();
        loadPopularProducts();
        loadUserImage();

        // Обработчики кликов
        setupClickListeners();

        // Инициализация селектора
        updateGenderSelector();
    }

    private void setupClickListeners() {
        // Кнопки See All
        btnSeeAllCategories.setOnClickListener(v -> navigateToAllCategories());

        // Кнопки навигации
        btnProfile.setOnClickListener(v -> navigateToProfile());
        // Поиск
        etSearch.setOnClickListener(v -> {
            navigateToSearch();
        });
    }

    private void updateGenderSelector() {
        // Создание адаптера с макетом по умолчанию
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );

        // Укажите макет для выпадающего списка
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Применение адаптера к Spinner
        spiner.setAdapter(adapter);

        // Обработка выбора элемента (опционально)
        spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGender = parent.getItemAtPosition(position).toString();
                // Действия при выборе
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Действие при отсутствии выбора
            }
        });
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
                    CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), categoryList, HomeFragment.this::navigateToProducts);
                    categoriesRecyclerView.setAdapter(categoryAdapter);
                    categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                });
            }
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
        NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_productListFragment, args);
    }

    private void loadTopSellingProducts() {
        // Получение данных пользователя из Supabase
        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchProducts( new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadTopSellingProducts:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadTopSellingProducts:onResponse", responseBody);
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Product>>() {
                    }.getType();
                    List<Product> productList = gson.fromJson(responseBody, type);
                    ProductHorizontalAdapter productHorizontalAdapter = new ProductHorizontalAdapter(requireContext(), productList);

                    // Установка обработчика кликов
                    productHorizontalAdapter.setOnItemClickListener(product -> {
                        // Создаем фрагмент детализации
                        DetailFragment detailFragment = new DetailFragment();

                        // Передаем данные через Bundle
                        Bundle args = new Bundle();
                        args.putInt("product_id", product.getId());
                        detailFragment.setArguments(args);

                        // Переход к фрагменту
                        NavHostFragment.findNavController(HomeFragment.this)
                                .navigate(R.id.action_homeFragment_to_productDetailsFragment, args);
                    });

                    // Добавляем обработчик избранного
                    productHorizontalAdapter.setOnFavoriteClickListener((product, isFavorite) -> {
                        if (isFavorite) {
                            SupabaseClient supabaseClient = new SupabaseClient();
                            supabaseClient.checkInWishlist(DataBinding.getUuidUser(), product.getId(),
                                    new SupabaseClient.SBC_Callback() {
                                        @Override
                                        public void onFailure(IOException e) {
                                            requireActivity().runOnUiThread(() -> {
                                                Toast.makeText(requireContext(), "Ошибка проверки избранного",
                                                        Toast.LENGTH_SHORT).show();
                                            });
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
                                                        int position = productHorizontalAdapter.getPositionForProduct(product);
                                                        if (position != -1) {
                                                            product.setFavorite(false);
                                                            productHorizontalAdapter.notifyItemChanged(position);
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
                                                                            int position = productHorizontalAdapter.getPositionForProduct(product);
                                                                            if (position != -1) {
                                                                                product.setFavorite(false);
                                                                                productHorizontalAdapter.notifyItemChanged(position);
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

                    topSellingRecyclerView.setAdapter(productHorizontalAdapter);
                    topSellingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                });
            }
        });
    }

    private void loadPopularProducts() {
        // Получение данных пользователя из Supabase
        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.fetchProducts( new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadPopularProducts:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadPopularProducts:onResponse", responseBody);
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Product>>(){}.getType();
                    List<Product> productList = gson.fromJson(responseBody, type);
                    ProductGridAdapter productGridAdapter = new ProductGridAdapter(getActivity().getApplicationContext(), productList);

                    // Установка обработчика кликов
                    productGridAdapter.setOnItemClickListener(product -> {
                        // Создаем фрагмент детализации
                        DetailFragment detailFragment = new DetailFragment();

                        // Передаем данные через Bundle
                        Bundle args = new Bundle();
                        args.putInt("product_id", product.getId());
                        detailFragment.setArguments(args);

                        // Переход к фрагменту
                        NavHostFragment.findNavController(HomeFragment.this)
                                .navigate(R.id.action_homeFragment_to_productDetailsFragment, args);
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

                    popularProductsRecyclerView.setAdapter(productGridAdapter);
                    popularProductsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                });
            }
        });
    }

    private void loadUserImage() {
        // Получение данных пользователя из Supabase
        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.getData( new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadUserImage:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadUserImage:onResponse", responseBody);
                    try {
                        parseAndDisplayImage(responseBody);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(),
                                "Ошибка обработки данных",
                                Toast.LENGTH_SHORT).show();
                        Log.e("loadUserImage", "Error: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void parseAndDisplayImage(String jsonResponse) {
        // Ответ приходит в виде массива, даже для одного элемента
        Gson gson = new Gson();
        Profile[] profiles = gson.fromJson(jsonResponse, Profile[].class);

        if (profiles != null && profiles.length > 0) {
            Profile profile = profiles[0];

            avatar_url = profile.getAvatar_url();
            // Загрузка аватара
            loadAvatar(avatar_url);
        } else {
            Toast.makeText(requireContext(),
                    "Данные профиля не найдены",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            btnProfile.setImageResource(R.drawable.profilepicture);
            return;
        }

        // Сформируйте публичный URL
        String publicUrl = SupabaseClient.STORAGE_AVATAR + avatarUrl;

        Glide.with(this)
                .load(publicUrl)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(btnProfile);
    }

    private void navigateToAllCategories() {
        // Переход к категориям
        NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_categoryFragment);
    }

    private void navigateToProfile() {
        // Получаем BottomNavigationView из активности
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);

        // Переключаемся на вкладку профиля
        bottomNav.setSelectedItemId(R.id.navigation_profile);
    }

    private void navigateToSearch() {
        NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_searchFragment);
    }
}