package com.example.clot.pin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clot.R;
import com.example.clot.models.PinStorage;
import com.example.clot.view.MainActivity;
import com.example.clot.view.SignInFragment;

public class PinFragment extends Fragment {
    // Режимы работы фрагмента
    public static final int MODE_LOGIN = 0;    // Режим ввода PIN для входа
    public static final int MODE_SETUP = 1;    // Режим установки нового PIN

    private static final String ARG_MODE = "mode";
    private static final String ARG_USER_ID = "user_id";

    private int currentMode;
    private String userId;
    private PinStorage pinStorage;

    private String enteredPin = "";
    private String confirmedPin = "";
    private boolean isConfirmationStage = false;

    // UI элементы
    private LinearLayout pinIndicators;
    private Button btnGo;
    private TextView tvTitle, tvSubtitle;
    private ImageButton btnBack;

    public static PinFragment newInstance(int mode, String userId) {
        PinFragment fragment = new PinFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentMode = getArguments().getInt(ARG_MODE, MODE_LOGIN);
            userId = getArguments().getString(ARG_USER_ID);
        }
        pinStorage = new PinStorage(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_pin, container, false);
        setupViews(view);
        setupListeners(view);
        updateUI();
        return view;
    }

    private void setupViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        pinIndicators = view.findViewById(R.id.pinIndicators);
        btnGo = view.findViewById(R.id.btnGo);
        btnBack = view.findViewById(R.id.btnSignIn);

        // Настройка внешнего вида в зависимости от режима
        if (currentMode == MODE_LOGIN) {
            tvTitle.setText(R.string.sign_in);
        } else {
            tvTitle.setText(R.string.set_up_pin);
        }
    }

    private void setupListeners(View view) {
        // Обработка цифровых кнопок
        int[] buttonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

        for (int id : buttonIds) {
            view.findViewById(id).setOnClickListener(v -> {
                Button button = (Button) v;
                handleDigitInput(button.getText().toString());
            });
        }

        // Кнопка удаления
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> deleteLastDigit());

        // Кнопка очистки
        view.findViewById(R.id.btn_clear).setOnClickListener(v -> clearAll());

        // Кнопка подтверждения
        btnGo.setOnClickListener(v -> handleGoButton());

        // Кнопка "Назад"
        btnBack.setOnClickListener(v -> navigateToSignIn());
    }

    private void handleDigitInput(String digit) {
        if (currentMode == MODE_LOGIN || !isConfirmationStage) {
            // Ввод основного PIN
            if (enteredPin.length() < 4) {
                enteredPin += digit;
            }
        } else {
            // Ввод подтверждения
            if (confirmedPin.length() < 4) {
                confirmedPin += digit;
            }
        }
        updateUI();
    }

    private void handleGoButton() {
        if (currentMode == MODE_LOGIN) {
            verifyPin();
        } else {
            if (isConfirmationStage) {
                confirmPin();
            } else {
                moveToConfirmationStage();
            }
        }
    }

    public void navigateToSignIn() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignInFragment())
                .addToBackStack(null)
                .commit();
    }

    private void verifyPin() {
        if (enteredPin.length() == 4) {
            String savedPin = pinStorage.getPin(userId);
            if (enteredPin.equals(savedPin)) {
                navigateToMain();
            } else {
                showError(getString(R.string.invalid_pin));
                clearAll();
            }
        }
    }

    private void moveToConfirmationStage() {
        if (enteredPin.length() == 4) {
            isConfirmationStage = true;
            updateUI();
        }
    }

    private void confirmPin() {
        if (enteredPin.equals(confirmedPin)) {
            pinStorage.savePin(userId, enteredPin);
            navigateToMain();
        } else {
            showError(getString(R.string.pin_mismatch));
            clearAll();
        }
    }

    private void deleteLastDigit() {
        if (isConfirmationStage) {
            if (!confirmedPin.isEmpty()) {
                confirmedPin = confirmedPin.substring(0, confirmedPin.length() - 1);
            }
        } else {
            if (!enteredPin.isEmpty()) {
                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
            }
        }
        updateUI();
    }

    private void clearAll() {
        enteredPin = "";
        confirmedPin = "";
        isConfirmationStage = false;
        updateUI();
    }

    private void updateUI() {
        // Обновление текста в зависимости от режима и этапа
        if (currentMode == MODE_LOGIN) {
            tvSubtitle.setText(R.string.please_enter_your_pin_code);
        } else {
            if (isConfirmationStage) {
                tvSubtitle.setText(R.string.confirm_pin);
            } else {
                tvSubtitle.setText(R.string.create_new_pin);
            }
        }

        // Обновление индикаторов
        updatePinIndicators();

        // Обновление кнопки Go
        if (currentMode == MODE_LOGIN) {
            btnGo.setEnabled(enteredPin.length() == 4);
            btnGo.setText(R.string.go);
        } else {
            if (isConfirmationStage) {
                btnGo.setEnabled(confirmedPin.length() == 4);
                btnGo.setText(R.string.confirm);
            } else {
                btnGo.setEnabled(enteredPin.length() == 4);
                btnGo.setText(R.string.continue_text);
            }
        }
    }

    private void updatePinIndicators() {
        pinIndicators.removeAllViews();

        // Определяем, какой PIN отображать
        String displayPin = isConfirmationStage ? confirmedPin : enteredPin;
        int totalIndicators = 4;

        for (int i = 0; i < totalIndicators; i++) {
            ImageView indicator = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    24, 24
            );
            params.setMargins(16, 0, 16, 0);
            indicator.setLayoutParams(params);

            if (i < displayPin.length()) {
                indicator.setImageResource(R.drawable.ic_pin_filled);
            } else {
                indicator.setImageResource(R.drawable.ic_pin_empty);
            }

            pinIndicators.addView(indicator);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        startActivity(new Intent(requireActivity(), MainActivity.class));
        requireActivity().finish();
    }
}