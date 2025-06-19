package com.example.clot.models;

import com.example.clot.R;

public class PaymentMethod {
    private String id;
    private String userId;
    private String cardLast4;
    private int cardExpMonth;
    private int cardExpYear;
    private String cardholderName;
    private String cardBrand;
    private boolean isDefault;

    // Конструкторы
    public PaymentMethod() {}

    public PaymentMethod(String id, String userId, String cardLast4, int cardExpMonth,
                         int cardExpYear, String cardholderName, String cardBrand,
                         boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.cardLast4 = cardLast4;
        this.cardExpMonth = cardExpMonth;
        this.cardExpYear = cardExpYear;
        this.cardholderName = cardholderName;
        this.cardBrand = cardBrand;
        this.isDefault = isDefault;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public int getCardExpMonth() { return cardExpMonth; }
    public void setCardExpMonth(int cardExpMonth) { this.cardExpMonth = cardExpMonth; }

    public int getCardExpYear() { return cardExpYear; }
    public void setCardExpYear(int cardExpYear) { this.cardExpYear = cardExpYear; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    // Форматированные данные для отображения
    public String getFormattedCardNumber() {
        return "**** **** **** " + cardLast4;
    }

    public String getFormattedExpiryDate() {
        return String.format("%02d/%d", cardExpMonth, cardExpYear % 100);
    }

    public int getBrandIcon() {
        if (cardBrand == null) return R.drawable.ic_credit_card;
        switch (cardBrand.toLowerCase()) {
            case "visa": return R.drawable.ic_visa;
            case "mastercard": return R.drawable.ic_mastercard;
            case "amex": return R.drawable.ic_amex;
            case "discover": return R.drawable.ic_discover;
            case "mir": return R.drawable.ic_mir;
            default: return R.drawable.ic_credit_card;
        }
    }
}
