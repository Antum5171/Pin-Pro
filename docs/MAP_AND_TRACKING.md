# PinPro Map & Real-Time Tracking Engine

This document details the design, physics calculations, route polyline math, and smooth animation loop used in the PinPro map rendering and responder tracking engine.

---

## 🗺️ Map Architecture & Rendering Engine

PinPro uses a hardware-accelerated Jetpack Compose Canvas map rendering engine custom-tuned for **Brgy. Poblacion, Balanga City, Bataan** (`Latitude: 14.6763, Longitude: 120.5361`).

### 1. Map Layers & Styles
The top header provides a compact layers menu allowing seamless switching between 3 map styles:
- 🗺️ **Streets Mode** (Default): High contrast road grid, street names, and building outlines.
- 🛰️ **Satellite Mode**: High-resolution aerial imagery layer with translucent road overlays.
- 🌙 **Dark Mode**: Dark grey base theme optimized for night-time patrols and low light conditions.

### 2. Viewport & Touch Interaction
- **Center Focus**: Smooth pan-to-location animations when selecting an incident report from lists or notifications.
- **Zoom Constraints**: Default zoom level `16.5f`, detailed driver view zoom `18.5f`, min zoom `13.0f`, max zoom `20.0f`.
- **Gesture Control**: Dual-finger pinch-to-zoom, single-finger pan, double-tap zoom in, two-finger tap zoom out.

---

## 📍 Marker System

### 1. Incident Markers
- **Base Icon**: High-visibility pin marker with category-specific icon overlay (Medical Cross, Fire Flame, Peace Shield, Flood Wave, Alert Bell).
- **Status Color Coding**:
  - 🔴 **Red**: `REPORTED` (Pending official acceptance).
  - 🟠 **Orange**: `ACCEPTED` (Official assigned, preparing response).
  - 🟢 **Green**: `ON_THE_WAY` (Active responder en route).
  - 🔵 **Blue**: `ARRIVED` (Responder on site).

### 2. Responder Patrol Marker
- **Visual Design**: Sleek patrol vehicle icon with direction arrow indicator and attached floating badge displaying `PATROL • X MIN`.
- **Dynamic Rotation**: Vehicle marker continuously rotates to match its actual driving direction along road segments.

---

## 🚘 Smooth Responder Physics & Simulation Loop

The simulation system powers realistic, smooth vehicle movement along road polylines without sudden snapping or straight-line teleportation.

```text
 Polyline Points: P0 -------> P1 ---------------> P2 --------> Target (Incident)
 Progress:         [=========|============>      ]
 Route Trimming:   (Trimmed)  (Remaining Green Line)
```

### 1. 60 FPS Frame Loop & Interpolation
Vehicle position is updated approximately 60 times per second using Kotlin Coroutines and Compose animation frames:

$$\Delta t = t_{\text{current}} - t_{\text{last}}$$

$$\text{Distance Traveled} = v \cdot \text{Speed Multiplier} \cdot \Delta t$$

where baseline driving velocity $v \approx 35 \text{ km/h} \approx 9.72 \text{ m/s}$.

### 2. Cornering Speed Adjustment
To mimic realistic driving behavior, the simulation automatically calculates the turn angle $\theta_{\text{turn}}$ between adjacent polyline segments:

$$\theta_{\text{turn}} = |\text{Heading}(P_k \to P_{k+1}) - \text{Heading}(P_{k-1} \to P_k)|$$

If $\theta_{\text{turn}} > 45^\circ$, the vehicle velocity is slowed down dynamically:

$$v_{\text{corner}} = v_{\text{base}} \cdot \max\left(0.4, \cos\left(\frac{\theta_{\text{turn}}}{2}\right)\right)$$

### 3. Smooth Vehicle Rotation (Angular Easing)
The target heading angle $\phi_{\text{target}}$ is calculated from segment displacement:

$$\phi_{\text{target}} = \text{atan2}(\Delta \text{Lng} \cdot \cos(\text{Lat}), \Delta \text{Lat}) \cdot \frac{180}{\pi}$$

To prevent sudden snaps at turns, angular rotation is interpolated using shortest arc easing:

$$\Delta \phi = ((\phi_{\text{target}} - \phi_{\text{current}} + 540^\circ) \bmod 360^\circ) - 180^\circ$$

$$\phi_{\text{next}} = \phi_{\text{current}} + \alpha \cdot \Delta \phi \quad (\text{where } \alpha \approx 0.15)$$

### 4. Dynamic Route Trimming
As the responder advances along the route line:
- The segment of the polyline behind the vehicle is dynamically trimmed and removed.
- Only the remaining polyline ahead of the vehicle is drawn on the map in vibrant neon green (`#39FF14`).

### 5. Driver Camera Follow Mode
In Driver View mode, the map camera continuously smoothly pans (`panTo`) to keep the responder vehicle centered in the lower third of the screen, providing optimal forward visibility along the remaining route.

### 6. Auto-Arrival Detection
For both real GPS tracking and simulation mode:
- Distance to incident is evaluated every frame using the Haversine formula.
- When $\text{Distance} \le 30 \text{ meters}$ or simulation reaches the final point:
  - Report status is automatically changed to `ARRIVED`.
  - Notification is dispatched to the submitting resident.
  - Vehicle speed drops to zero.
