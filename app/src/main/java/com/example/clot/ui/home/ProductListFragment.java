package com.example.clot.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.models.Product;


public class ProductListFragment extends Fragment {

    private RecyclerView productsRecyclerView;
    private String categoryId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("category_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);

        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        TextView categoryTitle = view.findViewById(R.id.categoryTitle);

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Загрузка данных
        loadProducts(categoryTitle);

        return view;
    }

    private void loadProducts(TextView titleView) {

    }

    private void navigateToProductDetails(Product product) {
       /* Bundle args = new Bundle();
        args.putString("product_id", product.getId());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_productListFragment_to_productDetailsFragment, args);*/
    }
}