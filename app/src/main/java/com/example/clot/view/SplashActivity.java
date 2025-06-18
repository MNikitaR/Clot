package com.example.clot.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.models.PinStorage;
import com.example.clot.onboarding.OnboardingActivity;
import com.example.clot.pin.PinFragment;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private static final String PREFS_NAME = "app_session";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Инициализация хранилищ
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        restoreAuthState();

        // Анимация названия приложения
        TextView appName = findViewById(R.id.appNameTextView);
        appName.setAlpha(0f);
        appName.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();

        // Задержка перед переходом на основной экран
        new Handler().postDelayed(this::checkUserState, SPLASH_DELAY);
    }

    //Восстанавливает состояние авторизации из SharedPreferences
    private void restoreAuthState() {
        String savedToken = sharedPreferences.getString("bearer_token", null);
        String savedUserId = sharedPreferences.getString("user_id", null);

        if (savedToken != null && savedUserId != null) {
            DataBinding.saveBearerToken(savedToken);
            DataBinding.saveUuidUser(savedUserId);
        }
    }

    //Сохраняет состояние авторизации в SharedPreferences
    private void saveAuthState() {
        sharedPreferences.edit()
                .putString("bearer_token", DataBinding.getBearerToken())
                .putString("user_id", DataBinding.getUuidUser())
                .apply();
    }

    private void checkUserState() {
        // Проверяем авторизацию пользователя
        if (isUserLoggedIn()) {
            // Пользователь авторизован - проверяем PIN
            checkPinStatus();
        } else {
            // Пользователь не авторизован - проверяем онбординг
            checkOnboardingStatus();
        }
    }

    private boolean isUserLoggedIn() {
        // Проверяем наличие токена авторизации
        String token = DataBinding.getBearerToken();
        return token != null && !token.isEmpty();
    }

    private void checkPinStatus() {
        String userId = DataBinding.getUuidUser();
        PinStorage pinStorage = new PinStorage(this);

        // Сохраняем состояние перед переходом
        saveAuthState();

        if (pinStorage.hasPin(userId)) {
            // PIN установлен - переход к вводу PIN
            startPinFragment(PinFragment.MODE_LOGIN, userId);
        } else {
            // PIN не установлен - переход к установке PIN
            startPinFragment(PinFragment.MODE_SETUP, userId);
        }
    }

    private void checkOnboardingStatus() {
        boolean onboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false);

        if (onboardingCompleted) {
            // Пользователь уже прошел онбординг - открываем авторизацию
            startActivity(new Intent(this, AuthActivity.class));
        } else {
            // Первый запуск - показываем онбординг
            startActivity(new Intent(this, OnboardingActivity.class));
        }
        finish();
    }

    private void startPinFragment(int mode, String userId) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("fragment", "pin");
        intent.putExtra("mode", mode);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }

}