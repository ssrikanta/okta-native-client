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

import com.okta.oidc.AuthorizationStatus;
import com.okta.oidc.OIDCConfig;
import com.okta.oidc.Okta;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.ResultCallback;
import com.okta.oidc.clients.sessions.SessionClient;
import com.okta.oidc.clients.web.WebAuthClient;
import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.storage.security.DefaultEncryptionManager;
import com.okta.oidc.util.AuthorizationException;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "OktaPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private WebAuthClient webAuthClient;
    private SessionClient sessionClient;
    
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        initOkta();
        setupClickListeners();
    }
    
    private void initViews() {
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
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
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
    }
    
    private void performLogin() {
        showLoading(true);
        statusText.setText("Authenticating...");
        
        webAuthClient.signIn(this, new RequestCallback<AuthorizationStatus, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull AuthorizationStatus result) {
                if (result == AuthorizationStatus.AUTHORIZED) {
                    // Get user info after successful login
                    getUserInfo();
                } else {
                    showLoading(false);
                    showError("Authorization failed");
                }
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Login error: " + error, exception);
                showError("Login failed: " + error);
            }
        });
    }
    
    private void getUserInfo() {
        sessionClient.getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull UserInfo result) {
                showLoading(false);
                
                // Save login state
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
                
                // Navigate to home activity
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("userInfo", result.toString());
                intent.putExtra("authMethod", "traditional");
                startActivity(intent);
                finish();
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Error getting user info: " + error, exception);
                showError("Failed to get user information");
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
    }
    
    private void showError(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
