# PinPro Changelog

All notable changes to the PinPro project are documented in this file.

---

## [1.1.0] - 2026-10-25

### 🎨 Brand & Icon Refresh
- **New App Logo Integration**: Fully updated application icon to `Logo.png` across launcher icons, adaptive mipmap assets, application manifest, and UI headers.
- **Marker Distinction**: Preserved red map pin strictly for incident location markers on interactive maps while using `Logo.png` for all brand identities and app icons.

### 🚘 Smooth Responder Physics & Simulation Engine
- **Road-Following Interpolation**: Implemented 60 FPS animation loop with linear waypoint interpolation along actual road paths instead of straight-line jumps.
- **Realistic Speed & Acceleration**: Added constant baseline driving speed (30–40 km/h) with dynamic slowdown at sharp turns (>45° angle).
- **Smooth Vehicle Rotation**: Added shortest-arc angular rotation easing (`atan2` math) to turn patrol markers smoothly without snapping.
- **Dynamic Route Trimming**: Green polyline route automatically trims behind responder vehicle as it advances.
- **Driver View Camera Follow**: Camera automatically pans (`panTo`) to keep responder vehicle centered in the lower third of the screen.
- **Auto-Arrival Trigger**: Automatically updates report status to `ARRIVED` when vehicle GPS drops below 30 meters from incident location or simulation finishes.

### 📱 Responsive Layout & UI Cleanup
- **Top Bar Edge Alignment**: Redesigned map header into a compact single-row bar (56px max height) pinned directly below safe-area top insets (`env(safe-area-inset-top)`).
- **Map Layers Dropdown**: Replaced horizontal text buttons with a compact layers icon button opening a dropdown menu (Streets, Satellite, Dark).
- **Expandable Bottom Sheet**: Created draggable tracking sheet supporting collapsed (35dvh max) and expanded (60dvh max) states with smooth swipe gestures.
- **Driver View Consolidation**: Removed top box overlay and moved all simulation controls (Pause/Resume, 1x/2x/4x speed toggle, ETA badge, Mark Resolved) into the bottom panel.

### 🔔 Real-Time Notification Engine
- **Official Alerts**: Instant notification sent to ALL officials in the barangay when a resident submits a new incident report (`"New report: [category]"`).
- **Resident Status Updates**: Automated push notifications dispatched to submitting residents when an official Accepts (`"Your report was accepted"`), Starts Driving (`"Responder en route"`), Arrives (`"Responder arrived"`), or Resolves (`"Report resolved"`).
- **In-App Banner Overlay**: Sliding top banner with haptic feedback and alert sound for active app sessions.

### 📚 Documentation Suite
- Created complete, production-grade GitHub Markdown documentation:
  - `README.md` (Root documentation & quick start)
  - `docs/ARCHITECTURE.md` (System architecture & Mermaid sequence diagrams)
  - `docs/MAP_AND_TRACKING.md` (Canvas map engine & smooth tracking math)
  - `docs/DATABASE.md` (Firestore schemas, status state machine & security rules)
  - `docs/NOTIFICATIONS.md` (Notification matrix, payload formats & channel setup)
  - `docs/SETUP.md` (Prerequisites, build steps & security checklist)
  - `docs/TESTING.md` (Robolectric test suite & CUJ manual test scripts)
  - `docs/TROUBLESHOOTING.md` (Diagnostics & common issue resolution)
  - `.env.example` (Template environment file)

---

## [1.0.0] - 2026-10-25

### 🚀 Initial Release
- Core incident reporting application for Brgy. Poblacion, Balanga City, Bataan.
- Multi-category submission (Medical, Fire, Peace & Order, Flood, General Complaint).
- Firebase Cloud Firestore database integration with offline persistence.
- Google Sign-In authentication via Jetpack Credential Manager.
- Basic interactive map view with status markers.
- Robolectric unit testing setup.
