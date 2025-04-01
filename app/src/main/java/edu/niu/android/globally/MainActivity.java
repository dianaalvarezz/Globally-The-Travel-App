package edu.niu.android.globally;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import android.content.Intent;
import com.google.android.gms.auth.GoogleAuthUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements PostPage.MapUpdateListener
{

    public static String googlePhotosAccessToken;
    private GoogleSignInOptions gso;
    public static boolean isTokenReady = false;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Load HomePage by default
        loadHomePage();

        // ✅ Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this::handleNavigation);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .requestScopes(new Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
                .build();


    }

    @Override
    public void refreshMap()
    {
        // Find the HomePage fragment and update its map
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomePage)
        {
            ((HomePage) currentFragment).updateMap();
        }
    }

    private void loadHomePage()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomePage())
                .commit();
    }

    private boolean handleNavigation(MenuItem item)
    {
        int itemId = item.getItemId();

        // Handling Bottom Navigation for Fragments
        if (itemId == R.id.home)
        {
            // Navigate to HomePage fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomePage())
                    .commit();
            return true;

        }
        else if (itemId == R.id.friends)
        {
            // Navigate to FriendsPage fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendsPage())
                    .commit();
            return true;

        }
        else if (itemId == R.id.post)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostPage())
                    .commit();
            return true;
        }


        else if (itemId == R.id.search)
        {
            // Navigate to SearchPage fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchPage())
                    .commit();
            return true;

        }
        else if (itemId == R.id.profile)
        {
            // Navigate to ProfilePage fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfilePage())
                    .commit();
            return true;
        }

        return false;
    }


    private void saveToFirebase(String imageUrl, String city, String country, String caption) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("posts");

        String postId = databaseRef.push().getKey();

        if (postId != null) {
            Map<String, String> postData = new HashMap<>();
            postData.put("imageUrl", imageUrl);
            postData.put("city", city);
            postData.put("country", country);
            postData.put("caption", caption);

            databaseRef.child(postId).setValue(postData)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(getContext(), "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        //Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "Post uploaded unsuccessfully!", Toast.LENGTH_SHORT).show();

                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.top_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.notifications)
        {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        else if (id == R.id.messages)
        {
            startActivity(new Intent(this, MessagesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null || account.isExpired()) {
            GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
            startActivityForResult(client.getSignInIntent(), 1000);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String authCode = account.getServerAuthCode(); // ✅ Get auth code

                Thread thread = new Thread(() ->
                {
                    try
                    {
                        URL url = new URL("https://oauth2.googleapis.com/token");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        String postData = "code=" + authCode
                                + "&client_id=" + getString(R.string.default_web_client_id)
                                + "&client_secret=" + getString(R.string.google_client_secret)
                                + "&redirect_uri=" + ""  // Optional
                                + "&grant_type=authorization_code";

                        conn.getOutputStream().write(postData.getBytes("UTF-8"));

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null)
                        {
                            response.append(line);
                        }


                        JSONObject jsonResponse = new JSONObject(response.toString());
                            String token = jsonResponse.getString("access_token");
                        MainActivity.googlePhotosAccessToken = token;
                        Log.d("AUTH", "Access token set: " + token);
                        Log.d("AUTH", "Account email: " + account.getEmail());

                        Log.d("AccessToken", "Token: " + token);
                        MainActivity.googlePhotosAccessToken = token;
                        MainActivity.isTokenReady = true;

                        runOnUiThread(() -> {
                            // Only launch PostPage if it was requested before
                            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                            bottomNav.setSelectedItemId(R.id.post);  // This will trigger handleNavigation()
                        });

                    }
                    catch (Exception e)
                    {
                        Log.e("AccessToken", "Failed to get access token", e);
                    }
                });
                thread.start();
            }
            catch (ApiException e)
            {
                Log.w("SignIn", "Sign-in failed: " + e.getStatusCode());
            }
        }
    }

}
