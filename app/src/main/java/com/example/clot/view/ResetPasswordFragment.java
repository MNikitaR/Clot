package com.example.clot.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.OTPRequest;

import java.io.IOException;

public class ResetPasswordFragment extends Fragment {

    private EditText passwordEditText, confirmPasswordEditText;
    private Button resetButton;
    private String email, otp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString("email");
            otp = getArguments().getString("otp");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        resetButton = view.findViewById(R.id.resetButton);

        resetButton.setOnClickListener(v -> resetPassword());

        return view;
    }

    private void resetPassword() {
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.resetPasswordWithOTP(email, otp, password, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("resetPassword:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String response) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("resetPassword:onResponse", response);

                    navigateToSignIn();
                });
            }
        });
    }

    private void navigateToSignIn() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignInFragment())
                .addToBackStack(null)
                .commit();
    }
}