package edu.niu.android.globally;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class HomePage extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_homepage, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds worldBounds = new LatLngBounds(
                new LatLng(-70, -140),
                new LatLng(80, 60)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(worldBounds, 10));
        mMap.setMinZoomPreference(0.5f);

        SharedPreferences prefs = requireContext().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
        Map<String, ?> allPins = prefs.getAll();
        int totalPinnedCount = 0;

        for (Map.Entry<String, ?> entry : allPins.entrySet()) {
            String value = entry.getValue().toString(); // Format: lat,lng,country
            String[] parts = value.split(",");
            if (parts.length == 3) {
                double lat = Double.parseDouble(parts[0]);
                double lng = Double.parseDouble(parts[1]);
                String country = parts[2];

                LatLng position = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(position).title(country));

                totalPinnedCount++;
            }
        }

        // Calculate the percentage of pinned countries (out of the total 195 countries)
        double percentage = ((double) totalPinnedCount / 195) * 100; // 195 is the total number of recognized countries
        String percentText = String.format("%.2f%%", percentage);

        // Set the total pinned countries and percentage in textView3 and textView4
        TextView countryCount = rootView.findViewById(R.id.textView3);
        countryCount.setText("Total Countries: " + totalPinnedCount);

        TextView percentageText = rootView.findViewById(R.id.textView4);
        percentageText.setText("Percentage Pinned: " + percentText);
    }


}
