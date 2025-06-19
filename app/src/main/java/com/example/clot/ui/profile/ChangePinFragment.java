package com.example.clot.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.models.PinStorage;
import com.example.clot.view.MainActivity;

public class ChangePinFragment extends Fragment {

    private PinStorage pinStorage;
    private String userId;
    private EditText etCurrentPin, etNewPin, etConfirmPin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация объектов, не зависящих от вью
        pinStorage = new PinStorage(requireContext());
        userId = DataBinding.getUuidUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_pin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCurrentPin = view.findViewById(R.id.etCurrentPin);
        etNewPin = view.findViewById(R.id.etNewPin);
        etConfirmPin = view.findViewById(R.id.etConfirmPin);
        Button btnChangePin = view.findViewById(R.id.btnChangePin);

        // Установка фильтров для ввода только цифр
        setupInputFilters();

        btnChangePin.setOnClickListener(v -> {
            // Проверяем, что фрагмент все еще прикреплен
            if (!isAdded() || getContext() == null) return;

            String currentPin = etCurrentPin.getText().toString();
            String newPin = etNewPin.getText().toString();
            String confirmPin = etConfirmPin.getText().toString();

            if (validatePins(currentPin, newPin, confirmPin)) {
                if (pinStorage.getPin(userId).equals(currentPin)) {
                    pinStorage.savePin(userId, newPin);

                    // Закрываем фрагмент только если он прикреплен
                    if (isAdded() && getActivity() != null) {
                        navigateBack();
                        Toast.makeText(getContext(), "PIN changed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid current PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(ChangePinFragment.this).popBackStack();
    }

    private void setupInputFilters() {
        InputFilter[] digitFilter = new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                },
                new InputFilter.LengthFilter(4)
        };

        etCurrentPin.setFilters(digitFilter);
        etNewPin.setFilters(digitFilter);
        etConfirmPin.setFilters(digitFilter);
    }

    private boolean validatePins(String current, String newPin, String confirm) {
        // Проверяем, что фрагмент прикреплен
        if (!isAdded() || getContext() == null) return false;

        // Проверка 1: Длина всех PIN-кодов
        if (current.length() != 4) {
            Toast.makeText(getContext(), "Current PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPin.length() != 4) {
            Toast.makeText(getContext(), "New PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirm.length() != 4) {
            Toast.makeText(getContext(), "Confirm PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка 2: Соответствие нового PIN и подтверждения
        if (!newPin.equals(confirm)) {
            Toast.makeText(getContext(), "New PINs don't match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка 3: Новый PIN не должен совпадать с текущим
        if (newPin.equals(current)) {
            Toast.makeText(getContext(), "New PIN must be different from current PIN", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}