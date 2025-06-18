package com.example.clot.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.clot.R;
import com.example.clot.pin.PinFragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        // Проверяем, нужно ли показывать PIN-фрагмент
        if (shouldShowPinFragment()) {
            showPinFragment();
        } else {
            showSignInFragment();
        }

        // Помечаем онбординг как завершенный
        markOnboardingCompleted();
    }

    private boolean shouldShowPinFragment() {
        Intent intent = getIntent();
        return intent != null &&
                "pin".equals(intent.getStringExtra("fragment")) &&
                intent.hasExtra("mode") &&
                intent.hasExtra("user_id");
    }

    private void showPinFragment() {
        Intent intent = getIntent();
        int mode = intent.getIntExtra("mode", PinFragment.MODE_LOGIN);
        String userId = intent.getStringExtra("user_id");

        PinFragment fragment = PinFragment.newInstance(mode, userId);
        replaceFragment(fragment);
    }

    private void showSignInFragment() {
        replaceFragment(new SignInFragment());
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void markOnboardingCompleted() {
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply();
    }
}