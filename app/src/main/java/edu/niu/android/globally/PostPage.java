package edu.niu.android.globally;
import android.content.Intent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Geocoder;
import android.location.Address;

import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import android.widget.ImageView;
import com.bumptech.glide.Glide;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.recyclerview.widget.GridLayoutManager;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.app.Activity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.DataSnapshot;


public class PostPage extends Fragment
{
    private RecyclerView recyclerView;
    private PinAdapter pinAdapter;
    private List<Pin> pinList = new ArrayList<>();
    private MapUpdateListener mapUpdateListener;
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<String> photoUrls = new ArrayList<>();
    private Uri selectedImageUri; // To store the selected image URI
    private RecyclerView uploadedPostsRecyclerView;
    private ArrayList<Post> uploadedPosts = new ArrayList<>();
    private PostAdapter postAdapter;


    public interface MapUpdateListener
    {
        void refreshMap();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_postpage, container, false);

        recyclerView = rootView.findViewById(R.id.pinRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Photos RecyclerView
        photosRecyclerView = rootView.findViewById(R.id.photosRecyclerView);

        photosRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        photoAdapter = new PhotoAdapter(photoUrls);
        photosRecyclerView.setAdapter(photoAdapter);

        SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
        Map<String, ?> allPins = prefs.getAll();
        //Button viewGooglePhotosButton = rootView.findViewById(R.id.viewGooglePhotosButton);

        Button selectPhotoButton = rootView.findViewById(R.id.selectPhotoButton);
        selectPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1010); // Custom request code
        });



        // Create a list of pin objects
        for (Map.Entry<String, ?> entry : allPins.entrySet())
        {
            String value = entry.getValue().toString();
            String[] parts = value.split(",");

            if (parts.length == 3)
            {
                double lat = Double.parseDouble(parts[0]);
                double lng = Double.parseDouble(parts[1]);
                String country = parts[2];
                pinList.add(new Pin(lat, lng, country));
            }
        }

        // Set up the PinAdapter
        pinAdapter = new PinAdapter(getContext(), pinList, this);
        recyclerView.setAdapter(pinAdapter);

        // Handle Add Location button click
        Button addLocationButton = rootView.findViewById(R.id.addLocationButton);
        addLocationButton.setOnClickListener(v ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Enter City Name");

            final EditText input = new EditText(getContext());
            input.setHint("e.g. London, NYC");
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) ->
            {
                String cityName = input.getText().toString();

                if (!cityName.isEmpty())
                {
                    addCityPin(cityName);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Fetch photos when fragment starts
        fetchGooglePhotos();


        uploadedPostsRecyclerView = rootView.findViewById(R.id.uploadedPostsRecyclerView);
        uploadedPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(), uploadedPosts);

        uploadedPostsRecyclerView.setAdapter(postAdapter);

        loadUploadedPosts();

        return rootView;
    }

    private void loadUploadedPosts()
    {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.get().addOnSuccessListener(dataSnapshot -> {
            uploadedPosts.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    uploadedPosts.add(post);
                }
            }

            postAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("FIREBASE", "Failed to load posts", e);
            Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1010 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null)
        {
            selectedImageUri = data.getData();

            if (selectedImageUri != null)
            {
                // ✅ Upload to Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("user_photos/" + UUID.randomUUID().toString());

                storageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                Log.d("UPLOAD", "Photo uploaded. URL: " + downloadUrl);

                                // Next: Prompt user for location + caption
                                promptForLocationAndCaption(downloadUrl);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UPLOAD", "Failed to upload photo", e);
                            Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }
    private void promptForLocationAndCaption(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Location & Caption");

        // Create a vertical layout with 2 input fields
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText cityInput = new EditText(getContext());
        cityInput.setHint("City");
        layout.addView(cityInput);

        final EditText countryInput = new EditText(getContext());
        countryInput.setHint("Country");
        layout.addView(countryInput);

        final EditText captionInput = new EditText(getContext());
        captionInput.setHint("Short caption");
        layout.addView(captionInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String city = cityInput.getText().toString().trim();
            String country = countryInput.getText().toString().trim();
            String caption = captionInput.getText().toString().trim();

            if (!city.isEmpty() && !country.isEmpty() && !caption.isEmpty()) {
                saveToFirebase(imageUrl, city, country, caption);
            } else {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    public void fetchGooglePhotos()
    {
        Log.d("FETCH", "Called fetchGooglePhotos()");
        Log.d("FETCH", "Access token = " + MainActivity.googlePhotosAccessToken);

        if (MainActivity.isTokenReady && MainActivity.googlePhotosAccessToken != null)
        {
            GooglePhotosHelper.fetchAllPhotos(
                    MainActivity.googlePhotosAccessToken,
                    new GooglePhotosHelper.MediaCallback()
                    {
                        @Override
                        public void onMediaFetched(String json)
                        {
                            try
                            {
                                JSONObject response = new JSONObject(json);

                                if (response.has("mediaItems"))
                                {
                                    Log.d("FETCH", "Received JSON: " + json);

                                    JSONArray mediaItems = response.getJSONArray("mediaItems");
                                    photoUrls.clear();

                                    // ✅ TEMPORARY: Load just 1 image manually for testing
                                    if (mediaItems.length() > 0)
                                    {
                                        JSONObject first = mediaItems.getJSONObject(0);
                                        String url = first.getString("baseUrl") + "=w500-h500";

                                        Log.d("ManualPhotoURL", url);

                                        getActivity().runOnUiThread(() ->
                                        {
                                            ImageView testImage = new ImageView(getContext());
                                            Glide.with(getContext()).load(url).into(testImage);
                                            Toast.makeText(getContext(), "Image fetched successfully", Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    // ✅ Add all photo URLs to the list
                                    for (int i = 0; i < mediaItems.length(); i++)
                                    {
                                        JSONObject item = mediaItems.getJSONObject(i);
                                        if (item.has("baseUrl"))
                                        {
                                            String baseUrl = item.getString("baseUrl");
                                            String finalUrl = baseUrl + "=w500-h500";
                                            Log.d("PhotoURL", finalUrl);
                                            photoUrls.add(finalUrl);
                                        }
                                    }

                                    getActivity().runOnUiThread(() -> photoAdapter.notifyDataSetChanged());
                                }
                            }
                            catch (JSONException e)
                            {
                                Log.e("PostPage", "Error parsing photos JSON", e);
                            }
                        }

                        @Override
                        public void onError(Exception e)
                        {
                            Log.e("PostPage", "Error fetching photos", e);
                            getActivity().runOnUiThread(() ->
                                    showToast("Failed to load photos")
                            );
                        }
                    }
            );
        }
        else
        {
            showToast("Please sign in to view photos");
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        // Fetch photos when fragment becomes visible again
        if (MainActivity.googlePhotosAccessToken != null) {
            fetchGooglePhotos();
        }
    }

    private void showToast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof MapUpdateListener)
        {
            mapUpdateListener = (MapUpdateListener) context;
        }
    }

    public void removePin(Pin pin)
    {
        SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove the pin from SharedPreferences using the key (lat, lng, country)
        String pinKey = pin.getLatitude() + "," + pin.getLongitude() + "," + pin.getCountry();
        editor.remove(pinKey);  // Remove the pin from SharedPreferences
        editor.apply();

        // Notify the adapter and remove the pin from the list
        pinList.remove(pin);
        pinAdapter.notifyDataSetChanged();

        // Show a toast to inform the user
        Toast.makeText(getActivity(), "Pin removed!", Toast.LENGTH_SHORT).show();

        if (mapUpdateListener != null)
        {
            mapUpdateListener.refreshMap(); // Trigger map refresh
        }
    }



    public void updateMap()
    {
        // ✅ Notify MainActivity to refresh the map via the listener
        if (mapUpdateListener != null)
        {
            mapUpdateListener.refreshMap();
        }
    }


    // Method to add a new pin and update SharedPreferences
    private void addCityPin(String cityName)
    {
        Log.d("PostPage", "addCityPin called for city: " + cityName);

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (!addresses.isEmpty())
            {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                String country = address.getCountryName();

                // Save the pin (for now we’ll use SharedPreferences)
                SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                String pinKey = lat + "," + lng + "," + country;
                String value = lat + "," + lng + "," + country;
                editor.putString(pinKey, value);
                editor.apply();

                // Add the pin to the list and update RecyclerView
                pinList.add(new Pin(lat, lng, country));
                pinAdapter.notifyDataSetChanged();

                if (mapUpdateListener != null)
                {
                    mapUpdateListener.refreshMap();
                }

                Toast.makeText(getContext(), "Location saved: " + cityName, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getContext(), "City not found.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error getting location.", Toast.LENGTH_SHORT).show();
        }
    }




    // Static inner Pin class
    public static class Pin
    {
        private double latitude;
        private double longitude;
        private String country;

        public Pin(double latitude, double longitude, String country)
        {
            this.latitude = latitude;
            this.longitude = longitude;
            this.country = country;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public double getLongitude()
        {
            return longitude;
        }

        public String getCountry()
        {
            return country;
        }
    }


private void saveToFirebase(String imageUrl, String city, String country, String caption)
{
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("posts");

    Map<String, String> postData = new HashMap<>();
    postData.put("imageUrl", imageUrl);
    postData.put("city", city);
    postData.put("country", country);
    postData.put("caption", caption);

    databaseRef.push().setValue(postData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Post saved!", Toast.LENGTH_SHORT).show();
                loadUploadedPosts();
            })
            .addOnFailureListener(e -> {
                Log.e("FIREBASE", "Failed to save post", e);
                Toast.makeText(getContext(), "Failed to save post", Toast.LENGTH_SHORT).show();
            });
    }
}
