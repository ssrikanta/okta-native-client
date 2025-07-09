package com.example.oktanativeclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.okta.oidc.OIDCConfig;
import com.okta.oidc.Okta;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.clients.sessions.SessionClient;
import com.okta.oidc.clients.web.WebAuthClient;
import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.storage.security.DefaultEncryptionManager;
import com.okta.oidc.util.AuthorizationException;

public class HomeActivity extends AppCompatActivity {
    
    private static final String TAG = "HomeActivity";
    private static final String PREFS_NAME = "OktaPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private WebAuthClient webAuthClient;
    private SessionClient sessionClient;
    
    private TextView welcomeText;
    private TextView userInfoText;
    private TextView authMethodText;
    private Button refreshTokenButton;
    private Button changeAuthMethodButton;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        initViews();
        initOkta();
        loadUserInfo();
        setupClickListeners();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        welcomeText = findViewById(R.id.welcome_text);
        userInfoText = findViewById(R.id.user_info_text);
        authMethodText = findViewById(R.id.auth_method_text);
        refreshTokenButton = findViewById(R.id.refresh_token_button);
        changeAuthMethodButton = findViewById(R.id.change_auth_method_button);
        progressBar = findViewById(R.id.progress_bar);
        
        // Display authentication method
        String authMethod = getIntent().getStringExtra("authMethod");
        if (authMethod != null && authMethod.equals("biometric")) {
            authMethodText.setText("Authenticated with: Biometric + Okta");
        } else {
            authMethodText.setText("Authenticated with: Traditional Okta Login");
        }
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
        refreshTokenButton.setOnClickListener(v -> refreshAccessToken());
        changeAuthMethodButton.setOnClickListener(v -> {
            // Navigate to authentication choice
            Intent intent = new Intent(this, AuthenticationChoiceActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadUserInfo() {
        showLoading(true);
        
        sessionClient.getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull UserInfo result) {
                showLoading(false);
                displayUserInfo(result);
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Error loading user info: " + error, exception);
                showError("Failed to load user information");
            }
        });
    }
    
    private void displayUserInfo(UserInfo userInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(userInfo.get("name")).append("\n");
        sb.append("Email: ").append(userInfo.get("email")).append("\n");
        sb.append("Sub: ").append(userInfo.get("sub")).append("\n");
        sb.append("Preferred Username: ").append(userInfo.get("preferred_username")).append("\n");
        
        welcomeText.setText("Welcome, " + userInfo.get("name") + "!");
        userInfoText.setText(sb.toString());
    }
    
    private void refreshAccessToken() {
        showLoading(true);
        
        sessionClient.refreshToken(new RequestCallback<Void, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull Void result) {
                showLoading(false);
                Toast.makeText(HomeActivity.this, "Token refreshed successfully", Toast.LENGTH_SHORT).show();
                loadUserInfo(); // Reload user info with new token
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Error refreshing token: " + error, exception);
                showError("Failed to refresh token");
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void performLogout() {
        showLoading(true);
        
        webAuthClient.signOutOfOkta(this, new RequestCallback<Integer, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull Integer result) {
                showLoading(false);
                
                // Clear login state
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                
                // Navigate back to login
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            
            @Override
            public void onError(String error, AuthorizationException exception) {
                showLoading(false);
                Log.e(TAG, "Error during logout: " + error, exception);
                showError("Logout failed: " + error);
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        refreshTokenButton.setEnabled(!show);
        changeAuthMethodButton.setEnabled(!show);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
