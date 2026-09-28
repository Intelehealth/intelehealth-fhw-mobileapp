# Point-of-Care (POC) Device Screens

This document describes the screens involved in the two point-of-care hardware
integrations available during the **Diagnostics** step of a visit:

1. [Digital Stethoscope](#1-digital-stethoscope-flow) (AyuSynk / CardioSynk SDK) — captures heart & lung sound recordings.
2. [HbA1c Reader](#2-hba1c-flow) (BiosenseLib SDK) — captures a diabetes HbA1c blood-test reading.

Both are reached from the visit's diagnostics/physical-exam steps inside
`VisitCreationActivity` and write their results back into the visit's local
SQLite database so they sync to the server like any other observation.

---

## 1. Digital Stethoscope Flow

**SDK:** `com.ayudevices.cardiosynksdk` (`ayudevicesdk` dependency, see
[app/build.gradle](../app/build.gradle) and the `com.ayudevice.ayusynksdk.clientId`
meta-data in `AndroidManifest.xml`).

### Entry point — recommendation card in the question list

When a Physical Examination question's protocol requires a heart or lung sound
exam, the question list shows an inline **"Clinically Recommended"** card
(green banner, reason/symptom text, "X Upcoming" count, and a "Connect
Device" action) instead of the usual answer options.

- **File:** [NestedQuestionsListingAdapter.java](../app/src/main/java/org/intelehealth/app/ayu/visit/common/adapter/NestedQuestionsListingAdapter.java) (`ll_digital_auscultation_container` view holder)
- **Layout:** `quesionnode_list_item.xml` / `ui2_nested_question_item_view.xml`
- Tapping the card calls `onAyuDeviceRequest(node)` in [PhysicalExaminationFragment.java](../app/src/main/java/org/intelehealth/app/ayu/visit/physicalexam/PhysicalExaminationFragment.java), which opens the recording screen below.

### Screen 1 — `SoundFragment` (recording screen)

- **File:** [SoundFragment.java](../app/src/main/java/org/intelehealth/app/ayu/visit/pocdevice/SoundFragment.java)
- **Layout:** `fragment_aortic.xml` (reused for every auscultation position — the filename is a historical artifact, not tied to the Aortic point specifically)

The main capture screen, hosted in `fl_steps_body`. It shows:
- A large **body-diagram image** (`imgdirection`) that changes per auscultation point (`getImageForPosition()`).
- A **live waveform** (`AyuVisualizerView`) fed by the SDK once recording starts.
- A **"Position: <name>"** label and a **15-second countdown timer**.
- Three button states that swap in place: **Start Recording**, **Stop Recording**, and **Retry / Save Recording**.

On first entry the screen immediately opens the connect dialog (below). Once
connected, it shows the diagram for the first required position; **Start**
applies the heart/lung audio filter and begins BLE audio streaming (with
retry); recording stops automatically at 15s or on manual **Stop**. **Save**
writes the PCM audio to `getExternalFilesDir()/records/<position>_<timestamp>.pcm`,
inserts a row into the local recordings table, and advances to the next
required position — reopening the connect dialog to show updated progress —
until every position is recorded, then returns to `PhysicalExaminationFragment`.

### Screen 2 — `AyuConnectDialogFragment` (connect / position picker)

- **File:** [AyuConnectDialogFragment.java](../app/src/main/java/org/intelehealth/app/ayu/visit/pocdevice/AyuConnectDialogFragment.java)
- **Layout:** `dialog_ayu_connect.xml`

A modal shown on top of `SoundFragment`. Shows:
- A **status dot + "Connected"/"Disconnected"** text and a **Scan** button (BLE scan via `AyuDevice.getBleInstance()`).
- Two selectable cards, **Heart** and **Lung**, each with a progress bar and "X / N recorded" count (dimmed when not the active type, turns green once complete).

Tapping a card requires the device to already be connected; it hands the
selected type back to `SoundFragment` and dismisses. A **Continue** button
only appears once every required heart *and* lung position has been recorded
(checked live against the local recordings table).

### Auscultation points captured

- **Heart:** Aortic, Pulmonic, Tricuspid, Mitral
- **Lung:** Anterior 1–6, Lateral 1–4, Posterior 1–6 (left/right, top/middle/lower positions)

### Data storage

Recordings are **not** stored as OpenMRS observations — there is no
stethoscope concept UUID in `UuidDictionary.java`. Each position is written
only to the local table `tbl_follow_up_heart_lung_recoding`
(`patient_uuid, visit_uuid, encounter_uuid, type, position, recordingStatus,
audio_path, result`), plus the raw `.pcm` audio file on device storage.

### Legacy screens (present in code, not reachable from the live flow)

Worth knowing about if you're navigating the codebase, and candidates for
cleanup: `ConnectPocDeviceFragment` + `PocDeviceListFragment` (an older
wizard-style connect screen, `fragment_connect_pocdevice.xml`, dead case in
`VisitCreationActivity`), `SoundDialogFragment` (earlier dialog-based version
of `SoundFragment`), `DigitalStethoscopeDialogFragment` (single-position
full-screen dialog, only referenced from commented-out code), and
`RecordHeartSoundsFragment` / `RecordLungSoundsFragment`.

---

## 2. HbA1c Flow

**SDK:** `biosenselib` (BLE reader).

### Entry point — `DiagnosticsCollectionFragment`

- **File:** [DiagnosticsCollectionFragment.java](../app/src/main/java/org/intelehealth/app/ayu/visit/diagnostics/DiagnosticsCollectionFragment.java)
- **Layout:** `fragment_diagnostics_collection.xml`

The main Diagnostics step form — a scrollable list of blood-test fields
(Random/Fasting/Post-Prandial Glucose, Hemoglobin, Uric Acid, Cholesterol,
Diabetes HbA1c), each shown or hidden per protocol config
(`PatientDiagnosticsConfigKeys`). Unlike the stethoscope, there is **no
separate result screen** — the HbA1c card lives entirely on this form:

- A numeric entry field (`etv_diabetes_hba1c`).
- A **"Scan HbA1c Device"** button (`btnScanDevice`).
- A status dot + text: *Disconnected* (red) → *Connecting* (orange) → *Ready — you may start the test now* / *Connected* (green/black), with a pulsing "live" badge.
- A **"Last updated <time>"** label.
- (Debug builds only) an in-app scrollable log with Copy/Email buttons for support diagnostics.

### Screen 1 — `BleScanActivity` (device picker)

- **File:** [BleScanActivity.java](../app/src/main/java/org/intelehealth/app/ayu/visit/hba1c/BleScanActivity.java)
- **Layout:** `activity_ble_scan.xml`

Launched via `startActivityForResult` when **"Scan HbA1c Device"** is tapped.
Shows a **Scan** button and a list of discovered BLE devices (name + MAC
address); tapping one returns its address to the caller.

### Live connection — no dedicated screen

Back in `DiagnosticsCollectionFragment.onActivityResult()`, the chosen
address is saved to `SharedPreferences` and handed to
`VisitCreationActivity.saveAndStartBleDevice()`, which starts a
`ControlCentre` (from `biosenselib`) managing the BLE GATT session —
connect → "ready to receive" handshake → streamed reading. This is surfaced
through **`HbA1cLiveViewModel`**
([HbA1cLiveViewModel.java](../app/src/main/java/org/intelehealth/app/ayu/visit/hba1c/HbA1cLiveViewModel.java)),
an Activity-scoped `AndroidViewModel` shared between the Activity and the
fragment so the BLE connection survives fragment navigation. It exposes
`hba1cReading`, `connected`, `readyToReceive`, and `lastUpdatedAt` as
LiveData; `DiagnosticsCollectionFragment` observes these to update the status
text and auto-fills/auto-saves the reading into the form field (with a green
flash animation) the moment a value arrives.

### Submit

Tapping **Submit** (`btn_submit`) validates all diagnostics fields
(range-checked in `AppConstants`), resolves the final HbA1c value with a
3-tier fallback (live ViewModel reading → typed field text → previously
saved value), and advances to the Diagnostics Summary step
(`DiagnosticsCollectionSummaryFragment`).

### Data storage

The HbA1c reading is stored as a standard OpenMRS-style observation — an
`ObsDTO` with `conceptuuid = UuidDictionary.DIABETES_HBA1C`
(`f0631271-e0b3-48ca-a4e5-70959a7b76d9`), `encounteruuid = encounterVitals`,
`conceptsetuuid = UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET`, saved via
`ObsDAO.insertObs()` / `updateObs()` into `tbl_obs`, with the parent
encounter marked unsynced so it's picked up on the next sync. The other
diagnostics fields (glucose, hemoglobin, uric acid, cholesterol) follow the
same `tbl_obs` pattern with their own concept UUIDs.

### Legacy screens (present in code, not reachable from the live flow)

`Hba1cActivity`, `MeasurementActivity`, `ResultActivity`, `BleSearchActivity`
(with `BleManager`, `BleConnectedThread`, `DeviceAdapter`) in
`app/src/main/java/org/intelehealth/app/ayu/visit/hba1c/` — declared in
`AndroidManifest.xml`, likely an earlier standalone scan → measure → result
wizard, superseded by the inline `BleScanActivity` + `HbA1cLiveViewModel`
flow described above.

---

## Summary: screen flow at a glance

```
Physical Exam question list
  └─ "Clinically Recommended" card ─▶ SoundFragment ⇄ AyuConnectDialogFragment
                                          (record heart/lung sounds per position)
                                          └─▶ back to Physical Exam question list

Diagnostics Collection form
  └─ "Scan HbA1c Device" ─▶ BleScanActivity (pick device)
                              └─▶ back to Diagnostics form, live reading via HbA1cLiveViewModel
                                    └─▶ Submit ─▶ Diagnostics Summary
```
