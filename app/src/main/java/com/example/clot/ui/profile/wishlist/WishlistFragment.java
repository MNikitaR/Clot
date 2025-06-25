package com.example.clot.ui.profile.wishlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Product;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private SupabaseClient supabaseClient;
    private List<Product> wishlistProducts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wishlist, container, false);

        recyclerView = view.findViewById(R.id.rv_wishlist);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        supabaseClient = new SupabaseClient();
        adapter = new WishlistAdapter(wishlistProducts, this::onWishlistClick);
        recyclerView.setAdapter(adapter);

        loadWishlist();
        return view;
    }

    private void loadWishlist() {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) return;

        supabaseClient.fetchWishlistProducts(userId, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<Product> products = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            JSONObject productJson = item.getJSONObject("products");
                            Product product = new Gson().fromJson(productJson.toString(), Product.class);
                            product.setFavorite(true); // Пометить как избранный
                            products.add(product);
                        }

                        wishlistProducts.clear();
                        wishlistProducts.addAll(products);
                        adapter.updateList(wishlistProducts);
                    } catch (JSONException e) {
                        Log.e("WishlistFragment", "JSON parsing error", e);
                    }
                });
            }
        });
    }

    private void onWishlistClick(Product product) {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) return;

        supabaseClient.removeFromWishlist(userId, product.getId(), new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    wishlistProducts.remove(product);
                    adapter.updateList(wishlistProducts);
                    Toast.makeText(requireContext(), "Товар удален!",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}