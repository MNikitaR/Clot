package com.example.clot.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.AuthResponse;
import com.example.clot.models.LoginRequest;
import com.google.gson.Gson;

import java.io.IOException;

public class SignInFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnContinueEmail, btnSignIn;
    private TextView tvCreateAccount, tvForgotPassword;
    private View socialLoginSection, passwordSection, emailSection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Инициализация элементов
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnContinueEmail = view.findViewById(R.id.btnContinueEmail);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        tvCreateAccount = view.findViewById(R.id.tvCreateAccount);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        socialLoginSection = view.findViewById(R.id.socialLoginSection);
        passwordSection = view.findViewById(R.id.passwordSection);
        emailSection = view.findViewById(R.id.emailSection);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        // Переход к вводу пароля
        btnContinueEmail.setOnClickListener(v -> validateEmail());

        // Кнопка входа
        btnSignIn.setOnClickListener(v -> validatePassword());

        // Переход к регистрации
        tvCreateAccount.setOnClickListener(v -> navigateToSignUp());

        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        // Валидация email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnContinueEmail.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Валидация email
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSignIn.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            return;
        }

        // Скрыть секции соц. кнопки и email, показать поле пароля
        socialLoginSection.setVisibility(View.GONE);
        emailSection.setVisibility(View.GONE);
        passwordSection.setVisibility(View.VISIBLE);
    }

    private void validatePassword() {
        String password = etPassword.getText().toString().trim();

        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            return;
        }

        // Реализация логина
        performLogin(etEmail.getText().toString().trim(), password);
    }

    private void performLogin(String email, String password) {

        SupabaseClient supabaseClient = new SupabaseClient();
        LoginRequest loginRequest = new LoginRequest(email, password);

        supabaseClient.loginUser(loginRequest, new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("performLogin:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("performLogin:onResponse", responseBody);
                    Gson gson = new Gson();
                    AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                    DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
                    DataBinding.saveUuidUser(auth.getUser().getId());

                    startActivity(new Intent(getActivity(), MainActivity.class));
                    Log.e("performLogin:onResponse", auth.getUser().getId());
                });
            }
        });
    }

    private void navigateToSignUp() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignUpFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToForgotPassword() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ForgotPasswordFragment())
                .addToBackStack("forgot_password")
                .commit();
    }
}