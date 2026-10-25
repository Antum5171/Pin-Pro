# PinPro Setup & Deployment Guide

This guide provides step-by-step instructions for setting up, building, running, and deploying the PinPro Android application.

---

## 📋 System Prerequisites

Ensure your development workstation meets the following requirements:
- **Operating System**: macOS, Linux, or Windows 11.
- **Java Development Kit (JDK)**: OpenJDK 17 or Java 17 LTS.
- **Android Studio**: Android Studio Jellyfish (2023.3.1) or newer.
- **Android SDK**: SDK Platform 34 (Android 14) and Build-Tools 34.0.0.
- **Gradle**: Gradle 8.4+ (uses standard `gradle` binary).
- **Git**: Git 2.35+.

---

## 🚀 Step-by-Step Setup Instructions

### Step 1: Clone the Repository
```bash
git clone https://github.com/your-org/pinpro-android.git
cd pinpro-android
```

### Step 2: Configure Environment Variables & Secrets
PinPro manages API credentials securely through `.env` files and Android's `BuildConfig`.

1. Copy the example configuration file:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and configure your API keys:
   ```env
   # Google Maps Android API Key
   MAPS_API_KEY=AIzaSy...YourActualGoogleMapsKey Here
   ```
> **Security Note**: Never commit your `.env` file or hardcode keys inside `build.gradle.kts`. When developing in AI Studio, enter secrets into the **Secrets Panel**.

---

### Step 3: Firebase Provisioning & Authentication Setup

1. **Create a Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/) and create a project named `pinpro-app`.
2. **Add Android Application**:
   - Package name: `com.example` (or your configured `applicationId`).
   - Register your app and download `google-services.json`.
   - Place `google-services.json` into the `/app/` directory.
3. **Configure Google Sign-In via Credential Manager**:
   - Go to **Firebase Console -> Authentication -> Sign-in method -> Google**.
   - Enable Google Sign-In and copy your Web Client ID.
   - Obtain your development SHA-1 certificate fingerprint using `keytool`:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Add the SHA-1 fingerprint under **Project Settings -> Android app credentials**.
4. **Deploy Firestore Security Rules**:
   ```bash
   firebase deploy --only firestore:rules
   ```

---

### Step 4: Building and Running the Application

#### Option A: Command Line Interface (CLI)
To assemble a debug APK via Gradle:
```bash
gradle assembleDebug
```
The generated APK will be available at:
`/app/build/outputs/apk/debug/app-debug.apk`

To execute local JVM Robolectric unit tests:
```bash
gradle :app:testDebugUnitTest
```

#### Option B: Android Studio
1. Launch Android Studio and click **Open**.
2. Select the `pinpro-android` root directory.
3. Wait for Gradle sync to complete.
4. Select target device (Streaming Emulator or Physical Android Device).
5. Click **Run** (`Shift + F10`).

---

## 🔒 Production Security Checklist

- [ ] **API Key Restrictions**: Restrict your `MAPS_API_KEY` in Google Cloud Console to your Android package name (`com.example`) and SHA-1 fingerprint.
- [ ] **Firestore Zero-Trust Rules**: Ensure `firestore.rules` is deployed to production to prevent unauthorized writes.
- [ ] **Google Sign-In Enforcement**: Verify that anonymous authentication is disabled and all Firestore reads/writes require Google Sign-In.
- [ ] **Debug Flags Disabled**: Ensure `debuggable` is set to `false` in release build types.
