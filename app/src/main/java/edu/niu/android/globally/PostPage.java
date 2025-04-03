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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Activity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.location.Geocoder;
import android.location.Address;
import java.util.Locale;
import java.io.IOException;
import java.util.List;
import com.google.firebase.database.DataSnapshot;

import edu.niu.android.globally.R;




public class PostPage extends Fragment
{
    // Photo Stuff
    private RecyclerView photosRecyclerView; // Grid of Photos
    private ArrayList<String> photoUrls = new ArrayList<>(); // Stores Google Photos URLs
    private Uri selectedImageUri; // URI of selected image
    private PhotoAdapter photoAdapter; // Adapter for Google Photos grid


    // Posts Stuff
    private List<String> postKeys = new ArrayList<>(); // Firebase keys for posts (delete)
    private List<Post> uploadedPosts = new ArrayList<>(); // Lists of posts from firebase
    private PostAdapter postAdapter; // Adapter for users' uploaded posts


    // Map Stuff
    private MapUpdateListener mapUpdateListener; // Callback to update map


    private View fragmentRootView; // Root view of the fragment



/*
Allows the fragment to notify the parent activity to refresh map with a pin is added/deleted
 */
    public interface MapUpdateListener
    {
        // Forces map refresh
        void refreshMap();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_postpage, container, false);

        photosRecyclerView = rootView.findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        photoAdapter = new PhotoAdapter(photoUrls);
        photosRecyclerView.setAdapter(photoAdapter);


        Button selectPhotoButton = rootView.findViewById(R.id.selectPhotoButton);
        selectPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1010);
        });

        fragmentRootView = rootView;

        fetchGooglePhotos();

        loadUploadedPosts();

        return rootView;
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
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("user_photos/" + UUID.randomUUID().toString());
                storageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot ->
                        {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                            {
                                String downloadUrl = uri.toString();
                                promptForLocationAndCaption(downloadUrl);
                            });
                        })
                        .addOnFailureListener(e ->
                        {
                            Log.e("UPLOAD", "Failed to upload photo", e);
                            Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }




    public void updateMap()
    {
        if (mapUpdateListener != null)
        {
            mapUpdateListener.refreshMap();
        }
    }





    private void promptForLocationAndCaption(String imageUrl)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Location & Caption");

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

        builder.setPositiveButton("Save", (dialog, which) ->
        {
            String city = cityInput.getText().toString().trim();
            String country = countryInput.getText().toString().trim();
            String caption = captionInput.getText().toString().trim();

            if (!city.isEmpty() && !country.isEmpty() && !caption.isEmpty())
            {
                saveToFirebase(imageUrl, city, country, caption);
            }
            else
            {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }




    private void saveToFirebase(String imageUrl, String city, String country, String caption)
    {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("posts");

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocationName(city + ", " + country, 1);

            if (!addresses.isEmpty())
            {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();

                // Save pin to SharedPreferences
                SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                // Convert lat/lng to strings with fixed precision
                String latStr = String.format(Locale.US, "%.6f", lat);
                String lngStr = String.format(Locale.US, "%.6f", lng);
                String pinKey = latStr + "," + lngStr + "," + country.trim().toLowerCase();

// Save to SharedPreferences
                editor.putString(pinKey, pinKey);

                editor.apply();

                if (mapUpdateListener != null)
                {
                    mapUpdateListener.refreshMap();
                }


                Map<String, String> postData = new HashMap<>();
                postData.put("imageUrl", imageUrl);
                postData.put("city", city);
                postData.put("country", country);
                postData.put("caption", caption);
                postData.put("lat", latStr);
                postData.put("lng", lngStr);

                databaseRef.push().setValue(postData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Post saved!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                        {
                            Log.e("FIREBASE", "Failed to save post", e);
                            Toast.makeText(getContext(), "Failed to save post", Toast.LENGTH_SHORT).show();
                        });
            }
            else
            {
                Toast.makeText(getContext(), "Could not find location.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e)
        {
            Log.e("GEO", "Geocoding error", e);
            Toast.makeText(getContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
        }
    }




    public void fetchGooglePhotos()
    {
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
                                    JSONArray mediaItems = response.getJSONArray("mediaItems");
                                    photoUrls.clear();
                                    for (int i = 0; i < mediaItems.length(); i++)
                                    {
                                        JSONObject item = mediaItems.getJSONObject(i);
                                        if (item.has("baseUrl"))
                                        {
                                            String baseUrl = item.getString("baseUrl");
                                            photoUrls.add(baseUrl + "=w500-h500");
                                        }
                                    }

                                    if (getActivity() != null)
                                    {
                                        getActivity().runOnUiThread(() -> photoAdapter.notifyDataSetChanged());
                                    }

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
                                    Toast.makeText(getContext(), "Failed to load photos", Toast.LENGTH_SHORT).show());
                        }
                    }
            );
        }
        else
        {
            Toast.makeText(getContext(), "Please sign in to view photos", Toast.LENGTH_SHORT).show();
        }
    }




    public void deletePostAndPin(Post post, String firebaseKey)
    {
        FirebaseDatabase.getInstance().getReference("posts").child(firebaseKey).removeValue();

        String pinKey = post.lat + "," + post.lng + "," + post.country;
        SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(pinKey);
        editor.apply();

        if (mapUpdateListener != null)
        {
            mapUpdateListener.refreshMap();
        }

        Toast.makeText(getContext(), "Post & Pin deleted", Toast.LENGTH_SHORT).show();
    }



/*
Attaches the fragment to its parent activity and checks if the activity implements MapUpdateListener
 */
    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof MapUpdateListener)
        {
            // Bind the listener
            mapUpdateListener = (MapUpdateListener) context;
        }
    }




    private void loadUploadedPosts()
    {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.get().addOnSuccessListener(dataSnapshot ->
        {
            uploadedPosts.clear();
            postKeys.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                Post post = snapshot.getValue(Post.class);

                if (post != null)
                {
                    uploadedPosts.add(post);
                    postKeys.add(snapshot.getKey());
                }
            }

            if (postAdapter != null)
            {
                postAdapter.notifyDataSetChanged();
            }
            else
            {
                RecyclerView uploadedPostsRecyclerView = fragmentRootView.findViewById(R.id.uploadedPostsRecyclerView);
                postAdapter = new PostAdapter(getContext(), uploadedPosts, postKeys, this::deletePost);

                uploadedPostsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                uploadedPostsRecyclerView.setAdapter(postAdapter);
            }

        }).addOnFailureListener(e ->
        {
            Log.e("FIREBASE", "Failed to load posts", e);
            Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
        });
    }



    /*
    Deletes post from Firebase using its key by removing the associated pin from SharedPreference
     */
    private void deletePost(Post post, String firebaseKey)
    {
        FirebaseDatabase.getInstance().getReference("posts")
                .child(firebaseKey).removeValue() // Delete from Firebase
                .addOnSuccessListener(aVoid ->
                {
                    // Ensure Post class stores lat/lng as Strings with sufficient precision
                    String latStr = String.format(Locale.US, "%.6f", Double.parseDouble(post.lat));
                    String lngStr = String.format(Locale.US, "%.6f", Double.parseDouble(post.lng));
                    String countryLower = post.country.trim().toLowerCase(); // Normalize country name
                    String pinKey = latStr + "," + lngStr + "," + countryLower;

// Remove from SharedPreferences
                    SharedPreferences prefs = getActivity().getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
                    prefs.edit().remove(pinKey).apply();


                    // Refreshes map and reloads post
                    if (mapUpdateListener != null)
                    {
                        mapUpdateListener.refreshMap();
                    }

                    // Message if post is deleted successfully
                    Toast.makeText(getContext(), "Post deleted!", Toast.LENGTH_SHORT).show();
                    loadUploadedPosts();
                })

                .addOnFailureListener(e ->
                {
                    // Message if post is deleted unsuccessfully
                    Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                });
    }



}
