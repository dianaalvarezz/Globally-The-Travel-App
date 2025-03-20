package edu.niu.android.globally;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.SignInButton;

public class SigninPage extends Fragment {

    public SigninPage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signinpage, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the Google Sign-In button
        SignInButton signInButton = view.findViewById(R.id.btn_google_sign_in);

        if (signInButton == null) {
            Log.e("SigninPage", "🚨 Google Sign-In Button NOT found!");
            return;
        } else {
            Log.d("SigninPage", "✅ Google Sign-In Button Found!");
        }

        // Click listener
        signInButton.setOnClickListener(v -> {
            Log.d("SigninPage", "✅ Google Sign-In Button Clicked!");
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signInWithGoogle();
            } else {
                Log.e("SigninPage", "❌ MainActivity not detected!");
            }
        });
    }


}
