package com.example.clot.ui.home.filters;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clot.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PriceFilterFragment extends BottomSheetDialogFragment {
    // Добавляем интерфейс
    public interface FilterListener {
        void applyPriceFilter(double min, double max);
    }

    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Button btnApply;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_price, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        btnApply = view.findViewById(R.id.btnApply);

        // Устанавливаем фильтры ввода для цен
        etMinPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etMaxPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        btnApply.setOnClickListener(v -> {
            applyPriceFilter();
            dismiss();
        });
    }

    private void applyPriceFilter() {
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();

        double minPrice = minPriceStr.isEmpty() ? 0 : Double.parseDouble(minPriceStr);
        double maxPrice = maxPriceStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceStr);

        // Получаем родительский фрагмент
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof FilterListener) {
            FilterListener listener = (FilterListener) parentFragment;
            listener.applyPriceFilter(minPrice, maxPrice);
        }
    }
}