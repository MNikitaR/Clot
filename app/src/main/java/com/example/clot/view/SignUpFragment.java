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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.AuthResponse;
import com.example.clot.models.LoginRequest;
import com.example.clot.models.ProfileUpdate;
import com.google.gson.Gson;

import java.io.IOException;

public class SignUpFragment extends Fragment {

    private EditText etFirstName, etLastName, etEmail, etPassword;
    private Button btnContinue;
    private ImageButton btnSignIn;
    TextView tvForgotPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> validateRegistration());

        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        btnSignIn.setOnClickListener(view -> navigateToSignIn());

        // Валидация полей в реальном времени
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFirstName.addTextChangedListener(textWatcher);
        etLastName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
    }

    private void validateFields() {
        boolean isValid = !TextUtils.isEmpty(etFirstName.getText()) &&
                !TextUtils.isEmpty(etLastName.getText()) &&
                !TextUtils.isEmpty(etEmail.getText()) &&
                !TextUtils.isEmpty(etPassword.getText());

        btnContinue.setEnabled(isValid);
    }

    private void validateRegistration() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("Введите имя");
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Введите фамилию");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректынй email");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            return;
        }

        // Реализация регистрации
        performRegistration(firstName, lastName, email, password);
    }

    public void updateProfile(String full_name) {
        SupabaseClient supabaseClient = new SupabaseClient();
        ProfileUpdate profileUpdate = new ProfileUpdate(full_name);
        supabaseClient.updateProfile(profileUpdate, new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("updateProfile:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("updateProfile:onResponse", responseBody);
                    startActivity(new Intent(getActivity(), MainActivity.class));
                });
            }
        });
    }

    private void performRegistration(String firstName, String lastName, String email, String password) {

        SupabaseClient supabaseClient = new SupabaseClient();
        LoginRequest loginRequest = new LoginRequest(email, password);

        supabaseClient.signUpUser(loginRequest, new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("performRegistration:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("performRegistration:onResponse", responseBody);
                    Gson gson = new Gson();
                    AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                    DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
                    DataBinding.saveUuidUser(auth.getUser().getId());

                    updateProfile(firstName + " " + lastName);
                    Log.e("performRegistration:onResponse", auth.getUser().getId());
                });
            }
        });
    }

    private void navigateToSignIn() {
        getParentFragmentManager().popBackStack();
    }

    private void navigateToForgotPassword() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ForgotPasswordFragment())
                .addToBackStack("forgot_password")
                .commit();
    }
}