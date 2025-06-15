package com.example.clot.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clot.R;
import com.example.clot.onboarding.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);

        // Анимация названия приложения
        TextView appName = findViewById(R.id.appNameTextView);
        appName.setAlpha(0f);
        appName.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();

        // Задержка перед переходом на основной экран
        new Handler().postDelayed(this::checkAuthAndOnboarding, SPLASH_DELAY);
    }

    private void checkAuthAndOnboarding() {

        // Проверка авторизации пользователя
        if (isUserLoggedIn()) {
            startMainActivity();
        } else {
            checkOnboardingStatus();
        }
    }

    private boolean isUserLoggedIn() {
        String accessToken = sharedPreferences.getString("access_token", null);
        return accessToken != null && !accessToken.isEmpty();
    }

    private void checkOnboardingStatus() {
        boolean onboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false);
        Intent intent;

        if (onboardingCompleted) {
            // Пользователь уже прошел онбординг - открываем авторизацию
            intent = new Intent(this, AuthActivity.class);
        } else {
            // Первый запуск - показываем онбординг
            intent = new Intent(this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}