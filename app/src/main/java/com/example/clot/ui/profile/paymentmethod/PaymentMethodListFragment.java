package com.example.clot.ui.profile.paymentmethod;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clot.R;
import com.example.clot.SupabaseClient;
import com.example.clot.models.PaymentMethod;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodListFragment extends Fragment {

    private RecyclerView recyclerView;
    private PaymentMethodAdapter adapter;
    private FloatingActionButton fabAddPaymentMethod;
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_method_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        recyclerView = view.findViewById(R.id.rvPaymentMethods);
        fabAddPaymentMethod = view.findViewById(R.id.fabAddPaymentMethod);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentMethodAdapter(paymentMethods,
                this::navigateToEditPaymentMethod,
                this::handleDeletePaymentMethod
        );
        recyclerView.setAdapter(adapter);

        // Кнопка добавления нового платежного метода
        fabAddPaymentMethod.setOnClickListener(v ->
                navigateToEditPaymentMethod(null)
        );

        // Загрузка платежных методов
        loadPaymentMethods();
    }

    private void navigateToEditPaymentMethod(PaymentMethod paymentMethod) {
        Bundle args = new Bundle();
        if (paymentMethod != null) {
            args.putString("payment_method_id", paymentMethod.getId());
            args.putString("card_last4", paymentMethod.getCardLast4());
            args.putInt("card_exp_month", paymentMethod.getCardExpMonth());
            args.putInt("card_exp_year", paymentMethod.getCardExpYear());
            args.putString("cardholder_name", paymentMethod.getCardholderName());
            args.putString("card_brand", paymentMethod.getCardBrand());
            args.putBoolean("is_default", paymentMethod.isDefault());
        }

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_paymentMethodListFragment_to_paymentEditFragment, args);
    }

    private void handleDeletePaymentMethod(PaymentMethod paymentMethod) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Payment Method")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Delete", (dialog, which) -> deletePaymentMethod(paymentMethod))
                .setNegativeButton("Cancel", null)
                .show();
    }

    SupabaseClient supabaseClient = new SupabaseClient();

    private void deletePaymentMethod(PaymentMethod paymentMethod) {
        supabaseClient.deletePaymentMethod(paymentMethod.getId(), new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        showError("Failed to delete payment method: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        requireActivity().runOnUiThread(() -> {
                            adapter.removePaymentMethod(paymentMethod);
                            Toast.makeText(
                                    requireContext(),
                                    "Payment method deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
    }

    private void loadPaymentMethods() {
        supabaseClient.getUserPaymentMethods(new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        showError("Failed to load payment methods");
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                JSONArray paymentMethodsArray = new JSONArray(responseBody);
                                paymentMethods.clear();

                                for (int i = 0; i < paymentMethodsArray.length(); i++) {
                                    JSONObject pmObj = paymentMethodsArray.getJSONObject(i);
                                    PaymentMethod pm = new PaymentMethod(
                                            pmObj.getString("id"),
                                            pmObj.getString("user_id"),
                                            pmObj.getString("card_last4"),
                                            pmObj.getInt("card_exp_month"),
                                            pmObj.getInt("card_exp_year"),
                                            pmObj.getString("cardholder_name"),
                                            pmObj.getString("card_brand"),
                                            pmObj.getBoolean("is_default")
                                    );

                                    paymentMethods.add(pm);
                                }

                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                showError("Error parsing payment methods");
                            }
                        });
                    }
                });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        );
    }
}