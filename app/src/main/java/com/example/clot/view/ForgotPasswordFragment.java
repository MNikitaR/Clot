package com.example.clot.view;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.clot.R;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private Button btnContinue, btnBacktoLogIn;
    private TextView tvMessage;
    private ImageButton btnBack;
    private ImageView img;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Инициализация элементов
        etEmail = view.findViewById(R.id.etEmail);
        btnContinue = view.findViewById(R.id.btnContinue);
        tvMessage = view.findViewById(R.id.tvMessage);
        btnBack = view.findViewById(R.id.btnBack);
        btnBacktoLogIn = view.findViewById(R.id.btnBackToLogin);
        img = view.findViewById(R.id.imgMessage);

        // Настройка слушателей
        btnContinue.setOnClickListener(v -> resetPassword());
        btnBack.setOnClickListener(v -> navigateToSignIn());
        btnBacktoLogIn.setOnClickListener(v -> navigateToSignIn());

        return view;
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            tvMessage.setVisibility(View.GONE);
            btnBacktoLogIn.setVisibility(View.GONE);
        } else {
            requireActivity().runOnUiThread(() -> {
                handleResetSuccess();
            });
        }
    }

    private void handleResetSuccess() {
        tvMessage.setVisibility(View.VISIBLE);
        btnBacktoLogIn.setVisibility(View.VISIBLE);
        img.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
    }

    public void navigateToSignIn() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignInFragment())
                .addToBackStack(null)
                .commit();
    }
}