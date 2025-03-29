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

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import android.content.Intent;
import com.google.android.gms.auth.GoogleAuthUtil;


public class MainActivity extends AppCompatActivity
{

    public static String googlePhotosAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
                .build();



        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

// Trigger sign-in (optional button click later)
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000); // 1000 = RC_SIGN_IN

    }

    private void loadHomePage()
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomePage())
                .commit();
    }

    private boolean handleNavigation(MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            selectedFragment = new HomePage();
        } else if (itemId == R.id.friends) {
            selectedFragment = new FriendsPage();
        } else if (itemId == R.id.post) {
            selectedFragment = new PostPage();
        } else if (itemId == R.id.search) {
            selectedFragment = new SearchPage();
        } else if (itemId == R.id.profile) {
            selectedFragment = new ProfilePage();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.messages) {
            startActivity(new Intent(this, MessagesActivity.class));
            return true;
        }



        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                Log.d("SignIn", "ID Token: " + idToken);

                // 🔐 Get Access Token for Google Photos API
                Thread thread = new Thread(() -> {
                    try {
                        String scope = "oauth2:https://www.googleapis.com/auth/photoslibrary.readonly";
                        String token = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope);
                        Log.d("AccessToken", "Token: " + token);

                        // ✅ Save token to static field
                        MainActivity.googlePhotosAccessToken = token;



                        Log.d("AccessToken", "Access Token set in static field: " + MainActivity.googlePhotosAccessToken);


                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e("AccessToken", "Failed to get access token", e);
                    }
                });
                thread.start();

            } catch (ApiException e) {
                Log.w("SignIn", "Sign-in failed: " + e.getStatusCode());
            }
        }
    }

}
