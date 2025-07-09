# Okta Native Client - Android App with Biometric Authentication

A secure Android native application that integrates with Okta using OAuth 2.0 with PKCE flow, enhanced with biometric authentication (Face ID/Fingerprint) support.

## 🔐 Authentication Methods

The app supports two authentication methods:

### 1. Traditional Okta Authentication
- Standard OAuth 2.0 with PKCE flow
- Browser-based authentication
- Secure token storage

### 2. Biometric + Okta Authentication
- Face ID, fingerprint, or other biometric authentication
- Combined with Okta OAuth for enhanced security
- Seamless user experience

## 🚀 Features

- **Dual Authentication Options**: Users can choose between traditional and biometric authentication
- **Secure Token Management**: Encrypted token storage with refresh capabilities
- **Modern Material Design UI**: Clean, intuitive interface with proper loading states
- **Biometric Availability Check**: Automatic detection of device biometric capabilities
- **Session Management**: Persistent login with secure logout
- **Authentication Method Switching**: Users can change their preferred authentication method

## 📱 App Flow

1. **Main Activity**: Checks authentication status and preferences
2. **Authentication Choice**: User selects preferred authentication method
3. **Authentication Process**: 
   - Traditional: Direct Okta OAuth flow
   - Biometric: Biometric verification → Okta OAuth flow
4. **Home Activity**: Displays user information and provides app actions

## 🔧 Technical Implementation

### Dependencies
```gradle
// Okta OIDC Android SDK
implementation 'com.okta.android:oidc-androidx:1.3.1'

// Biometric authentication
implementation 'androidx.biometric:biometric:1.1.0'

// Material Design
implementation 'com.google.android.material:material:1.11.0'
```

### Key Components

#### Activities
- `MainActivity.java` - Entry point and routing logic
- `AuthenticationChoiceActivity.java` - Authentication method selection
- `LoginActivity.java` - Traditional Okta authentication
- `BiometricAuthActivity.java` - Biometric + Okta authentication
- `HomeActivity.java` - User dashboard with session management

#### Security Features
- **PKCE Implementation**: Enhanced OAuth security for mobile apps
- **Biometric Authentication**: Uses Android BiometricPrompt API
- **Secure Storage**: Encrypted SharedPreferences for sensitive data
- **Session Validation**: Token refresh and expiration handling

### Biometric Authentication Flow

1. **Availability Check**: Verify device biometric capabilities
2. **First-Time Setup**: 
   - Biometric authentication
   - Okta OAuth flow
   - Save preferences
3. **Subsequent Logins**:
   - Biometric authentication
   - Check existing Okta session
   - Refresh tokens if needed
   - Open `app/src/main/res/raw/okta_oidc_config.json`
   - Update the following values:
     ```json
     {
       "client_id": "YOUR_ACTUAL_CLIENT_ID",
       "redirect_uri": "com.oktapreview.YOUR_DOMAIN:/callback",
       "end_session_redirect_uri": "com.oktapreview.YOUR_DOMAIN:/logout",
       "scopes": [
         "openid",
         "profile",
         "email",
         "offline_access"
       ],
       "discovery_uri": "https://YOUR_DOMAIN.okta.com/oauth2/default/.well-known/openid_configuration"
     }
     ```

3. **Update AndroidManifest.xml**
   - Open `app/src/main/AndroidManifest.xml`
   - Update the redirect URI scheme in the intent filter:
     ```xml
     <data android:scheme="com.oktapreview.YOUR_DOMAIN" />
     ```

4. **Build and Run**
   - Open the project in Android Studio
   - Sync the project with Gradle
   - Run the application on an emulator or physical device

## Application Flow

1. **Splash/Main Activity**: Checks if user is already authenticated
2. **Login Activity**: Presents Okta login interface using system browser
3. **Authentication**: Okta handles authentication with PKCE flow
4. **Home Activity**: Displays user information and provides logout option

## Architecture

- **MainActivity**: Entry point that routes to login or home based on auth status
- **LoginActivity**: Handles Okta authentication flow
- **HomeActivity**: Displays authenticated user data and provides logout
- **SharedPreferenceStorage**: Secure storage implementation for Okta SDK
- **Material Design**: Modern UI with cards, proper spacing, and Material theming

## Security Features

- **PKCE Flow**: Enhanced security for mobile applications
- **Encrypted Storage**: Sensitive data is encrypted using Android Keystore
- **Secure Logout**: Proper session cleanup and token revocation
- **Certificate Pinning Ready**: Easy to add certificate pinning for production

## Dependencies

- **Okta OIDC Android SDK**: Official Okta SDK for Android
- **AndroidX Libraries**: Modern Android development libraries
- **Material Components**: Material Design UI components
- **OkHttp**: HTTP client for secure network communication
- **Gson**: JSON parsing library

## Testing

Run the application and test the following scenarios:

1. **First Launch**: Should redirect to login
2. **Authentication**: Should open Okta login in browser
3. **Successful Login**: Should redirect to home with user info
4. **Token Refresh**: Test the refresh token functionality
5. **Logout**: Should clear session and redirect to login
6. **App Restart**: Should maintain login state

## Troubleshooting

### Common Issues

1. **Redirect URI Mismatch**
   - Ensure the redirect URI in Okta matches your app configuration
   - Check that the scheme in AndroidManifest.xml matches your Okta app

2. **Network Issues**
   - Verify internet connectivity
   - Check if your Okta domain is accessible
   - Ensure proper network security configuration

3. **Authentication Errors**
   - Verify client ID and discovery URI
   - Check Okta application configuration
   - Ensure PKCE is enabled in Okta

### Logs

Enable debug logging to troubleshoot issues:
- Check Android Studio Logcat for detailed error messages
- Look for tags: `LoginActivity`, `HomeActivity`, `OktaOIDC`

## Production Considerations

Before deploying to production:

1. **Security**
   - Enable certificate pinning
   - Use release keystore
   - Enable ProGuard/R8 obfuscation

2. **Configuration**
   - Use build variants for different environments
   - Store sensitive configuration securely

3. **Performance**
   - Test on various devices and Android versions
   - Implement proper error handling
   - Add analytics and crash reporting

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues related to:
- **Okta SDK**: Visit [Okta Developer Documentation](https://developer.okta.com)
- **Android Development**: Check [Android Developer Documentation](https://developer.android.com)
- **This Project**: Open an issue in the repository
