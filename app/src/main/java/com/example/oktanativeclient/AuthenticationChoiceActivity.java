package com.example.oktanativeclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

public class AuthenticationChoiceActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "OktaPrefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";
    
    private Button traditionalLoginButton;
    private Button biometricLoginButton;
    private TextView biometricStatusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_choice);
        
        initViews();
        checkBiometricAvailability();
        setupClickListeners();
    }
    
    private void initViews() {
        traditionalLoginButton = findViewById(R.id.traditional_login_button);
        biometricLoginButton = findViewById(R.id.biometric_login_button);
        biometricStatusText = findViewById(R.id.biometric_status_text);
    }
    
    private void checkBiometricAvailability() {
        BiometricManager biometricManager = BiometricManager.from(this);
        
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                biometricStatusText.setText("Biometric authentication is available");
                biometricLoginButton.setEnabled(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                biometricStatusText.setText("No biometric features available on this device");
                biometricLoginButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                biometricStatusText.setText("Biometric features are currently unavailable");
                biometricLoginButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                biometricStatusText.setText("No biometric credentials enrolled. Please set up Face ID or fingerprint in device settings");
                biometricLoginButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                biometricStatusText.setText("Security update required for biometric authentication");
                biometricLoginButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                biometricStatusText.setText("Biometric authentication is not supported");
                biometricLoginButton.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                biometricStatusText.setText("Biometric authentication status unknown");
                biometricLoginButton.setEnabled(false);
                break;
        }
    }
    
    private void setupClickListeners() {
        traditionalLoginButton.setOnClickListener(v -> {
            // Save preference for traditional login
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, false).apply();
            
            // Navigate to traditional login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        
        biometricLoginButton.setOnClickListener(v -> {
            // Save preference for biometric login
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, true).apply();
            
            // Navigate to biometric authentication
            startActivity(new Intent(this, BiometricAuthActivity.class));
            finish();
        });
    }
}
