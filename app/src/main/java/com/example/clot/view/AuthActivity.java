package com.example.clot.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clot.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        // Устанавливаем фрагмент авторизации по умолчанию
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignInFragment())
                .commit();

        // Помечаем онбординг как завершенный
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply();
    }
}