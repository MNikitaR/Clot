package com.example.clot;

import androidx.annotation.NonNull;

import com.example.clot.models.LoginRequest;
import com.example.clot.models.OTPRequest;
import com.example.clot.models.ProfileUpdate;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    public static String DOMAIN_NAME = "https://vhvygctsmcdqkfgflchf.supabase.co/";
    public static String AUTH_PATH = "auth/v1/";
    public static String REST_PATH = "rest/v1/";
    public static String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZodnlnY3RzbWNkcWtmZ2ZsY2hmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkwMTUxNjIsImV4cCI6MjA2NDU5MTE2Mn0.C_VaBGTogftuvzliwRUIY9HUQgxZTdR02PBqbeX49hU";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
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
