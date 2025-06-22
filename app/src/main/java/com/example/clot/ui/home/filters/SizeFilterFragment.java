package com.example.clot.ui.home.filters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Sizes;
import com.example.clot.ui.home.SearchFragment;
import com.example.clot.ui.home.adapters.SizeAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SizeFilterFragment extends BottomSheetDialogFragment
        implements SizeAdapter.OnSizeClickListener {

    private RecyclerView recyclerView;
    private Button btnApply;
    private SizeAdapter adapter;
    private List<Sizes> sizes = new ArrayList<>();
    private Set<String> selectedSizes = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_size, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnApply = view.findViewById(R.id.btnApply);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        // Инициализация адаптера
        adapter = new SizeAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Загрузка размеров
        loadSizes();

        btnApply.setOnClickListener(v -> {
            if (getParentFragment() instanceof SearchFragment) {
                SearchFragment fragment = (SearchFragment) getParentFragment();
                fragment.applySizeFilter(selectedSizes);
            }
            dismiss();
        });
    }

    private void loadSizes() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchSizes(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("SizeFilter", "Error loading sizes", e);
            }

            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Sizes>>(){}.getType();
                List<Sizes> sizes = gson.fromJson(responseBody, type);

                requireActivity().runOnUiThread(() -> {
                    if (sizes != null) {
                        adapter.updateData(sizes);
                    }
                });
            }
        });
    }

    @Override
    public void onSizeClick(Sizes size) {
        String sizeId = size.getId();
        if (selectedSizes.contains(sizeId)) {
            selectedSizes.remove(sizeId);
        } else {
            selectedSizes.add(sizeId);
        }
        adapter.notifyDataSetChanged();
    }

    public interface FilterListener {
        void applySizeFilter(Set<String> sizes);
    }
}