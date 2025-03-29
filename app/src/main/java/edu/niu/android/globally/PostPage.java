package edu.niu.android.globally;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PostPage extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private ArrayList<String> photoUrls = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_postpage, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new PhotoAdapter(photoUrls);
        recyclerView.setAdapter(adapter);

        Button addLocationButton = view.findViewById(R.id.addLocationButton);
        addLocationButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Enter City Name");

            final EditText input = new EditText(requireContext());
            input.setHint("e.g. London, NYC");
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String cityName = input.getText().toString();
                if (!cityName.isEmpty()) {
                    addCityPin(cityName);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        return view;
    }

    private void addCityPin(String cityName) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                String country = address.getCountryName();

                SharedPreferences prefs = requireContext().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String pinKey = UUID.randomUUID().toString();
                String value = lat + "," + lng + "," + country;
                editor.putString(pinKey, value);
                editor.apply();

                Toast.makeText(getContext(), "Location saved: " + cityName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "City not found.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error getting location.", Toast.LENGTH_SHORT).show();
        }
    }
}
