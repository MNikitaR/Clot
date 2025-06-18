package com.example.clot.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;

public class EditProfileFragment extends Fragment {

    private TextView accountNumber, etUsername, etEmail;
    private ImageView img;
    private Button btnChangePin, btnChangePassword;
    private ImageButton btnBack, btnUpdateImg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Инициализация элементов
        accountNumber = view.findViewById(R.id.tvAcountNumber);
        etUsername = view.findViewById(R.id.tvUserName);
        etEmail = view.findViewById(R.id.tvUserEmail);
        img = view.findViewById(R.id.profile_picture);
        btnBack = view.findViewById(R.id.btnBack);
        btnChangePin = view.findViewById(R.id.btnChPin);
        btnChangePassword = view.findViewById(R.id.btnChPas);
        btnUpdateImg = view.findViewById(R.id.btnUpdateImg);

        // Загрузка данных пользователя
        loadUserData();

        // Обработчики событий
        setupListeners();

        return view;
    }

    private void loadUserData() {
        // Получение данных из аргументов
        if (getArguments() != null) {
            String acNumber = DataBinding.getUuidUser();
            String username = getArguments().getString("username", "");
            String email = getArguments().getString("email", "");
            String url = getArguments().getString("avatar_url", "");

            accountNumber.setText(acNumber);
            etUsername.setText(username);
            etEmail.setText(email);

            loadAvatar(url);
        }
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            img.setImageResource(R.drawable.profilepicture);
            return;
        }

        // Сформируйте публичный URL
        String publicUrl = SupabaseClient.STORAGE_AVATAR + avatarUrl;

        Glide.with(this)
                .load(publicUrl)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(img);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> navigateBack());
/*        btnChangePin.setOnClickListener(v -> navigateToChangePin());
        btnChangePassword.setOnClickListener(v -> navigateToChangePassword());*/
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(EditProfileFragment.this).popBackStack();
    }

/*    private void navigateToChangePin() {
        // Переход к смене PIN
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_editProfileFragment_to_changePinFragment);
    }

    private void navigateToChangePassword() {
        // Переход к смене пароля
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_editProfileFragment_to_changePasswordFragment);
    }*/
}