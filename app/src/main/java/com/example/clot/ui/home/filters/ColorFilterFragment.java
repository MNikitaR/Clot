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
import com.example.clot.models.Colors;
import com.example.clot.ui.home.SearchFragment;
import com.example.clot.ui.home.adapters.ColorAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColorFilterFragment extends BottomSheetDialogFragment
        implements ColorAdapter.OnColorClickListener {

    private RecyclerView recyclerView;
    private Button btnApply;
    private ColorAdapter adapter;
    private Set<String> selectedColors = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_color, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnApply = view.findViewById(R.id.btnApply);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        // Инициализация адаптера
        adapter = new ColorAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Загрузка цветов
        loadColors();

        btnApply.setOnClickListener(v -> {
            if (getParentFragment() instanceof SearchFragment) {
                SearchFragment fragment = (SearchFragment) getParentFragment();
                fragment.applyColorFilter(selectedColors);
            }
            dismiss();
        });
    }

    private void loadColors() {

        // Загрузка цветов из БД

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchColor(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("ColorFilter", "Error loading genders", e);
            }

            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Colors>>(){}.getType();
                List<Colors> colors = gson.fromJson(responseBody, type);

                requireActivity().runOnUiThread(() -> {
                    if (colors != null) {
                        adapter.updateData(colors);
                    }
                });
            }
        });
    }

    @Override
    public void onColorClick(Colors color) {
        String colorId = String.valueOf(color.getId());
        if (selectedColors.contains(colorId)) {
            selectedColors.remove(colorId);
        } else {
            selectedColors.add(colorId);
        }
        adapter.notifyDataSetChanged();
    }

    public interface FilterListener {
        void applyColorFilter(Set<String> colors);
    }
}
