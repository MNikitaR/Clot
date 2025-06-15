package com.example.clot.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clot.R;
import com.example.clot.view.AuthActivity;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class OnboardingActivity extends AppCompatActivity
        implements OnboardingFragment.OnboardingInteractionListener {

    private ViewPager2 viewPager;
    private DotsIndicator dotsIndicator;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Привязываем индикатор к ViewPager
        dotsIndicator.setViewPager2(viewPager);

        // Обработчик переключения страниц
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Дополнительная логика при смене страницы
            }
        });
    }

    @Override
    public void onSkipClicked() {
        // Переход сразу на последний экран
        viewPager.setCurrentItem(adapter.getItemCount() - 1, true);
    }

    @Override
    public void onNextClicked(int position) {
        // Переход к следующему экрану
        viewPager.setCurrentItem(position + 1, true);
    }

    @Override
    public void onFinishClicked() {

        // Переходим к следующему шагу (авторизация или главный экран)
        startActivity(new Intent(this, AuthActivity.class));

        finish();
    }

    private void saveUserPreferences() {
        // Сохранение данных в SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("onboarding_complete", true)
                .apply();
    }
}