package com.example.clot.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clot.R;
import com.example.clot.onboarding.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Задержка перед переходом на основной экран
        new Handler().postDelayed(() -> {
            checkOnboardingStatus();
        }, 2000); // 2 секунды
        TextView appName = findViewById(R.id.appNameTextView);
        appName.setAlpha(0f);
        appName.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();
    }

    private void checkOnboardingStatus() {

        Intent intent;

        // Показываем онбординг
        intent = new Intent(this, OnboardingActivity.class);

        startActivity(intent);
        finish();
    }
}