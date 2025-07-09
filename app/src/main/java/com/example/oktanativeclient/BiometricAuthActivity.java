package com.example.oktanativeclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.okta.oidc.AuthorizationStatus;
import com.okta.oidc.OIDCConfig;
import com.okta.oidc.Okta;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.clients.sessions.SessionClient;
import com.okta.oidc.clients.web.WebAuthClient;
import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.storage.security.DefaultEncryptionManager;
import com.okta.oidc.util.AuthorizationException;

import java.util.concurrent.Executor;

public class BiometricAuthActivity extends AppCompatActivity {
    
    private static final String TAG = "BiometricAuthActivity";
    private static final String PREFS_NAME = "OktaPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_BIOMETRIC_SETUP = "biometricSetup";
    
    private WebAuthClient webAuthClient;
    private SessionClient sessionClient;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    
    private Button authenticateButton;
    private Button setupBiometricButton;
    private Button switchToTraditionalButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView instructionText;
    
    private boolean isFirstTimeSetup = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_auth);
        
        initViews();
        initOkta();
        initBiometric();
        checkBiometricSetup();
        setupClickListeners();
    }
    
    private void initViews() {
        authenticateButton = findViewById(R.id.authenticate_button);
        setupBiometricButton = findViewById(R.id.setup_biometric_button);
        switchToTraditionalButton = findViewById(R.id.switch_to_traditional_button);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        instructionText = findViewById(R.id.instruction_text);
    }
    
    private void initOkta() {
        try {
            OIDCConfig config = new OIDCConfig.Builder()
                    .withJsonFile(this, R.raw.okta_oidc_config)
                    .create();
                    
            webAuthClient = new Okta.WebAuthBuilder()
                    .withConfig(config)
                    .withContext(getApplicationContext())
                    .withStorage(new SharedPreferenceStorage(this))
                    .withEncryptionManager(new DefaultEncryptionManager(this))
                    .setRequireHardwareBackedKeyStore(false)
                    .create();
                    
            sessionClient = webAuthClient.getSessionClient();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Okta", e);
            showError("Failed to initialize authentication");
        }
    }
    
    private void initBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                showError("Biometric authentication error: " + errString);
            }
            
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                
                if (isFirstTimeSetup) {
                    // First time setup - proceed with Okta authentication
                    performOktaAuthentication();
                } else {
                    // Regular authentication - check if already logged in to Okta
                    checkExistingOktaSession();
                }
            }
            
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                showError("Biometric authentication failed. Please try again.");
            }
        });
        
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your biometric credential to authenticate")
                .setDescription("Place your finger on the sensor or look at the front camera")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
    }
    
    private void checkBiometricSetup() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean biometricSetup = prefs.getBoolean(KEY_BIOMETRIC_SETUP, false);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (!biometricSetup) {
            // First time setup
            isFirstTimeSetup = true;
            instructionText.setText("Set up biometric authentication for secure access to your Okta account");
            setupBiometricButton.setVisibility(View.VISIBLE);
            authenticateButton.setVisibility(View.GONE);
            statusText.setText("Biometric authentication not set up");
        } else if (isLoggedIn) {
            // User has both biometric and Okta set up, and is logged in
            instructionText.setText("Use biometric authentication to access your account");
            setupBiometricButton.setVisibility(View.GONE);
            authenticateButton.setVisibility(View.VISIBLE);
            statusText.setText("Ready for biometric authentication");
        } else {
            // User has biometric set up but not logged in to Okta
            isFirstTimeSetup = true;
            instructionText.setText("Authenticate with biometric and complete Okta login");
            setupBiometricButton.setVisibility(View.GONE);
            authenticateButton.setVisibility(View.VISIBLE);
            statusText.setText("Biometric setup complete, Okta login required");
        }
    }
    
    private void setupClickListeners() {
        setupBiometricButton.setOnClickListener(v -> showBiometricPrompt());
        authenticateButton.setOnClickListener(v -> showBiometricPrompt());
        switchToTraditionalButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AuthenticationChoiceActivity.class));
            finish();
        });
    }
    
    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            showError("Biometric authentication is not available");
        }
    }
    
    private void performOktaAuthentication() {
        showLoading(true);
        statusText.setText("Authenticating with Okta...");
        
        webAuthClient.signIn(this, new RequestCallback<AuthorizationStatus, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull AuthorizationStatus result) {
                if (result == AuthorizationStatus.AUTHORIZED) {
                    getUserInfo();
                } else {
                    showLoading(false);
                    showError("Okta authorization failed");
                }
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Okta login error: " + error, exception);
                showError("Okta login failed: " + error);
            }
        });
    }
    
    private void checkExistingOktaSession() {
        showLoading(true);
        statusText.setText("Checking session...");
        
        sessionClient.getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull UserInfo result) {
                showLoading(false);
                navigateToHome(result);
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                // Session expired or invalid, need to re-authenticate with Okta
                performOktaAuthentication();
            }
        });
    }
    
    private void getUserInfo() {
        sessionClient.getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull UserInfo result) {
                showLoading(false);
                
                // Save login and biometric setup state
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putBoolean(KEY_BIOMETRIC_SETUP, true)
                    .apply();
                
                navigateToHome(result);
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Error getting user info: " + error, exception);
                showError("Failed to get user information");
            }
        });
    }
    
    private void navigateToHome(UserInfo userInfo) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("userInfo", userInfo.toString());
        intent.putExtra("authMethod", "biometric");
        startActivity(intent);
        finish();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        authenticateButton.setEnabled(!show);
        setupBiometricButton.setEnabled(!show);
    }
    
    private void showError(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
