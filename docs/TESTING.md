# PinPro Testing Guide & Test Suite

This document covers automated testing procedures, Robolectric unit test execution, and step-by-step Critical User Journey (CUJ) test scripts for PinPro.

---

## 🧪 Automated Testing Framework

PinPro utilizes **Robolectric** for local JVM unit testing, allowing fast execution of Android lifecycle, repository, and ViewModel tests without requiring a physical emulator or device.

### Executing Automated Tests
To run all automated unit tests, execute the following command:

```bash
gradle :app:testDebugUnitTest
```

### Test Suite Matrix (`ExampleRobolectricTest.kt`)

| Test Case Name | Target Feature / Component | Description | Result |
| :--- | :--- | :--- | :---: |
| `testReportSubmission` | `PinProRepository` | Verifies creating a new report with category, location, and initial `REPORTED` status. | PASSED |
| `testOfficialAcceptance` | `CyberzoneViewModel` | Verifies official assigning themselves and updating status to `ACCEPTED`. | PASSED |
| `testSimulationMovementAndArrival` | Physics Engine | Verifies interpolation along route polylines and auto-arrival trigger at 30m distance. | PASSED |
| `testReportResolution` | State Machine | Verifies status transition to `RESOLVED` and moving report to Records archive. | PASSED |
| `testNotificationGeneration` | Notification Engine | Verifies dispatching notifications to residents and officials based on role filters. | PASSED |
| `testRoleSwitching` | Auth / UI State | Verifies switching user context between Resident and Official modes. | PASSED |

---

## 📝 Critical User Journey (CUJ) Manual Test Scripts

Use these step-by-step scripts to verify application behavior end-to-end on a streaming emulator or physical test device.

---

### CUJ 1: Resident Incident Submission

**Objective**: Verify that a resident can submit a geo-located incident report with custom category and address.

1. Launch PinPro and ensure the active role is set to **Resident** (check profile tab or top indicator).
2. Tap the **Submit** tab (or green "+" action button on the Home tab).
3. Select an incident category (e.g., **"Peace & Order / Fights Reported"**).
4. Enter a title: `"Loud Fight near Plaza"` and description: `"Gathering near basketball court."`.
5. Tap **Choose Location on Map** and position the pin near Capitol Drive, Brgy. Poblacion.
6. Tap **Submit Report**.
7. **Expected Outcome**:
   - The report appears on the map as a red marker.
   - The status is set to `REPORTED`.
   - All officials in Brgy. Poblacion receive an instant app notification: `"New report: Peace & Order"`.

---

### CUJ 2: Official Dispatch Acceptance & Simulation Controls

**Objective**: Verify that an official can accept an incident, control the driver view, and start smooth simulation.

1. Switch role to **Official / Tanod** via the Profile tab.
2. Tap the incoming alert notification or tap the red incident pin on the map.
3. Tap **Accept Incident Response**.
4. **Expected Outcome**:
   - Status badge changes to orange `ACCEPTED`.
   - Submitting resident receives notification: `"Your report was accepted. Tanod Berto Aquino is responding."`.
5. Tap **Simulate** in the bottom panel.
6. **Expected Outcome**:
   - Status changes to green `ON_THE_WAY`.
   - Blue/gold patrol vehicle marker appears on map facing driving direction.
   - Resident receives notification: `"Responder is on the way. ETA: 4 mins."`.
7. Tap the speed button next to Simulate to cycle through **1x → 2x → 4x**.
8. **Expected Outcome**: Vehicle movement accelerates smoothly while following the road path without teleporting or jumping.

---

### CUJ 3: Dynamic Route Trimming & Auto-Arrival

**Objective**: Verify smooth vehicle rotation, dynamic polyline trimming, driver camera follow, and auto-arrival.

1. While simulation is running, observe the green route polyline ahead of the patrol vehicle.
2. **Expected Outcome**:
   - The green polyline behind the vehicle is dynamically trimmed as the vehicle advances.
   - The patrol marker smoothly rotates to match street turns without sudden snapping.
   - The map camera pans smoothly (`panTo`) keeping the vehicle centered in the lower third of the screen.
3. Allow the simulation vehicle to reach the incident destination (or move within 30 meters).
4. **Expected Outcome**:
   - Status badge automatically changes to blue `ARRIVED`.
   - An arrival audit event is appended to the report timeline.
   - Submitting resident receives notification: `"Responder has arrived at your location."`.

---

### CUJ 4: Incident Resolution & Records Archiving

**Objective**: Verify that marking an incident resolved removes it from the active map and archives it in records.

1. On the bottom sheet panel for the arrived incident, tap **Mark Resolved**.
2. **Expected Outcome**:
   - Incident marker is immediately removed from the active map.
   - Submitting resident receives notification: `"Your report has been marked resolved."`.
3. Open the **Records** tab.
4. **Expected Outcome**:
   - The resolved report appears in the historical records archive.
   - Tapping the record displays its full resolution timestamp, audit history, and responder details.
