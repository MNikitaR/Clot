package com.example.clot;

public class DataBinding {
    private static String bearerToken;
    private static String uuidUser;

    public static String getBearerToken() {
        return bearerToken;
    }

    public static void saveBearerToken(String bearerToken) {
        DataBinding.bearerToken = bearerToken;
    }

    public static String getUuidUser() {
        return uuidUser;
    }

    public static void saveUuidUser(String uuidUser) {
        DataBinding.uuidUser = uuidUser;
    }
}
