package edu.niu.android.globally;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.FirebaseApp; // For Firebase initialization
import androidx.fragment.app.Fragment; // For fragment transactions

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.Client_id))
                .requestEmail()
                // .setAccountName(null)
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();

        // Check if the user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, go directly to the homepage
            Log.d("MainActivity", "✅ User is already signed in: " + currentUser.getEmail());
            loadHomePage();
        } else {
            // User is not signed in, open sign-in page
            Log.d("MainActivity", "❌ No user signed in, redirecting to login...");
            openSignInPage();
        }
    }


    // 🔹 Google Sign-In Method
    public void signInWithGoogle() {
        Log.d("MainActivity", "🚀 Google Sign-In Started!");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                   /*
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getEmail() != null) {
                            Log.d("MainActivity", "🔑 Google Account Retrieved: " + account.getEmail());
                        } else {
                            Log.d("MainActivity", "❗ Email is null or account is invalid");
                        }
                        firebaseAuthWithGoogle(account);  // ✅ Only one call here
                    } catch (ApiException e) {
                        Log.e("MainActivity", "❌ Google Sign-In Failed", e);
                    }
                    */

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            String idToken = account.getIdToken();
                            if (idToken == null || idToken.isEmpty()) {
                                Log.e("MainActivity", "❌ Google ID Token is NULL or Empty. Authentication will fail.");
                                return;
                            }

                            Log.d("MainActivity", "🔑 Google ID Token Retrieved: " + idToken);
                            firebaseAuthWithGoogle(account);
                        } else {
                            Log.d("MainActivity", "❗ Google Sign-In Failed: account is NULL");
                        }
                    } catch (ApiException e) {
                        Log.e("MainActivity", "❌ Google Sign-In Failed", e);
                    }

                }
            });

    // 🔹 Authenticate Google Sign-In with Firebase
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("MainActivity", "🔑 Authenticating with Firebase: " + acct.getEmail());

        auth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d("MainActivity", "✅ Firebase Sign-In Success: " + user.getEmail());
                        updateUI(user);  // Update UI after successful sign-in
                    } else {
                        Log.e("MainActivity", "❌ Firebase Sign-In Failed", task.getException());
                    }
                });
    }

    // 🔹 Update UI Based on Login Status
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d("FirebaseAuth", "✅ User signed in: " + user.getEmail());
            loadHomePage(); // Ensures HomePage always loads for signed-in users
        } else {
            Log.d("FirebaseAuth", "❌ User not signed in, redirecting to login.");
            openSignInPage();
        }
    }
    private void loadHomePage() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomePage())
                .commit();
    } // ✅ Missing closing brace added



    // 🔹 Sign Out Method
    private void signOut() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(this, revokeTask -> {
                Log.d("MainActivity", "✅ Google Sign-Out & Revoke Successful!");
                updateUI(null);
            });
        });
    }
    // Handle Bottom Navigation
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
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public void openSignInPage() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SigninPage())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}