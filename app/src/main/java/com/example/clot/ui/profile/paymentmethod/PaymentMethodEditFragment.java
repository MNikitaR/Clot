package com.example.clot.ui.profile.paymentmethod;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.PaymentMethod;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Calendar;

public class PaymentMethodEditFragment extends Fragment {

    private TextInputLayout tilCardNumber, tilCardholder, tilExpiryMonth, tilExpiryYear, tilCvv;
    private TextInputEditText etCardNumber, etCardholder, etExpiryMonth, etExpiryYear, etCvv;
    private Button btnSavePaymentMethod;
    private CheckBox cbDefault;
    private String paymentMethodId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_method_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadPaymentMethodData();
        setupListeners();
    }

    private void initViews(View view) {
        etCardNumber = view.findViewById(R.id.etCardNumber);
        etCardholder = view.findViewById(R.id.etCardholder);
        etExpiryMonth = view.findViewById(R.id.etExpiryMonth);
        etExpiryYear = view.findViewById(R.id.etExpiryYear);
        etCvv = view.findViewById(R.id.etCvv);
        btnSavePaymentMethod = view.findViewById(R.id.btnSavePaymentMethod);
        cbDefault = view.findViewById(R.id.cbDefault);

        tilCardNumber = view.findViewById(R.id.tilCardNumber);
        tilCardholder = view.findViewById(R.id.tilCardholder);
        tilExpiryMonth = view.findViewById(R.id.tilExpiryMonth);
        tilExpiryYear = view.findViewById(R.id.tilExpiryYear);
        tilCvv = view.findViewById(R.id.tilCvv);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());

        // Установка фильтров и форматеров
        etCardNumber.addTextChangedListener(new CardNumberFormatWatcher());

        // Ограничение длины ввода
        etExpiryMonth.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
        etExpiryYear.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});

        // Автоматический переход между полями
        setupExpiryFieldsNavigation();
    }

    private void setupExpiryFieldsNavigation() {
        // Переход от месяца к году после ввода 2 цифр
        etExpiryMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    etExpiryYear.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Переход от года к CVV после ввода 2 цифр
        etExpiryYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    etCvv.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Переход при нажатии Enter/Next
        etExpiryYear.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etCvv.requestFocus();
                return true;
            }
            return false;
        });
    }

    private void loadPaymentMethodData() {
        Bundle args = getArguments();
        if (args != null) {
            paymentMethodId = args.getString("payment_method_id");
            if (paymentMethodId != null) {
                setupViewMode(args);
            } else {
                setupCreateMode();
            }
        } else {
            setupCreateMode();
        }
    }

    private void setupViewMode(Bundle args) {
        etCardNumber.setText("**** **** **** " + args.getString("card_last4"));
        etCardholder.setText(args.getString("cardholder_name"));

        int expMonth = args.getInt("card_exp_month");
        int expYear = args.getInt("card_exp_year") % 100;
        etExpiryMonth.setText(String.format("%02d", expMonth));
        etExpiryYear.setText(String.format("%02d", expYear));

        cbDefault.setChecked(args.getBoolean("is_default", false));

        setFieldsEnabled(false);
        cbDefault.setVisibility(View.GONE);
        btnSavePaymentMethod.setVisibility(View.GONE);
        tilCvv.setVisibility(View.GONE);
    }

    private void setupCreateMode() {
        setFieldsEnabled(true);
        cbDefault.setVisibility(View.VISIBLE);
        btnSavePaymentMethod.setVisibility(View.VISIBLE);
        tilCvv.setVisibility(View.VISIBLE);

        etCardNumber.setText("");
        etCardholder.setText("");
        etExpiryMonth.setText("");
        etExpiryYear.setText("");
        etCvv.setText("");
        cbDefault.setChecked(false);
    }

    private void setFieldsEnabled(boolean enabled) {
        etCardNumber.setEnabled(enabled);
        etCardholder.setEnabled(enabled);
        etExpiryMonth.setEnabled(enabled);
        etExpiryYear.setEnabled(enabled);
        etCvv.setEnabled(enabled);

        int bgColor = enabled ?
                ContextCompat.getColor(requireContext(), R.color.white) :
                ContextCompat.getColor(requireContext(), R.color.gray);

        tilCardNumber.setBoxBackgroundColor(bgColor);
        tilCardholder.setBoxBackgroundColor(bgColor);
        tilExpiryMonth.setBoxBackgroundColor(bgColor);
        tilExpiryYear.setBoxBackgroundColor(bgColor);
        tilCvv.setBoxBackgroundColor(bgColor);
    }

    private void setupListeners() {
        btnSavePaymentMethod.setOnClickListener(v -> savePaymentMethod());
    }

    private void savePaymentMethod() {
        if (!validateForm()) {
            return;
        }

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(paymentMethodId);
        paymentMethod.setUserId(DataBinding.getUuidUser());
        paymentMethod.setCardholderName(etCardholder.getText().toString().trim());
        paymentMethod.setDefault(cbDefault.isChecked());

        if (paymentMethodId == null) {
            String cardNumber = etCardNumber.getText().toString().replaceAll(" ", "");
            paymentMethod.setCardLast4(cardNumber.substring(cardNumber.length() - 4));

            paymentMethod.setCardExpMonth(Integer.parseInt(etExpiryMonth.getText().toString()));
            paymentMethod.setCardExpYear(2000 + Integer.parseInt(etExpiryYear.getText().toString()));

            paymentMethod.setCardBrand(detectCardBrand(cardNumber));
        }

        if (paymentMethod.isDefault()) {
            resetDefaultPaymentMethod(paymentMethod);
        } else {
            savePaymentMethodToServer(paymentMethod);
        }
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "Visa";
        if (cardNumber.startsWith("5")) return "MasterCard";
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) return "American Express";
        if (cardNumber.startsWith("6")) return "Discover";
        if (cardNumber.startsWith("22")) return "Mir";
        return "Unknown";
    }

    SupabaseClient supabaseClient = new SupabaseClient();

    private void resetDefaultPaymentMethod(PaymentMethod newDefault) {
        supabaseClient.resetDefaultPaymentMethod(newDefault.getUserId(), new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() ->
                        showError("Failed to reset default payment method")
                );
            }

            @Override
            public void onResponse(String responseBody) {
                savePaymentMethodToServer(newDefault);
            }
        });
    }

    private void savePaymentMethodToServer(PaymentMethod paymentMethod) {
        supabaseClient.savePaymentMethod(paymentMethod, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() ->
                        showError("Failed to save payment method: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(
                            requireContext(),
                            "Payment method saved",
                            Toast.LENGTH_SHORT
                    ).show();
                    NavHostFragment.findNavController(PaymentMethodEditFragment.this)
                            .navigateUp();
                });
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (paymentMethodId == null) {
            // Валидация номера карты
            String cardNumber = etCardNumber.getText().toString().replaceAll(" ", "");
            if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
                tilCardNumber.setError("Invalid card number");
                isValid = false;
            } else {
                tilCardNumber.setError(null);
            }

            // Валидация CVV
            String cvv = etCvv.getText().toString();
            if (cvv.length() < 3 || !cvv.matches("\\d+")) {
                tilCvv.setError("Invalid CVV");
                isValid = false;
            } else {
                tilCvv.setError(null);
            }
        }

        // Валидация месяца
        String expiryMonth = etExpiryMonth.getText().toString();
        if (expiryMonth.length() != 2 || !expiryMonth.matches("(0[1-9]|1[0-2])")) {
            tilExpiryMonth.setError("Invalid month");
            isValid = false;
        } else {
            tilExpiryMonth.setError(null);
        }

        // Валидация года
        String expiryYear = etExpiryYear.getText().toString();
        if (expiryYear.length() != 2 || !expiryYear.matches("\\d{2}")) {
            tilExpiryYear.setError("Invalid year");
            isValid = false;
        } else {
            tilExpiryYear.setError(null);
        }

        // Проверка срока действия
        if (isValid) {
            int month = Integer.parseInt(expiryMonth);
            int year = 2000 + Integer.parseInt(expiryYear);

            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1;

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                tilExpiryYear.setError("Card has expired");
                isValid = false;
            }
        }

        return isValid;
    }

    // Форматтер номера карты
    private static class CardNumberFormatWatcher implements TextWatcher {
        private boolean isFormatting;
        private int previousLength;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (!isFormatting) {
                previousLength = s.length();
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) return;
            isFormatting = true;

            String digits = s.toString().replaceAll("\\D", "");
            int digitsLength = digits.length();

            if (digitsLength > 16) {
                digits = digits.substring(0, 16);
                digitsLength = 16;
            }

            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digitsLength; i++) {
                if (i > 0 && i % 4 == 0) formatted.append(' ');
                formatted.append(digits.charAt(i));
            }

            if (!s.toString().equals(formatted.toString())) {
                s.replace(0, s.length(), formatted.toString());
            }

            isFormatting = false;
        }
    }
}