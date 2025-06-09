package com.example.clot.onboarding;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.clot.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private OnboardingInteractionListener interactionListener;

    public interface OnboardingInteractionListener {
        void onSkipClicked();
        void onNextClicked(int position);
        void onFinishClicked();
    }

    public static OnboardingFragment newInstance(int position) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof OnboardingInteractionListener) {
            interactionListener = (OnboardingInteractionListener) getActivity();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int position = getArguments().getInt(ARG_POSITION);
        int layoutRes;

        switch (position) {
            case 0:
                layoutRes = R.layout.fragment_onboarding;
                break;
            case 1:
                layoutRes = R.layout.fragment_onboarding;
                break;
            case 2:
                layoutRes = R.layout.fragment_last_onboarding;
                break;
            default:
                layoutRes = R.layout.fragment_onboarding;
        }

        View view = inflater.inflate(layoutRes, container, false);
        setupView(view, position);
        return view;
    }

    private void setupView(View view, int position) {
        switch (position) {
            case 0:
                setupScreen1(view);
                break;
            case 1:
                setupScreen2(view);
                break;
            case 2:
                setupScreen3(view);
                break;
        }
    }

    private void setupScreen1(View view) {
        // Настройка первого экрана
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        Button btnNext = view.findViewById(R.id.btnNext);
        Button btnSkip = view.findViewById(R.id.btnSkip);
        ImageView image = view.findViewById(R.id.onboarding_image);

        title.setText(getResources().getString(R.string.first_onboarding_high));
        description.setText(getResources().getString(R.string.first_onboarding_low));
        image.setImageResource(R.drawable.first_onboarding);

// Критически важная проверка перед установкой слушателей
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (interactionListener != null) interactionListener.onNextClicked(0);
            });
        } else {
            Log.e("Onboarding", "btnNext not found in screen1");
        }

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                if (interactionListener != null) interactionListener.onSkipClicked();
            });
        } else {
            Log.e("Onboarding", "btnSkip not found in screen1");
        }
    }

    private void setupScreen2(View view) {
        // Настройка второго экрана
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        Button btnNext = view.findViewById(R.id.btnNext);
        Button btnSkip = view.findViewById(R.id.btnSkip);
        ImageView image = view.findViewById(R.id.onboarding_image);

        title.setText(getResources().getString(R.string.second_onboarding_high));
        description.setText(getResources().getString(R.string.second_onboarding_low));
        image.setImageResource(R.drawable.second_onboarding);

        btnNext.setOnClickListener(v -> {
            if (interactionListener != null) interactionListener.onNextClicked(1);
        });

        btnSkip.setOnClickListener(v -> {
            if (interactionListener != null) interactionListener.onSkipClicked();
        });
    }

    private void setupScreen3(View view) {
        // Настройка третьего экрана
        TextView title = view.findViewById(R.id.title);
        TextView question1 = view.findViewById(R.id.question1);
        TextView question2 = view.findViewById(R.id.question2);
        RadioGroup rgGender = view.findViewById(R.id.genderGroup);
        RadioButton radioMen = view.findViewById(R.id.radioMen);
        RadioButton radioWomen = view.findViewById(R.id.radioWomen);
        Button btnFinish = view.findViewById(R.id.btnFinish);

        title.setText(getResources().getString(R.string.tell_us_about_yourself));
        question1.setText(getResources().getString(R.string.who_do_you_shop_for));
        question2.setText(getResources().getString(R.string.how_old_are_you));

        Spinner ageSpinner = view.findViewById(R.id.ageSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.age_ranges,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpinner.setAdapter(adapter);

        // Установите первый элемент как подсказку
        ageSpinner.setSelection(0, false);

        // Установка обработчика изменений
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMen) {
                // Действия при выборе "Men"
                radioMen.setBackgroundResource(R.drawable.radio_button_background);
                radioWomen.setBackgroundResource(R.drawable.radio_button_background);
            } else if (checkedId == R.id.radioWomen) {
                // Действия при выборе "Women"
                radioMen.setBackgroundResource(R.drawable.radio_button_background);
                radioWomen.setBackgroundResource(R.drawable.radio_button_background);
            }
        });

        btnFinish.setOnClickListener(v -> {

            // Сохранение выбранных данных
            int selectedId = rgGender.getCheckedRadioButtonId();
            String gender = (selectedId == R.id.radioMen) ? "Men" : "Women";

            // Передача данных в активность
            if (interactionListener != null)
                interactionListener.onFinishClicked();
        });

    }
}