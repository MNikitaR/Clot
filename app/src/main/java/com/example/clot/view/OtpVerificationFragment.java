package com.example.clot.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.clot.R;
import com.example.clot.SupabaseClient;

import java.io.IOException;

public class OtpVerificationFragment extends Fragment {

    private String email;
    private EditText[] otpInputs = new EditText[6];
    private Button verifyButton;
    private TextView resendTextView, emailTextView;
    private CountDownTimer countDownTimer;
    private ImageButton btnBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString("email");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        emailTextView = view.findViewById(R.id.tvEmail);
        emailTextView.setText(R.string.please_check_your_email + email + R.string.to_see);

        // Инициализация полей OTP
        otpInputs[0] = view.findViewById(R.id.otp1);
        otpInputs[1] = view.findViewById(R.id.otp2);
        otpInputs[2] = view.findViewById(R.id.otp3);
        otpInputs[3] = view.findViewById(R.id.otp4);
        otpInputs[4] = view.findViewById(R.id.otp5);
        otpInputs[5] = view.findViewById(R.id.otp6);

        setupOtpInputs();

        verifyButton = view.findViewById(R.id.verifyButton);
        resendTextView = view.findViewById(R.id.resendTextView);
        btnBack = view.findViewById(R.id.btnBack);

        verifyButton.setOnClickListener(v -> verifyOtp());
        btnBack.setOnClickListener(v -> navigateToBack());
        startCountdown();

        return view;
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() == 1 && index == otpInputs.length - 1) {
                        verifyOtp();
                    }
                }

                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            // Обработка удаления символов
            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void startCountdown() {
        resendTextView.setEnabled(false);
        resendTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        countDownTimer = new CountDownTimer(90000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                resendTextView.setText(String.format("Resend code to %02d:%02d", seconds / 60, seconds % 60));
            }

            public void onFinish() {
                resendTextView.setText("Resend code");
                resendTextView.setEnabled(true);
                resendTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
                resendTextView.setOnClickListener(v -> resendOtp());
            }
        }.start();
    }

    private void verifyOtp() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText input : otpInputs) {
            otpBuilder.append(input.getText().toString());
        }
        String otp = otpBuilder.toString();

        if (otp.length() != 6) {
            Toast.makeText(requireContext(), "Please enter full OTP code", Toast.LENGTH_SHORT).show();
            return;
        }

        navigateToResetPassword(email, otp);

        /*SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.verifyOtp(email, otp, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("verifyOtp:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String response) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("verifyOtp:onResponse", response);
                    navigateToResetPassword(email);
                });
            }
        });*/
    }

    private void resendOtp() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.sendOtp(email, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("resendOtp:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    startCountdown();
                    Log.e("resendOtp:onResponse", responseBody);
                });
            }
        });
    }

    private void navigateToResetPassword(String email, String otp) {
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("otp", otp);

        ResetPasswordFragment fragment = new ResetPasswordFragment();
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("reset_password")
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void navigateToBack() {
        getParentFragmentManager().popBackStack();
    }
}