package com.example.oktanativeclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "OktaPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean biometricEnabled = prefs.getBoolean("biometricEnabled", false);
        
        if (isLoggedIn) {
            // User is logged in, check authentication method preference
            if (biometricEnabled) {
                // User prefers biometric authentication
                startActivity(new Intent(this, BiometricAuthActivity.class));
            } else {
                // User is logged in with traditional method, go to home
                startActivity(new Intent(this, HomeActivity.class));
            }
        } else {
            // User is not logged in, show authentication choice
            startActivity(new Intent(this, AuthenticationChoiceActivity.class));
        }
        
        finish();
    }
}
