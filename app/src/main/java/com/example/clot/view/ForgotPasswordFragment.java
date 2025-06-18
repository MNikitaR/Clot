package com.example.clot.view;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.AuthResponse;
import com.example.clot.pin.PinFragment;
import com.google.gson.Gson;

import java.io.IOException;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private Button btnContinue;
    private ImageButton btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Инициализация элементов
        etEmail = view.findViewById(R.id.etEmail);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnBack = view.findViewById(R.id.btnBack);
        // Настройка слушателей
        btnContinue.setOnClickListener(v -> resetPassword());
        btnBack.setOnClickListener(v -> navigateToBack());

        return view;
    }

    private void resetPassword() {

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        // Отправляем запрос на восстановление пароля
        supabaseClient.sendOtp(email, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("resetPassword:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("resetPassword:onResponse", responseBody);
                    navigateToOtpVerification(email);
                });
            }
        });
    }

    private void navigateToOtpVerification(String email) {
        Bundle args = new Bundle();
        args.putString("email", email);

        OtpVerificationFragment fragment = new OtpVerificationFragment();
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("otp_verification")
                .commit();
    }

    private void navigateToBack() {
        getParentFragmentManager().popBackStack();
    }
}
