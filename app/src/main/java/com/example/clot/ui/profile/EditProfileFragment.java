package com.example.clot.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.clot.DataBinding;
import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.AuthResponse;
import com.example.clot.models.LoginRequest;
import com.example.clot.models.Profile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;

    private TextView accountNumber, etUsername, etEmail;
    private ImageView img;
    private Button btnChangePin, btnChangePassword;
    private ImageButton btnBack, btnUpdateImg;
    private String currentAvatarUrl;
    private Uri selectedImageUri;

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
        // Обработчик для кнопки обновления аватарки
        btnUpdateImg.setOnClickListener(v -> checkStoragePermission());
        // Кнопка назад
        btnBack.setOnClickListener(v -> navigateBack());
        btnChangePin.setOnClickListener(v ->
        NavHostFragment.findNavController(EditProfileFragment.this)
                .navigate(R.id.action_editProfileFragment_to_changePinFragment)
        );

        btnChangePassword.setOnClickListener(v ->
                showChangePasswordDialog()
        );
    }

    private void checkStoragePermission() {
        // Для Android 13+ (API 33) используем Photo Picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openPhotoPicker();
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private void openPhotoPicker() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(requireContext(),
                        "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Показать выбранное изображение
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(img);

            // Загрузить изображение на сервер
            uploadAvatarToSupabase();
        }
    }

    private void uploadAvatarToSupabase() {
        if (selectedImageUri == null) return;

        btnUpdateImg.setVisibility(View.GONE);

        // Получить ID пользователя
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            showError("User not authenticated");
            return;
        }

        // Создать уникальное имя файла
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg";

        // Сжать изображение перед загрузкой
        new CompressImageTask(fileName).execute(selectedImageUri);
    }

    private class CompressImageTask extends AsyncTask<Uri, Void, File> {
        private final String fileName;
        private Exception exception;

        public CompressImageTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected File doInBackground(Uri... uris) {
            try {
                return compressImage(uris[0]);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(File compressedFile) {
            if (compressedFile == null || exception != null) {
                showError("Image compression failed: " + (exception != null ? exception.getMessage() : ""));
                return;
            }

            // Загрузить сжатое изображение
            uploadCompressedAvatar(compressedFile, fileName);
        }
    }

    private File compressImage(Uri uri) throws IOException {
        InputStream input = requireContext().getContentResolver().openInputStream(uri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(input);

        // Рассчитать размеры
        int maxDimension = 1024;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        if (width > maxDimension || height > maxDimension) {
            float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);

            originalBitmap = Bitmap.createScaledBitmap(
                    originalBitmap, width, height, true
            );
        }

        // Сохранить во временный файл
        File outputFile = File.createTempFile("compressed_", ".jpg", requireContext().getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        // Сжать и сохранить
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        outputStream.close();

        return outputFile;
    }

    private void uploadCompressedAvatar(File file, String fileName) {
        // Удалить старую аватарку
        deleteOldAvatar();

        // Загрузить в Supabase Storage
        SupabaseClient.uploadAvatar(file, fileName, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                showError("Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                // Обновить URL аватарки в профиле пользователя
                updateProfileAvatar(fileName);
            }
        });
    }

    private void deleteOldAvatar() {
        if (currentAvatarUrl == null || currentAvatarUrl.isEmpty()) return;

        SupabaseClient.deleteAvatar(currentAvatarUrl, new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("AvatarDelete", "Failed to delete old avatar: " + e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                Log.i("AvatarDelete", "Old avatar deleted");
            }
        });
    }

    private void updateProfileAvatar(String fileName) {
        String userId = DataBinding.getUuidUser();
        if (userId == null || userId.isEmpty()) {
            return;
        }

        // Создать JSON с новым URL аватарки
        JSONObject payload = new JSONObject();
        try {
            payload.put("avatar_url", fileName);
        } catch (JSONException e) {
            Log.e("EditProfile", "JSON error: " + e.getMessage());
        }

        Profile profile = new Profile();
        // Отправить запрос на обновление
        SupabaseClient.updateURL(payload.toString(), new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                showError("Profile update failed: " + e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                requireActivity().runOnUiThread(() -> {

                    // Сохранить новый URL локально
                    currentAvatarUrl = fileName;
                    profile.setAvatar_url(fileName);

                    // Обновить ViewModel
                    ProfileViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
                    Profile currentProfile = viewModel.getUserProfile().getValue();
                    if (currentProfile != null) {
                        currentProfile.setAvatar_url(fileName);
                        viewModel.setUserProfile(currentProfile);
                    }

                    Toast.makeText(requireContext(),
                            "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void navigateBack() {
        // Вернуться назад
        NavHostFragment.findNavController(EditProfileFragment.this).popBackStack();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ссылки на элементы
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        TextView tvForgotPassword = dialogView.findViewById(R.id.tvForgotPassword);

        SupabaseClient supabaseClient = new SupabaseClient();

        // Обработка смены пароля
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();
            String email = etEmail.getText().toString();

            LoginRequest loginRequest = new LoginRequest(email, currentPassword);

            if (validatePasswords(currentPassword, newPassword, confirmPassword)) {
                // Сначала проверяем текущий пароль
                supabaseClient.loginUser(loginRequest, new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e("reauth:onFailure", e.getLocalizedMessage());
                        });
                    }

                    @Override
                    public void onResponse(String responseBody) {

                        Gson gson = new Gson();
                        AuthResponse auth = gson.fromJson(responseBody, AuthResponse.class);

                        DataBinding.saveBearerToken("Bearer " + auth.getAccess_token());
                        DataBinding.saveUuidUser(auth.getUser().getId());

                        // После успешной переаутентификации обновляем пароль
                        supabaseClient.updatePassword(newPassword, new SupabaseClient.SBC_Callback() {
                            @Override
                            public void onFailure(IOException e) {
                                requireActivity().runOnUiThread(() -> {
                                    Log.e("update:onFailure", e.getLocalizedMessage());
                                });
                            }

                            @Override
                            public void onResponse(String responseBody) {
                                requireActivity().runOnUiThread(() -> {
                                    Log.e("update:onResponse", responseBody);
                                });
                            }
                        });
                    }
                });
            }
        });

        // Обработка "Forgot Password?"
        tvForgotPassword.setOnClickListener(v -> {
            Profile profile = new Profile();
            String email = profile.getEmail();
            supabaseClient.resetPassword(email, new SupabaseClient.SBC_Callback() {
                @Override
                public void onFailure(IOException e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e("reset:onFailure", e.getLocalizedMessage());
                    });
                }

                @Override
                public void onResponse(String responseBody) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e("reset:onResponse", responseBody);
                    });
                }
            });
        });
    }

    private boolean validatePasswords(String current, String newPass, String confirm) {
        if (TextUtils.isEmpty(current)) {
            Toast.makeText(requireContext(), "Enter current password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPass)) {
            Toast.makeText(requireContext(), "Enter new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(confirm)) {
            Toast.makeText(requireContext(), "Confirm your new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPass.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}