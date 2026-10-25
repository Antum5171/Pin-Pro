# PinPro - Barangay Incident Reporting & Responder Tracking System

![PinPro Logo](Logo.png)

**PinPro** is a modern, real-time community incident reporting and emergency responder tracking application designed for local government units (LGUs) and barangays in the Philippines, starting with **Brgy. Poblacion, Balanga City, Bataan**.

PinPro bridges the gap between residents needing urgent assistance and local barangay officials/tanods. Residents can submit geo-located incident reports with one tap, track dispatched responders in real-time with smooth road-following physics and live ETAs, and receive instant push and in-app notifications at every stage of incident resolution.

---

## 🚀 Key Features

- 📍 **Instant Incident Reporting**: One-tap reporting for Medical, Fire, Peace & Order (Fights), Flood/Disaster, and General Complaints with precise pin placement on interactive maps of Brgy. Poblacion.
- 🔔 **Real-Time Notification System**: Instant alerts sent to all barangay officials when a resident submits a report, and direct update notifications to residents as officials accept, respond, arrive, and resolve incidents.
- 🚘 **Smooth Responder Simulation & Driver View**: Realistic vehicle movement along actual road paths with 60 FPS interpolation, cornering speed adjustment, smooth heading rotation, camera follow mode, and dynamic route trimming.
- 🗺️ **Compact & Responsive Map Interface**: Single-row adaptive map header with quick map style switching (Streets, Satellite, Dark), single-tap filters, and safe-area aware top spacing.
- 📱 **Expandable Tracking Sheet**: Draggable bottom sheet panel (35dvh collapsed, 60dvh expanded) with status timeline, ETA badge, distance indicator, and speed controls (1x, 2x, 4x).
- 🔐 **Secure Multi-Role Access**: Google Sign-In authentication via Jetpack Credential Manager and Firebase Firestore zero-trust security rules separating Resident and Official privileges.
- 📋 **Incident History & Records**: Automatic archiving of resolved reports into searchable historical records with audit logs.

---

## 🛠️ Tech Stack

| Domain | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 1.9+ |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Architecture** | MVVM (Model-View-ViewModel) with StateFlow |
| **Map Rendering** | Compose Canvas & Osmdroid Tile Engine (Balanga City, Bataan Focus) |
| **Backend & Database** | Firebase Firestore (Cloud DB) |
| **Authentication** | Google Sign-In (Jetpack `CredentialManager`) & Firebase Auth |
| **Async & Loops** | Kotlin Coroutines & Flow (`requestAnimationFrame` equivalent animation loops) |
| **Testing** | Robolectric Local JVM Testing Framework |
| **Min / Target SDK** | Android 7.0 (API 24) / Android 14 (API 34) |

---

## ⚡ Quick Start

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-org/pinpro-android.git
   cd pinpro-android
   ```

2. **Configure Environment Secrets**:
   Copy `.env.example` to your active environment or configure variables in AI Studio Secrets / Gradle:
   ```bash
   cp .env.example .env
   ```

3. **Build the Project**:
   ```bash
   gradle assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

For detailed setup, Firebase provisioning, and map key instructions, see [docs/SETUP.md](docs/SETUP.md).

---

## 👥 User Roles & Permissions

| Feature / Action | Resident | Barangay Official / Tanod |
| :--- | :---: | :---: |
| Submit New Incident Report | ✅ | ❌ |
| View Active Reports Map | ✅ (Own + General) | ✅ (All Barangay Incidents) |
| Receive New Report Notification | ❌ | ✅ (All Officials in Barangay) |
| Accept Incident Dispatch | ❌ | ✅ |
| Simulate / Control Responder Vehicle | ❌ | ✅ |
| View Live Responder ETA & Route | ✅ | ✅ |
| Auto-Arrive (GPS < 30m / Simulation End) | ❌ | ✅ (Automatic) |
| Mark Incident Resolved | ❌ | ✅ |
| Access Resolved Records & History | ✅ (Personal) | ✅ (Barangay-wide Archive) |

---

## 📸 Application Screenshots

| Screen | Description | Path |
| :--- | :--- | :--- |
| **Home Dashboard** | Overview of recent incidents, quick report button, and barangay emergency contacts. | `docs/images/home.png` |
| **Map View** | Interactive map of Brgy. Poblacion with incident markers and compact top edge header. | `docs/images/map.png` |
| **Responder Tracking** | Live driver view showing smooth vehicle rotation, dynamic route trimming, and ETA sheet. | `docs/images/tracking.png` |
| **Records Archive** | Searchable list of resolved incidents with status timeline histories. | `docs/images/records.png` |

---

## 📁 Directory Structure

```text
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Application entry point & edge-to-edge window setup
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/Models.kt           # Data classes: Report, Official, Event, Barangay
│   │   │   │   │   └── repository/
│   │   │   │   │       └── PinProRepository.kt   # Central state repository & Firestore data layer
│   │   │   │   ├── ui/
│   │   │   │   │   ├── cyberzone/
│   │   │   │   │   │   ├── BalangaGoogleMapCanvas.kt         # Map tile engine & smooth vehicle canvas painter
│   │   │   │   │   │   ├── CyberzoneMainDashboard.kt        # Primary navigation & responsive layout container
│   │   │   │   │   │   ├── CyberzoneMapView.kt              # Interactive map screen, header & bottom sheet
│   │   │   │   │   │   ├── CyberzoneOtherTabs.kt            # Reports list, Submit form, Records & Profile tabs
│   │   │   │   │   │   └── GoogleMapsLocationPickerScreen.kt# Interactive location picker dialog
│   │   │   │   │   └── theme/
│   │   │   │   │       └── Theme.kt             # Material Design 3 color schemes & typography
│   │   │   │   └── util/                         # Helper utilities (Geocoding, Physics, Formatting)
│   │   │   ├── res/
│   │   │   │   ├── drawable/                     # App launcher icons & vector drawables
│   │   │   │   └── values/strings.xml           # App string resources
│   │   │   └── AndroidManifest.xml              # Manifest, permissions & launcher activity configuration
│   └── build.gradle.kts                         # App module Gradle configuration & dependencies
├── docs/                                        # Technical documentation
│   ├── ARCHITECTURE.md                          # System design, state flow & sequence diagrams
│   ├── MAP_AND_TRACKING.md                      # Canvas map rendering, route interpolation & physics math
│   ├── DATABASE.md                              # Firestore schema, status state machine & security rules
│   ├── NOTIFICATIONS.md                         # Real-time event notification engine & matrix
│   ├── SETUP.md                                 # Environment setup, Firebase provisioning & build steps
│   ├── TESTING.md                               # Robolectric test suite & CUJ verification scripts
│   └── TROUBLESHOOTING.md                       # Frequently encountered issues & diagnostic solutions
├── build.gradle.kts                             # Root build configuration
├── firestore.rules                              # Firebase Firestore zero-trust security rules
├── firestore.blueprint.json                     # Firebase database blueprint schema
├── Logo.png                                     # Official PinPro application brand logo
├── CHANGELOG.md                                 # Release history and feature changelog
└── README.md                                    # Main project documentation page
```

---

## 📚 Documentation Index

For in-depth architecture and development guides, refer to the documentation in the `docs/` folder:

1. [Architecture & System Flow](docs/ARCHITECTURE.md) - System overview, component breakdown, state flows, and Mermaid sequence diagrams.
2. [Map Rendering & Smooth Tracking](docs/MAP_AND_TRACKING.md) - Physics calculations, speed interpolation, heading rotation math, dynamic route trimming, and driver camera follow.
3. [Database & Security Rules](docs/DATABASE.md) - Firestore schema definitions, status state machine rules, and security guidelines.
4. [Notification Engine](docs/NOTIFICATIONS.md) - Detailed event matrix, payload structures, in-app banners, and system alerts.
5. [Setup & Deployment Guide](docs/SETUP.md) - Step-by-step setup guide for building, running, and configuring API keys.
6. [Testing Guide](docs/TESTING.md) - Robolectric testing suite, CUJ test scripts, and log screen instructions.
7. [Troubleshooting Guide](docs/TROUBLESHOOTING.md) - Solutions for common map, location, notification, and build errors.

---

## 📄 License

This project is licensed under the **MIT License** - see the `LICENSE` file for details.

---

## 🤝 Credits & Acknowledgments

- **Barangay Poblacion, Balanga City, Bataan** - Community domain context and geographic location mapping.
- **OpenStreetMap & Osmdroid** - Map tile provider and geographical coordinates data.
- **Android Jetpack & Material Design 3** - Modern Android UI components and styling guidelines.
