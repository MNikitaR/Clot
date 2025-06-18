package com.example.clot.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.Profile;
import com.google.gson.Gson;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private ImageView profileImg;
    private Button btnEditProfile, btnSignOut;
    String avatar_url;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Инициализация элементов
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        profileImg = view.findViewById(R.id.profilePicture);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        // Загрузка данных пользователя
        loadUserProfile();

        // Обработчики кликов
        setupClickListeners(view);

        return view;
    }

    private void loadUserProfile() {
        // Получение данных пользователя из Supabase
        SupabaseClient supabaseClient = new SupabaseClient();

        supabaseClient.getData( new SupabaseClient.SBC_Callback() {

            @Override
            public void onFailure(IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadUserProfile:onFailure", e.getLocalizedMessage());
                });
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("loadUserProfile:onResponse", responseBody);
                    try {
                        parseAndDisplayProfile(responseBody);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(),
                                "Ошибка обработки данных",
                                Toast.LENGTH_SHORT).show();
                        Log.e("ProfileParse", "Error: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void parseAndDisplayProfile(String jsonResponse) {
        // Ответ приходит в виде массива, даже для одного элемента
        Gson gson = new Gson();
        Profile[] profiles = gson.fromJson(jsonResponse, Profile[].class);

        if (profiles != null && profiles.length > 0) {
            Profile profile = profiles[0];

            tvUserName.setText(profile.getUsername());
            tvUserEmail.setText(profile.getEmail());
            avatar_url = profile.getAvatar_url();
            // Загрузка аватара
            loadAvatar(avatar_url);
        } else {
            Toast.makeText(requireContext(),
                    "Данные профиля не найдены",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            profileImg.setImageResource(R.drawable.profilepicture);
            return;
        }

        // Сформируйте публичный URL
        String publicUrl = SupabaseClient.STORAGE_AVATAR + avatarUrl;

        Glide.with(this)
                .load(publicUrl)
                .placeholder(R.drawable.profilepicture)
                .error(R.drawable.profilepicture)
                .circleCrop()
                .into(profileImg);
    }

    private void setupClickListeners(View view) {

        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());

        /*// Кнопка выхода
        btnSignOut.setOnClickListener(v -> signOutUser());

        // Меню профиля
        view.findViewById(R.id.btnAddress).setOnClickListener(v -> navigateTo(AddressFragment.class));
        view.findViewById(R.id.btnCart).setOnClickListener(v -> navigateTo(CartFragment.class));
        view.findViewById(R.id.btnWishlist).setOnClickListener(v -> navigateTo(WishlistFragment.class));
        view.findViewById(R.id.btnPayment).setOnClickListener(v -> navigateTo(PaymentFragment.class));*/
    }

    // Метод для обновления UI после редактирования
    public void updateProfileData(String username, String email) {
        tvUserName.setText(username);
        tvUserEmail.setText(email);
    }

    // Обработка обратного вызова
    @Override
    public void onResume() {
        super.onResume();
        // Обновление данных при возврате
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        tvUserName.setText(prefs.getString("username", ""));
        tvUserEmail.setText(prefs.getString("email", ""));
    }

    private void navigateTo(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToEditProfile() {
        // Подготовка данных для передачи
        Bundle args = new Bundle();
        args.putString("username", tvUserName.getText().toString());
        args.putString("email", tvUserEmail.getText().toString());
        args.putString("avatar_url", avatar_url);


        // Переход к редактированию
        NavHostFragment.findNavController(ProfileFragment.this)
                .navigate(R.id.action_profileFragment_to_editProfileFragment, args);
    }

/*    private void signOutUser() {
        SupabaseClient.getInstance().signOut();
        startActivity(new Intent(getActivity(), AuthActivity.class));
        requireActivity().finish();
    }*/
}
