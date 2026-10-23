# PinPro - Barangay Emergency and Complaint Alert App (Philippines)

PinPro is a modern, light-themed emergency and complaint dispatch system built for local government units (LGUs) and barangays across the Philippines. Residents can immediately send their geo-located emergency report, automatically routing to whichever barangay they are physically in via point-in-polygon boundary detection, and track the dispatched Barangay Tanod in real-time with an interactive Grab/Lalamove-style map experience.

---

## 🚀 Key Features

1. **Role-Based Workflows**:
   - **Resident**: 4-step registration with dummy OTP (123456) and dummy ID verification, Data Privacy Act (RA 10173) consent, 1-tap neon green "SEND ALERT" button, multi-category selection (Medical, Fire, Crime / Peace and Order, Flood / Disaster, Other Complaint), photo attachment, live Grab-style map tracking with Tanod badge & contact, status stepper, and 5-star resolution rating.
   - **Barangay Admin / Dispatcher**: Scoped dashboard for their specific barangay. Real-time alert list with incoming slide-in neon green highlight, interactive map displaying barangay boundaries, hall coordinates, alert pins, and active tanods, instant Tanod dispatching, resident suspension/reinstatement, tanod roster management, and "Simulate incoming alert".
   - **Tanod Mobile View**: Dedicated interface for assigned responders. Status controls ("Accept", "On the way" with smooth animated route simulation, "Arrived", "Resolved"), direct resident calling/messaging.
2. **Multi-Barangay Geo-Routing**:
   - Point-in-polygon detection for **Barangay Sample Hall** and **Barangay Riverside**.
   - If a resident sends an alert from inside a neighboring barangay, the alert is automatically routed to the physical barangay with a "Sent from outside home barangay" badge.
   - If outside covered barangays, 911 quick-call and fallback dispatch are presented.
3. **Demo & Simulation Controls**:
   - Floating Demo Panel to switch between roles or 1-click log in as seeded accounts (Maria Santos, Jose Reyes, Liza Ramos, Kapitan Ramon Villanueva, Kapitana Elena Cruz, Tanod Berto Aquino, etc.).
   - "Demo: set my location" override selector ("Inside Barangay Sample Hall", "Inside Barangay Riverside", "Outside both barangays", "Use real GPS").
   - "Seed demo data" and "Reset demo" buttons.

---

## 🎨 Visual Design Guidelines

- **Primary Accent**: Neon Green (`#39FF14`) used strictly as fill for key action buttons, active tags, route polylines, and live location pins.
- **Background**: Clean crisp white (`#FFFFFF`) and surfaces (`#F5F6F5`) with subtle borders (`#E4E6E4`).
- **Typography**: Google Fonts Inter with strictly 3 font sizes:
  - **Large (28sp)**: Screen titles, ETA, key numbers.
  - **Base (16sp)**: Body text, buttons, inputs, list items.
  - **Small (12sp)**: Labels, timestamps, error text, helper text.
- **Touch Targets**: Minimum 48dp x 48dp on all interactive elements.

---

## 🛠️ Firebase Backend Setup (Production & Web / Cloud Deployments)

When deploying with live Cloud Firestore & Firebase Auth:

1. **Firebase Console**:
   - Create a project in [Firebase Console](https://console.firebase.google.com).
   - Enable **Authentication** -> Sign-in method -> Email/Password.
   - Enable **Cloud Firestore** in Production mode.
   - Enable **Firebase Storage** (or use client-side compressed base64).
2. **Environment Variables**:
   Provide the following configuration variables:
   ```env
   VITE_FIREBASE_API_KEY=your_api_key
   VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
   VITE_FIREBASE_PROJECT_ID=your_project_id
   VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
   VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
   VITE_FIREBASE_APP_ID=your_app_id
   VITE_GOOGLE_MAPS_API_KEY=your_maps_api_key_optional
   ```
3. **Deploy Security Rules & Indexes**:
   ```bash
   firebase deploy --only firestore:rules
   firebase deploy --only firestore:indexes
   ```
4. **Seed Demo Data**:
   Use the built-in "Seed demo data" action to instantly populate both barangays, official accounts, tanods, and past incident histories.
