# PinPro Real-Time Notification System

This document outlines the notification architecture, event dispatch matrix, payload specifications, and Android system channel configurations for PinPro.

---

## 🔔 Overview

PinPro incorporates a bidirectional real-time notification engine that keeps both barangay officials and residents updated at every step of an incident response. Notifications are delivered simultaneously via:
1. **In-App Top Banner Alerts**: High-visibility sliding banners with alert sounds and subtle haptic feedback.
2. **Top Bar Badge Counter**: Unread notification counter badge in the main top app bar.
3. **Android System Notifications**: Standard system tray notifications via Firebase Cloud Messaging (FCM) and Android Notification Channels.

---

## 📋 Real-Time Notification Event Matrix

| Event | Recipient | Trigger Action | Notification Title | Notification Body | Target Screen / Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **New Report Submitted** | **ALL Officials** in Barangay | Resident submits new incident report. | `New report: [Category]` | `"[Title]" • [Address] • just now` | Opens Map centered on report with "Accept" panel active. |
| **Report Accepted** | **Submitting Resident** | Official accepts dispatch. | `Report Accepted` | `Your report was accepted. [Official Name] is responding.` | Opens Map focused on active tracking sheet. |
| **Responder En Route** | **Submitting Resident** | Official starts simulation or vehicle moves. | `Responder En Route` | `Responder is on the way. ETA: [X] mins.` | Opens Map focused on driver route view. |
| **Responder Arrived** | **Submitting Resident** | Vehicle within 30m or simulation finishes. | `Responder Arrived` | `Responder has arrived at your location.` | Displays arrival badge on tracking sheet. |
| **Report Resolved** | **Submitting Resident** | Official taps "Mark Resolved". | `Report Resolved` | `Your report has been marked resolved.` | Navigates to Records archive. |

*Note: Officials do not receive notifications for reports they submitted themselves.*

---

## 📦 Notification Payload Data Structure

System and in-app notifications pass serialized JSON payloads containing navigation metadata:

```json
{
  "id": "notif_7701",
  "title": "New report: Peace & Order",
  "body": "Fights Reported near Plaza • Capitol Drive • just now",
  "timestamp": "2026-10-25T09:30:00Z",
  "type": "NEW_REPORT",
  "reportId": "rep_982341",
  "category": "Peace & Order",
  "latitude": 14.67632,
  "longitude": 120.53614,
  "isRead": false,
  "targetRole": "OFFICIAL"
}
```

---

## 🛠️ Android Notification Channels & Permissions

### 1. Notification Channels
PinPro initializes two dedicated notification channels in `MainActivity.kt`:

- **Emergency Alerts Channel (`pinpro_emergency_channel`)**:
  - Importance: `IMPORTANCE_HIGH` (Vibration, Heads-Up Banner, Alarm Sound).
  - Used for: New Incident Reports submitted to Officials.
- **Status Updates Channel (`pinpro_status_channel`)**:
  - Importance: `IMPORTANCE_DEFAULT` (Standard System Sound & Tray Icon).
  - Used for: Accepted, On the way, Arrived, and Resolved updates sent to Residents.

### 2. Android 13+ (API 33) Runtime Permission
The app requests the `POST_NOTIFICATIONS` permission gracefully at startup:

```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (!isGranted) {
        // Fallback to in-app banners if system permission is denied
    }
}
```

If system notification permission is denied by the user, the app automatically falls back to displaying in-app sliding banners so critical alerts are never missed during active app usage.
