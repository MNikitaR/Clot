package com.example.clot.ui.profile.address;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.clot.models.Address;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddressFragment extends Fragment {

    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private FloatingActionButton fabAddAddress;
    private List<Address> addressList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        recyclerView = view.findViewById(R.id.rvAddresses);
        fabAddAddress = view.findViewById(R.id.fabAddAddress);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().onBackPressed());

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AddressAdapter(addressList, address -> navigateToEditAddress(address), // Обработчик клика
                this::handleDeleteAddress // Обработчик долгого нажатия
                );
        recyclerView.setAdapter(adapter);

        // Кнопка добавления нового адреса
        fabAddAddress.setOnClickListener(v ->
            // Переход к созданию нового адреса
            navigateToEditAddress(null));

        // Загрузка адресов
        loadAddresses();
    }

    private void loadAddresses() {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.getUserAddresses(new SupabaseClient.SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                Log.e("performLogin:onFailure", e.getLocalizedMessage());
            }

            @Override
            public void onResponse(String responseBody) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONArray addressesArray = new JSONArray(responseBody);
                        addressList.clear();

                        for (int i = 0; i < addressesArray.length(); i++) {
                            JSONObject addressObj = addressesArray.getJSONObject(i);
                            Address address = new Address(
                                    addressObj.getString("id"),
                                    addressObj.getString("street"),
                                    addressObj.getString("city"),
                                    addressObj.getString("state"),
                                    addressObj.getString("zip_code"),
                                    addressObj.getBoolean("is_default")
                            );
                            addressList.add(address);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void navigateToEditAddress(Address address) {
        Bundle args = new Bundle();
        if (address != null) {
            args.putString("address_id", address.getId());
            args.putString("street", address.getStreet());
            args.putString("city", address.getCity());
            args.putString("state", address.getState());
            args.putString("zip_code", address.getZipCode());
        }

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_addressListFragment_to_addressEditFragment, args);
    }

    private void handleDeleteAddress(Address address) {
        // Показать диалог подтверждения удаления
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAddress(Address address) {
        SupabaseClient supabaseClient = new SupabaseClient();
        supabaseClient.deleteAddress(address.getId(), new SupabaseClient.SBC_Callback() {
                    @Override
                    public void onFailure(IOException e) {
                        Log.e("performLogin:onFailure", e.getLocalizedMessage());
                    }

                    @Override
                    public void onResponse(String responseBody) {
                        requireActivity().runOnUiThread(() -> {
                            // Удалить адрес из списка
                            adapter.removeAddress(address);

                            // Показать уведомление
                            Toast.makeText(
                                    requireContext(),
                                    "Address deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
    }

}