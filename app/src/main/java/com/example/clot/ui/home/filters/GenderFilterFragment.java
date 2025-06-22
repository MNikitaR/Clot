package com.example.clot.ui.home.filters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Gender;
import com.example.clot.ui.home.SearchFragment;

import com.example.clot.ui.home.adapters.GenderAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenderFilterFragment extends BottomSheetDialogFragment
        implements GenderAdapter.OnGenderClickListener {

    private RecyclerView recyclerView;
    private Button btnApply;
    private GenderAdapter adapter;
    private Gender selectedGender;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_gender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnApply = view.findViewById(R.id.btnApply);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Инициализация адаптера
        adapter = new GenderAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Загрузка гендеров
        loadGenders();

        // Обработчик кнопки Apply
        btnApply.setOnClickListener(v -> {
            List<String> selected = new ArrayList<>();
            if (selectedGender != null) {
                selected.add(selectedGender.getName());
            }

            if (getParentFragment() instanceof SearchFragment) {
                SearchFragment fragment = (SearchFragment) getParentFragment();
                fragment.applyGenderFilter(selected);
            }
            dismiss();
        });
    }

    private void loadGenders() {
        // Загрузка гендеров из БД

        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.fetchGenders(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("GenderFilter", "Error loading genders", e);
            }

            @Override
            public void onResponse(String responseBody) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Gender>>(){}.getType();
                List<Gender> genderList = gson.fromJson(responseBody, type);

                requireActivity().runOnUiThread(() -> {
                    if (genderList != null && !genderList.isEmpty()) {
                        adapter.updateData(genderList);
                    }
                });
            }
        });
    }

    @Override
    public void onGenderClick(Gender gender) {
        selectedGender = gender;
    }

    public interface FilterListener {
        void applyGenderFilter(List<String> genders);
    }
}