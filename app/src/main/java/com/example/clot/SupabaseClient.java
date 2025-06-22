package com.example.clot;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.clot.models.Address;
import com.example.clot.models.LoginRequest;
import com.example.clot.models.PaymentMethod;
import com.example.clot.models.ProfileUpdate;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    public interface SBC_Callback{
        public void onFailure(IOException e);
        public void onResponse(String responseBody);
    }

    public static String DOMAIN_NAME = "https://iexkqvbyaqdhburzqphy.supabase.co/";
    public static String AUTH_PATH = "auth/v1/";
    public static String REST_PATH = "rest/v1/";
    public static String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlleGtxdmJ5YXFkaGJ1cnpxcGh5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAyMzY0MjIsImV4cCI6MjA2NTgxMjQyMn0.0HuekEOytqhI7Lj1JytIeUFAuV4ogx_Fk2FH8sDogD0";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final String STORAGE_AVATAR = DOMAIN_NAME + "storage/v1/object/public/avatars/";
    public static final String STORAGE_CATEGORY = DOMAIN_NAME + "storage/v1/object/public/categoryimg/";
    public static final String STORAGE_PRODUCT = DOMAIN_NAME + "storage/v1/object/public/productimg/";

    static OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // Авторизация пользователя
    public void loginUser(LoginRequest loginRequest, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        String json = gson.toJson(loginRequest);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "token?grant_type=password")
                .method("POST", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Регистрация пользователя
    public void signUpUser(LoginRequest loginRequest, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        String json = gson.toJson(loginRequest);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "signup")
                .method("POST", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    public void updateProfile(ProfileUpdate profile, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        String json = gson.toJson(profile);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + DataBinding.getUuidUser())
                .method("PATCH", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Отправка OTP
    public void sendOtp(String email, final SBC_Callback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
        } catch (Exception e) {
            callback.onFailure(new IOException("Error creating data: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "recover")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    public void resetPasswordWithOTP(String email, String token, String newPassword, SBC_Callback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("token", token);
            payload.put("type", "recovery");
            payload.put("password", newPassword);
        } catch (JSONException e) {
            callback.onFailure(new IOException("JSON error: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "verify")
                .put(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Password updated successfully");
                } else {
                    String errorBody = response.body().string();
                    callback.onFailure(new IOException("Error " + response.code() + ": " + errorBody));
                }
            }
        });
    }

    // Получение данных пользователя
    public void getData(SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "user_profile?user_id=eq." + DataBinding.getUuidUser())
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Метод для загрузки аватарки
    public static void uploadAvatar(File file, String fileName, SBC_Callback callback) {
        try {
            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody body = RequestBody.create(mediaType, file);

            Request request = new Request.Builder()
                    .url(STORAGE_AVATAR + fileName)
                    .put(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + DataBinding.getBearerToken())
                    .addHeader("Content-Type", "image/jpeg")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onResponse("Avatar uploaded");
                    } else {
                        String errorBody = response.body().string();
                        String errorMessage = parseStorageError(errorBody);
                        callback.onFailure(new IOException(errorMessage));
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(new IOException("File error: " + e.getMessage()));
        }
    }

    private static String parseStorageError(String errorBody) {
        try {
            JSONObject errorJson = new JSONObject(errorBody);
            return errorJson.optString("message", "Upload failed");
        } catch (JSONException e) {
            return "Upload error: " + errorBody;
        }
    }

    // Метод для удаления аватарки
    public static void deleteAvatar(String fileName, SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(STORAGE_AVATAR + fileName)
                .delete()
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Avatar deleted");
                } else {
                    callback.onFailure(new IOException("Delete failed: " + response.code()));
                }
            }
        });
    }

    public static void updateURL(String jsonPayload, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonPayload);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + DataBinding.getUuidUser())
                .method("PATCH", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Метод смены пароля
    public void updatePassword( String newPassword, SBC_Callback callback) {
        // Создаем объект запроса
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, newPassword);

        // Формируем HTTP-запрос
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "user")
                .method("PUT", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .build();

        // Выполняем запрос асинхронно
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Метод для восстановления пароля
    public void resetPassword(String email, SBC_Callback callback) {
        String json = "{\"email\":\"" + email + "\"}";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "recover")
                .method("POST", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    public void getUserAddresses(SBC_Callback callback) {
        String userId = DataBinding.getUuidUser();
        String url = DOMAIN_NAME + "/rest/v1/addresses?user_id=eq." + userId;

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    public void saveAddress(Address address, SBC_Callback callback) {
        String url;
        String method;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", DataBinding.getUuidUser());
            jsonBody.put("street", address.getStreet());
            jsonBody.put("city", address.getCity());
            jsonBody.put("state", address.getState());
            jsonBody.put("zip_code", address.getZipCode());
            jsonBody.put("is_default", address.isDefault());

            if (address.getId() != null && !address.getId().isEmpty()) {
                url = DOMAIN_NAME + "/rest/v1/addresses?id=eq." + address.getId();
                method = "PATCH";
            } else {
                url = DOMAIN_NAME + "/rest/v1/addresses";
                method = "POST";
            }
        } catch (JSONException e) {
            callback.onFailure(new IOException("JSON error"));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=representation")
                .method(method, body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Server error: " + response.code()));
                }
            }
        });
    }

    public void resetDefaultAddress(String userId, SBC_Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("is_default", false);
        } catch (JSONException e) {
            callback.onFailure(new IOException("JSON error"));
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + "/rest/v1/addresses?user_id=eq." + userId)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=minimal")
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Success");
                } else {
                    callback.onFailure(new IOException(
                            "Reset default error: " + response.code() + " - " + response.body().string()
                    ));
                }
            }
        });
    }

    public void deleteAddress(String addressId, SBC_Callback callback) {
        String url = DOMAIN_NAME + "/rest/v1/addresses?id=eq." + addressId;

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=minimal")
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Success");
                } else {
                    callback.onFailure(new IOException(
                            "Delete error: " + response.code() + " - " + response.body().string()
                    ));
                }
            }
        });
    }

    public void getUserPaymentMethods(SBC_Callback callback) {
        String userId = DataBinding.getUuidUser();
        String url = DOMAIN_NAME + "/rest/v1/payment_methods?user_id=eq." + userId;

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Server error: " + response.code()));
                }
            }
        });
    }

    public void savePaymentMethod(PaymentMethod paymentMethod, SBC_Callback callback) {
        String url;
        String method;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", paymentMethod.getUserId());
            jsonBody.put("card_last4", paymentMethod.getCardLast4());
            jsonBody.put("card_exp_month", paymentMethod.getCardExpMonth());
            jsonBody.put("card_exp_year", paymentMethod.getCardExpYear());
            jsonBody.put("cardholder_name", paymentMethod.getCardholderName());
            jsonBody.put("card_brand", paymentMethod.getCardBrand());
            jsonBody.put("is_default", paymentMethod.isDefault());

            if (paymentMethod.getId() != null && !paymentMethod.getId().isEmpty()) {
                url = DOMAIN_NAME + "/rest/v1/payment_methods?id=eq." + paymentMethod.getId();
                method = "PATCH";
            } else {
                url = DOMAIN_NAME + "/rest/v1/payment_methods";
                method = "POST";
            }
        } catch (JSONException e) {
            callback.onFailure(new IOException("JSON error"));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=minimal")
                .method(method, body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Save error: " + response.code()));
                }
            }
        });
    }

    public void deletePaymentMethod(String paymentMethodId, SBC_Callback callback) {
        String url = DOMAIN_NAME + "/rest/v1/payment_methods?id=eq." + paymentMethodId;

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=minimal")
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Success");
                } else {
                    callback.onFailure(new IOException("Delete error: " + response.code()));
                }
            }
        });
    }

    public void resetDefaultPaymentMethod(String userId, SBC_Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("is_default", false);
        } catch (JSONException e) {
            callback.onFailure(new IOException("JSON error"));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + "/rest/v1/payment_methods?user_id=eq." + userId)
                .header("apikey", API_KEY)
                .header("Authorization", DataBinding.getBearerToken())
                .header("Prefer", "return=minimal")
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Success");
                } else {
                    callback.onFailure(new IOException("Reset default error: " + response.code()));
                }
            }
        });
    }

    // Метод для выхода пользователя
    public void signOut(String accessToken, SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "logout")
                .post(RequestBody.create("", null)) // POST-запрос без тела
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Server error: " + response.code()));
                }
            }
        });
    }

    // Получение категорий
    public void fetchCategories( SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "categories?select=*")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Получение гендер
    public void fetchGenders( SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "genders?select=*")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Получение товаров с изображениями
    public void fetchProducts(SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "products?select=*")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    public void searchProducts(String query,
                              /* Set<String> colors,
                               List<String> genders,
                               double minPrice,
                               double maxPrice,
                               Set<String> sizes,*/
                               SBC_Callback callback) {

        new Thread(() -> {
            try {
                // Базовый URL с основными параметрами
                String url = DOMAIN_NAME + REST_PATH + "products?select=*";

                // Добавляем поиск по названию
                if (!query.isEmpty()) {
                    url += "&name=ilike.*" + URLEncoder.encode(query, "UTF-8") + "*";
                }

/*
                // Для цветов
                if (!colors.isEmpty()) {
                    String colorList = colors.stream()
                            .map(c -> "\"" + c + "\"")
                            .collect(Collectors.joining(","));
                    url += "&product_variants.color_id=in.(" + colorList + ")";
                }

                // Для размеров
                if (!sizes.isEmpty()) {
                    String sizeList = sizes.stream()
                            .map(s -> "\"" + s + "\"")
                            .collect(Collectors.joining(","));
                    url += "&product_variants.size_id=in.(" + sizeList + ")";
                }

                // Фильтр по полу
                if (!genders.isEmpty()) {
                    // Формируем список гендеров в формате SQL
                    String genderList = TextUtils.join(",",
                            genders.stream().map(g -> "\"" + g + "\"").collect(Collectors.toList()));

                    url += "&gender=in.(" + genderList + ")";
                }

                // Фильтр по цене
                url += "&price=gte." + String.format(Locale.US, "2d", minPrice)
                        + "&price=lte." + String.format(Locale.US, "2d", maxPrice);

                // Фильтр по наличию товара
                url += "&product_variants.stock=gt.0";
*/

                // Формируем запрос
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", DataBinding.getBearerToken())
                        .build();

                // Выполняем запрос
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.onFailure(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            callback.onResponse(responseBody);
                        } else {
                            callback.onFailure(new IOException("Server error: " + response.code()));
                        }
                    }
                });

            } catch (Exception e) {
                callback.onFailure(new IOException("URL build error: " + e.getMessage()));
            }
        }).start();
    }

    // Получение цвета
    public void fetchColor(SBC_Callback callback) {
        Request request = new Request.Builder()
                .url("https://iexkqvbyaqdhburzqphy.supabase.co/rest/v1/colors?select=*")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Получение размеров
    public void fetchSizes(SBC_Callback callback) {
        Request request = new Request.Builder()
                .url("https://iexkqvbyaqdhburzqphy.supabase.co/rest/v1/sizes?select=*")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Получение товаров по категории
    public void fetchProductsByCategory(String categoryId, SBC_Callback callback) {
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "products?category_id=eq." + categoryId)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Server error: " + response.code()));
                }
            }
        });
    }

    public static void getProductById(int id, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "products?select=*&id=eq." + id;
        executeGetRequest(url, callback);
    }

    public static void getProductImages(int productId, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "product_images?select=*&product_id=eq." + productId;
        executeGetRequest(url, callback);
    }

    public static void getProductVariants(int productId, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "product_variants?select=*&product_id=eq." + productId;
        executeGetRequest(url, callback);
    }

/*    public static void getProductReviews(int productId, SBC_Callback callback) {
        String url = DOMAIN_NAME + REST_PATH + "reviews?select=*&product_id=eq." + productId;
        executeGetRequest(url, callback);
    }*/

    private static void executeGetRequest(String url, SBC_Callback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", DataBinding.getBearerToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.onFailure(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            callback.onResponse(response.body().string());
                        } else {
                            callback.onFailure(new IOException("Server error: " + response.code()));
                        }
                    }
                });
            } catch (Exception e) {
                callback.onFailure(new IOException("URL error: " + e.getMessage()));
            }
        }).start();
    }

    /*// Метод для обновления профиля пользователя
    public void updateUserProfile(String userId, String jsonPayload, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonPayload);

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "user_profiles?user_id=eq." + userId)
                .patch(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse("Profile updated");
                } else {
                    callback.onFailure(new IOException("Update failed: " + response.code()));
                }
            }
        });
    }*/

   /* // Проверка OTP
    public void verifyOtp(String email, String token, final SBC_Callback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("token", token);
            payload.put("type", "recovery");
        } catch (Exception e) {
            callback.onFailure(new IOException("Ошибка формирования данных: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "verify")
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }

    // Обновление пароля
    public void updatePassword(OTPRequest otpRequest, SBC_Callback callback) {
        MediaType mediaType = MediaType.parse("application/json");
        String json = gson.toJson(otpRequest);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + AUTH_PATH + "user")
                .method("PUT", body)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", DataBinding.getBearerToken())
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onResponse(responseBody);
                } else {
                    callback.onFailure(new IOException("Ошибка сервера " + response));
                }
            }
        });
    }*/
}
