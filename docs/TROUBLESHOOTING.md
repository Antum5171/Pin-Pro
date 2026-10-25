# PinPro Troubleshooting & Diagnostic Guide

This guide provides diagnostic steps and solutions for common issues encountered during development, testing, and deployment of PinPro.

---

## 🔍 Diagnostics Quick Matrix

| Problem Category | Symptom | Primary Cause | Solution |
| :--- | :--- | :--- | :--- |
| **Map Engine** | Map display is blank or grid tiles show grey grid. | Missing or invalid `MAPS_API_KEY` in `.env`. | Add valid Google Maps key to `.env` or Secrets panel. Verify `INTERNET` permission in `AndroidManifest.xml`. |
| **Tracking / Physics** | Patrol vehicle jumps or teleports instead of moving smoothly. | Coroutine animation loop paused or frame delta calculation dropped. | Ensure 60 FPS interpolation loop is active in `BalangaGoogleMapCanvas.kt`. Check simulation speed multiplier. |
| **Notifications** | Alerts not appearing when reports are submitted or accepted. | Android 13+ `POST_NOTIFICATIONS` permission denied or role mismatch. | Grant notification permissions in Android settings. Verify user role in Profile tab. |
| **Firestore DB** | Console shows `Missing or insufficient permissions` error. | Firestore security rules blocking request or missing auth token. | Run `firebase deploy --only firestore:rules`. Verify user is logged in via Google Sign-In. |
| **Build & Gradle** | Build fails with `cannot find symbol` or dependency conflict. | Unresolved Gradle version catalog alias or outdated SDK setup. | Check `gradle/libs.versions.toml` formatting. Run `gradle assembleDebug` to re-sync dependencies. |

---

## 🛠️ Step-by-Step Problem Resolution

### 1. Map Tiles Not Loading or Blank Canvas
- **Symptoms**: The map area renders as a blank white/grey area or displays missing tile grid boxes.
- **Diagnostic Steps**:
  1. Open Logcat or build logs and filter by tag `PinProMap`.
  2. Verify that `MAPS_API_KEY` is set correctly in `.env` or AI Studio Secrets.
  3. Check device network connectivity.
- **Resolution**:
  - Ensure the API key has the **Maps SDK for Android** enabled in Google Cloud Console.
  - Verify that the application package `com.example` and SHA-1 fingerprint are registered under Key Restrictions.

---

### 2. Notifications Not Delivered to Officials or Residents
- **Symptoms**: Submitting a report does not trigger a notification on the official's screen, or accepting a report does not update the resident.
- **Diagnostic Steps**:
  1. Verify the current role using the role indicator in the Profile tab.
  2. Check if the app has system notification permission.
- **Resolution**:
  - For Android 13+ (API 33+), ensure runtime permission `POST_NOTIFICATIONS` was accepted.
  - If system notifications are disabled, PinPro automatically displays in-app sliding banners while the app is active.

---

### 3. Firestore `Missing or insufficient permissions` Error
- **Symptoms**: Console displays Firestore exception when attempting to submit a report or update status.
- **Diagnostic Steps**:
  1. Check `firestore.rules` file in the project root.
  2. Check Firebase Auth state via `PinProRepository.kt`.
- **Resolution**:
  - Authenticate the user via Google Sign-In before attempting database writes.
  - Deploy updated security rules using `firebase deploy --only firestore:rules`.

---

### 4. Vehicle Simulation Erratic or Route Trimming Frozen
- **Symptoms**: Patrol vehicle marker rotates wildly or green route line does not trim behind responder.
- **Diagnostic Steps**:
  1. Inspect the polyline waypoint list passed to `BalangaGoogleMapCanvas.kt`.
  2. Verify that waypoint coordinates are sorted sequentially along the road network.
- **Resolution**:
  - Check heading angle calculation math in `MAP_AND_TRACKING.md`.
  - Ensure shortest arc angular easing is active (`deltaAngle` modulo math).
