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
        
        if (isLoggedIn) {
            // User is logged in, redirect to home
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // User is not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
        }
        
        finish();
    }
}
