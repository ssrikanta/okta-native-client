# Okta Native Client - Android Application

A native Android application that demonstrates secure authentication using Okta with PKCE (Proof Key for Code Exchange) flow.

## Features

- **Secure Authentication**: Uses Okta OIDC SDK with PKCE flow
- **Modern UI**: Material Design components with a clean, modern interface
- **Token Management**: Automatic token refresh functionality
- **User Profile**: Displays authenticated user information
- **Secure Storage**: Encrypted storage for sensitive authentication data
- **Logout Functionality**: Complete logout with session cleanup

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API 23 or higher
- Okta Developer Account
- Java 8 or higher

## Okta Setup

1. **Create an Okta Developer Account**
   - Go to [developer.okta.com](https://developer.okta.com)
   - Sign up for a free developer account

2. **Create a New Application**
   - Navigate to Applications → Create App Integration
   - Choose "OIDC - OpenID Connect"
   - Select "Native Application"
   - Configure the following settings:
     - **App integration name**: Okta Native Client
     - **Grant type**: Authorization Code with PKCE
     - **Sign-in redirect URIs**: `com.oktapreview.dev-12345678:/callback`
     - **Sign-out redirect URIs**: `com.oktapreview.dev-12345678:/logout`

3. **Note Your Configuration**
   - **Client ID**: Found in the application settings
   - **Okta Domain**: Your Okta domain (e.g., `dev-12345678.okta.com`)
   - **Discovery URI**: `https://your-domain.okta.com/oauth2/default/.well-known/openid_configuration`

## Project Setup

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd okta-native-client
   ```

2. **Configure Okta Settings**
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
