package com.example.clot.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Colors;
import com.example.clot.models.Product;
import com.example.clot.models.ProductImage;
import com.example.clot.models.ProductVariant;
import com.example.clot.models.Review;
import com.example.clot.pin.PinFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DetailFragment extends Fragment {

    private int productId;
    private Product product;
    private List<ProductImage> productImages = new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private List<Colors> allColors = new ArrayList<>();
    private Map<String, String> colorNameToHexMap = new HashMap<>();

    // UI элементы
    private ViewPager imagePager;
    private TextView tvProductName, tvPrice, tvDescription;
    private LinearLayout sizeContainer, colorContainer;
    private ImageButton btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt("product_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);
        initViews(view);
        loadProductData();
        return view;
    }

    private void initViews(View view) {
        imagePager = view.findViewById(R.id.imagePager);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvDescription = view.findViewById(R.id.tvDescription);
        sizeContainer = view.findViewById(R.id.sizeContainer);
        colorContainer = view.findViewById(R.id.colorContainer);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void loadProductData() {
        // 1. Загрузка основного товара
        loadProduct();

        // 2. Загрузка изображений
        loadProductImages();

/*        // 3. Загрузка вариантов
        loadVariants();

        loadColors();*/

/*        // 4. Загрузка отзывов
        loadReviews();*/
    }

    private void loadProduct() {
        SupabaseClient.getProductById(productId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadProduct:onResponse", responseBody);

                    Gson gson = new Gson();

                    // 1. Парсим ответ как список товаров
                    Type productListType = new TypeToken<List<Product>>(){}.getType();
                    List<Product> products = gson.fromJson(responseBody, productListType);

                    // 2. Проверяем, что получили хотя бы один товар
                    if (products != null && !products.isEmpty()) {
                        product = products.get(0); // Берем первый элемент массива

                        requireActivity().runOnUiThread(() -> {
                            // 3. Обновляем UI
                            tvProductName.setText(product.getName());

                            // 4. Форматирование цены
                            if (product.getPrice() % 1 == 0) {
                                tvPrice.setText(String.format("%d₽", (int) product.getPrice()));
                            } else {
                                tvPrice.setText(String.format("%.2f₽", product.getPrice()));
                            }

                            tvDescription.setText(product.getDescription());
                        });
                    } else {
                        Log.e("DetailFragment", "No product found for ID: " + productId);
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("updateProfile:onFailure", e.getLocalizedMessage());
                });
            }
        });
    }

    private void loadProductImages() {
        SupabaseClient.getProductImages(productId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadProductImages:onResponse", responseBody);
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ProductImage>>(){}.getType();
                    productImages = gson.fromJson(responseBody, listType);
                    requireActivity().runOnUiThread(() -> setupImagePager());
                });
            }

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadProductImages:onFailure", e.getLocalizedMessage());
                });
            }
        });
    }

    private void loadVariants() {
        SupabaseClient.getProductVariants(productId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadVariants:onResponse", responseBody);
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ProductVariant>>(){}.getType();
                    variants = gson.fromJson(responseBody, listType);
                    requireActivity().runOnUiThread(() -> {
                        setupSizeSelector();
                        setupColorSelector();
                    });
                });
            }

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadVariants:onFailure", e.getLocalizedMessage());
                });            }
        });
    }

  /*  private void loadReviews() {
        SupabaseClient.getProductReviews(productId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Review>>(){}.getType();
                reviews = gson.fromJson(responseBody, listType);
                requireActivity().runOnUiThread(() -> updateReviewsUI());
            }

            @Override
            public void onFailure(IOException e) {
                Log.e("DetailFragment", "Error loading reviews", e);
            }
        });
    }*/

    private void setupImagePager() {
        ImagePagerAdapter adapter = new ImagePagerAdapter(getContext(), productImages);
        imagePager.setAdapter(adapter);

        // Добавляем индикатор точек
        TabLayout tabLayout = getView().findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(imagePager, true);
    }

    private void setupSizeSelector() {
        sizeContainer.removeAllViews();

        // Получаем уникальные размеры
        Set<String> sizes = variants.stream()
                .filter(v -> v.getStock() > 0)
                .map(ProductVariant::getSize)
                .collect(Collectors.toSet());

        for (String size : sizes) {
            Button button = new Button(getContext());
            button.setText(size);
            button.setBackgroundResource(R.drawable.bg_size_selector);
            button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.color_size_text_selector));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                // Сохраняем выбранный размер
                updateButtonStates(sizeContainer, v);
            });

            sizeContainer.addView(button);
        }
    }

    private void updateButtonStates(ViewGroup container, View selectedView) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setSelected(child == selectedView);
        }
    }

/*    private void updateReviewsUI() {
        if (reviews.isEmpty()) {
            tvRating.setText("0.0");
            tvReviewsCount.setText("0 Reviews");
            return;
        }

        // Рассчитываем средний рейтинг
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        tvRating.setText(String.format("%.1f", averageRating));
        tvReviewsCount.setText(String.format("%d Reviews", reviews.size()));

        // TODO: Добавить RecyclerView для отображения списка отзывов
    }*/

    // Адаптер для галереи изображений
    private static class ImagePagerAdapter extends PagerAdapter {
        private Context context;
        private List<ProductImage> images;

        public ImagePagerAdapter(Context context, List<ProductImage> images) {
            this.context = context;
            this.images = images;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String url = SupabaseClient.STORAGE_PRODUCT  + images.get(position).getImage_url();

            // Загрузка изображения с помощью Glide
            Glide.with(context)
                    .load(url)
                    .into(imageView);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void loadColors() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchColor(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("ColorLoad:onFailure", e.getLocalizedMessage());
                    // Даже при ошибке пытаемся настроить селектор с дефолтными цветами
                    setupColorSelector();
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        Gson gson = new Gson();
                        // Исправленный парсинг
                        Type type = new TypeToken<List<Colors>>(){}.getType();
                        List<Colors> colorList = gson.fromJson(responseBody, type);

                        // Обновляем карту цветов
                        colorNameToHexMap.clear();
                        for (Colors color : colorList) {
                            colorNameToHexMap.put(color.getName(), color.getHex_code());
                        }

                        // Настраиваем селектор цветов
                        setupColorSelector();
                    } catch (Exception e) {
                        Log.e("ColorLoad:onResponse", "Error parsing colors: " + e.getMessage());
                        setupColorSelector();
                    }
                });
            }
        });
    }

    private void setupColorSelector() {
        if (colorContainer == null || variants == null) return;
        colorContainer.removeAllViews();

        // Получаем уникальные доступные цвета из вариантов
        Set<String> uniqueColorNames = variants.stream()
                .filter(v -> v.getStock() > 0)
                .map(ProductVariant::getColor)
                .collect(Collectors.toSet());

        // Создаем кнопки для каждого доступного цвета
        for (String colorName : uniqueColorNames) {
            // Безопасное получение hex-кода из карты цветов
            String colorHex = getColorHexByName(colorName);

            Button button = createColorButton(colorHex);
            button.setTag(colorName); // Сохраняем имя цвета в теге

            button.setOnClickListener(v -> {
                updateButtonStates(colorContainer, v);
            });

            colorContainer.addView(button);
        }
    }

    private String getColorHexByName(String colorName) {
        // Проверяем карту цветов
        if (colorNameToHexMap.containsKey(colorName)) {
            return colorNameToHexMap.get(colorName);
        }

        // Если в карте нет, ищем в списке (на случай если карта не обновилась)
        for (Colors color : allColors) {
            if (colorName.equals(color.getName())) {
                return color.getHex_code();
            }
        }

        // Дефолтное значение, если цвет не найден
        return "#FFFFFF";
    }

    private Button createColorButton(String colorHex) {
        Button button = new Button(getContext());

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);

        try {
            shape.setColor(Color.parseColor(colorHex));
        } catch (IllegalArgumentException e) {
            shape.setColor(Color.WHITE); // Дефолтный цвет при ошибке
        }

        shape.setStroke(2, ContextCompat.getColor(getContext(), R.color.gray));
        button.setBackground(shape);

        // Размеры кнопки
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                48,
                getResources().getDisplayMetrics()
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(0, 0, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                getResources().getDisplayMetrics()
        ), 0);

        button.setLayoutParams(params);
        return button;
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(DetailFragment.this).popBackStack();
    }

   /* private void setupQuantitySelector(View view) {
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });
    }

    private void setupAddToBagButton(View view) {
        Button btnAddToBag = view.findViewById(R.id.btnAddToBag);
        btnAddToBag.setOnClickListener(v -> {
            if (selectedSize.isEmpty() || selectedColor.isEmpty()) {
                Toast.makeText(getContext(), "Please select size and color", Toast.LENGTH_SHORT).show();
                return;
            }

            // Логика добавления в корзину
            addToCart();
        });
    }

    private void addToCart() {
       *//* CartItem item = new CartItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity,
                selectedSize,
                selectedColor,
                product.getImageUrl()
        );

        // Сохраняем в корзину (SharedPreferences, Room, или ViewModel)
        CartManager.addToCart(item);
        Toast.makeText(getContext(), "Added to bag", Toast.LENGTH_SHORT).show();
    }*/
}