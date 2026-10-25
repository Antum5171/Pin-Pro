# PinPro Database Architecture & Schema

This document outlines the Firebase Cloud Firestore database architecture, collection schemas, status state machine rules, and zero-trust security policies for PinPro.

---

## 🗄️ Database Architecture Overview

PinPro uses **Firebase Cloud Firestore** as its primary cloud database service, paired with a reactive repository pattern (`PinProRepository.kt`) on the Android client. Real-time snapshot listeners ensure that all data changes (such as new reports, status updates, or location movements) propagate instantly to all connected mobile clients.

---

## 📊 Firestore Schema Collections

### 1. `reports` Collection

Stores all incident reports submitted by residents in the barangay.

```json
{
  "id": "rep_982341",
  "title": "Fights Reported near Plaza",
  "category": "Peace & Order",
  "description": "Loud fight involving 3 individuals near the Poblacion basketball court.",
  "address": "Capitol Drive corner Zulueta St, Brgy. Poblacion, Balanga City",
  "latitude": 14.67632,
  "longitude": 120.53614,
  "barangay": "Brgy. Poblacion",
  "residentId": "user_res_101",
  "residentName": "Maria Santos",
  "residentPhone": "+639171234567",
  "status": "ON_THE_WAY",
  "assignedOfficialId": "user_off_202",
  "assignedOfficialName": "Tanod Berto Aquino",
  "etaMinutes": 4,
  "distanceKm": 1.2,
  "imageUrl": "https://storage.googleapis.com/.../photo.jpg",
  "createdAt": "2026-10-25T09:30:00Z",
  "updatedAt": "2026-10-25T09:32:15Z",
  "resolvedAt": null
}
```

### 2. `officials` Collection

Stores registered barangay officials, responders, and their active status.

```json
{
  "id": "user_off_202",
  "name": "Tanod Berto Aquino",
  "role": "OFFICIAL",
  "title": "Senior Barangay Tanod",
  "barangay": "Brgy. Poblacion",
  "phone": "+639189876543",
  "isAvailable": true,
  "currentLatitude": 14.67710,
  "currentLongitude": 120.53580,
  "activeReportId": "rep_982341"
}
```

### 3. `events` Subcollection (`reports/{reportId}/events`)

Maintains an immutable audit log of every state change and milestone for an incident report.

```json
{
  "id": "evt_5501",
  "reportId": "rep_982341",
  "eventType": "REPORT_ACCEPTED",
  "timestamp": "2026-10-25T09:31:00Z",
  "actorId": "user_off_202",
  "actorName": "Tanod Berto Aquino",
  "notes": "Official accepted dispatch and began route preparation."
}
```

---

## 🔄 Status State Machine Rules

Incident reports follow a strict linear state transition sequence. Illegal transitions (e.g., jump from `REPORTED` directly to `RESOLVED` without acceptance) are blocked at the repository level and by Firestore security rules.

```text
 [REPORTED] ----(Official Accepts)----> [ACCEPTED]
                                            |
                                  (Official Starts Driving)
                                            v
 [RESOLVED] <---(Official Resolves)---- [ARRIVED] <---(GPS < 30m / Sim End)--- [ON_THE_WAY]
```

| State | Description | Next Allowed State | Trigger Action |
| :--- | :--- | :--- | :--- |
| `REPORTED` | Initial submission by resident. | `ACCEPTED` | Official taps "Accept Incident Response". |
| `ACCEPTED` | Official assigned to respond. | `ON_THE_WAY` | Official taps "Start Simulation / Drive". |
| `ON_THE_WAY` | Responder en route; live route visible. | `ARRIVED` | Vehicle arrives within 30m or simulation finishes. |
| `ARRIVED` | Responder physically present on site. | `RESOLVED` | Official taps "Mark Resolved". |
| `RESOLVED` | Incident concluded and archived. | *Terminal* | Report moved from map to Records tab. |

---

## 🔒 Firestore Security Rules (`firestore.rules`)

PinPro enforces zero-trust security policies to protect resident privacy and restrict official actions:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOfficial() {
      return isAuthenticated() && 
        request.auth.token.role == 'OFFICIAL';
    }

    function isReportOwner(residentId) {
      return isAuthenticated() && 
        request.auth.uid == residentId;
    }

    // Reports Collection Security Rules
    match /reports/{reportId} {
      // Anyone authenticated can read reports in their barangay
      allow read: if isAuthenticated();

      // Residents can create reports with their own UID
      allow create: if isAuthenticated() && 
        request.resource.data.residentId == request.auth.uid &&
        request.resource.data.status == 'REPORTED';

      // Officials can update status, assign themselves, or mark resolved
      allow update: if isOfficial() || (
        isReportOwner(resource.data.residentId) && 
        request.resource.data.diff(resource.data).affectedKeys().hasOnly(['description', 'imageUrl'])
      );

      // Audit Events Subcollection
      match /events/{eventId} {
        allow read: if isAuthenticated();
        allow create: if isAuthenticated();
      }
    }

    // Officials Roster Security Rules
    match /officials/{officialId} {
      allow read: if isAuthenticated();
      allow write: if isOfficial();
    }
  }
}
```
