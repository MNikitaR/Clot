package com.example.clot.ui.profile.address;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Address;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

public class AddressEditFragment extends Fragment {

    private TextInputEditText etStreetAddress, etCity, etState, etZipCode;
    private Button btnSaveAddress;
    private CheckBox cbDefaultAddress;
    private String addressId;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Получаем ID текущего пользователя
        userId = DataBinding.getUuidUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        initViews(view);

        // Загрузка данных адреса (если редактирование)
        loadAddressData();

        // Настройка обработчиков
        setupListeners();
    }

    private void initViews(View view) {
        etStreetAddress = view.findViewById(R.id.etStreetAddress);
        etCity = view.findViewById(R.id.etCity);
        etState = view.findViewById(R.id.etState);
        etZipCode = view.findViewById(R.id.etZipCode);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);
        cbDefaultAddress = view.findViewById(R.id.cbDefaultAddress);

        // Кнопка назад
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadAddressData() {
        Bundle args = getArguments();
        if (args != null) {
            addressId = args.getString("address_id");
            if (addressId != null && !addressId.isEmpty()) {
                // Режим редактирования: загружаем данные адреса
                etStreetAddress.setText(args.getString("street", ""));
                etCity.setText(args.getString("city", ""));
                etState.setText(args.getString("state", ""));
                etZipCode.setText(args.getString("zip_code", ""));
                cbDefaultAddress.setChecked(args.getBoolean("is_default", false));
            }
        }
    }

    private void setupListeners() {
        btnSaveAddress.setOnClickListener(v -> saveAddress());

        // Автозаполнение штата при вводе почтового индекса
        etZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 5) {
                    lookupStateByZip(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void lookupStateByZip(String zipCode) {
        // Здесь можно реализовать поиск штата по почтовому индексу
        // Например, через внешний API или локальную базу данных
        if ("95112".equals(zipCode)) {
            etState.setText("CA");
        } else if ("10001".equals(zipCode)) {
            etState.setText("NY");
        }
        // В реальном приложении это будет вызов API
    }

    private void saveAddress() {
        // Проверяем валидность введенных данных
        if (!validateForm()) {
            return;
        }

        // Создаем объект адреса
        Address address = new Address(
                addressId,
                etStreetAddress.getText().toString().trim(),
                etCity.getText().toString().trim(),
                etState.getText().toString().trim(),
                etZipCode.getText().toString().trim(),
                cbDefaultAddress.isChecked()
        );

        // Если это адрес по умолчанию, сначала сбрасываем предыдущий
        if (address.isDefault()) {
            resetDefaultAddress(address);
        } else {
            saveAddressToServer(address);
        }
    }

    private void resetDefaultAddress(Address newAddress) {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.resetDefaultAddress(userId, new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        showError("Failed to reset default address");
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        saveAddressToServer(newAddress);
                    }
                });
    }

    private void saveAddressToServer(Address address) {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.saveAddress(address, new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        showError("Failed to save address: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(
                                    requireContext(),
                                    "Address saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Возвращаемся назад
                            NavHostFragment.findNavController(AddressEditFragment.this)
                                    .navigateUp();
                        });
                    }
                });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Валидация улицы
        if (etStreetAddress.getText().toString().trim().isEmpty()) {
            etStreetAddress.setError("Street address is required");
            isValid = false;
        }

        // Валидация города
        if (etCity.getText().toString().trim().isEmpty()) {
            etCity.setError("City is required");
            isValid = false;
        }

        // Валидация штата
        if (etState.getText().toString().trim().isEmpty()) {
            etState.setError("State is required");
            isValid = false;
        } else if (etState.getText().toString().trim().length() != 2) {
            etState.setError("Use 2-letter state code");
            isValid = false;
        }

        // Валидация почтового индекса
        if (etZipCode.getText().toString().trim().isEmpty()) {
            etZipCode.setError("Zip code is required");
            isValid = false;
        } else if (etZipCode.getText().toString().trim().length() < 5) {
            etZipCode.setError("Invalid zip code");
            isValid = false;
        }

        return isValid;
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        );
    }
}