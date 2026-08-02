# `:abdm` — Module Reference & Porting Guide

Everything needed to drop this module into another project/branch and have it behave identically.
Current host: `:app` = `org.intelehealth.ezazi` (eZazi), branch `abdm_ezazi_master`.

---

## 1. What the module is

A self-contained Android library (`namespace org.intelehealth.abdm`) implementing the ABDM/ABHA
flows: **Create ABHA** (Aadhaar enrolment), **Verify existing ABHA** (Aadhaar / Mobile / ABHA
number / ABHA address), **ABHA address suggestions**, **compare-and-merge with an existing local
patient**, and **ABHA card download/view**.

Architecture: clean-ish 3 layers — `data` (Retrofit + DTO + mapper + repository impl), `domain`
(repository interfaces + models), `presentation` (Activities + `@HiltViewModel` + `StateFlow` UI
state + `Channel` one-shot events). DI is Hilt, all in `SingletonComponent`.

**Verified: the module has zero imports from outside `org.intelehealth.abdm`.** Everything it needs
from the host arrives through three interfaces and one Application-level Hilt entry point.

### Visibility discipline

Almost everything is `internal`. The **public surface** — the entire contract with the host — is
exactly:

| Type | Purpose |
|---|---|
| `config.AbdmConfig` | host → module: base URL + env suffix |
| `config.AbdmSessionProvider` | host → module: credentials + location UUID |
| `config.AbdmPatientLocalStore` | host → module: local patient DB bridge |
| `config.LocalPatientRecord` | shared Parcelable patient shape |
| `result.AbdmResult` / `AbdmAbhaProfile` / `AbdmOutcomes` | module → host: the outcome |
| `presentation.AbdmLauncher` | host entry point (starts Verify/Create) |
| `presentation.AbdmCardDownloader` | host entry point (download/view card) |
| `presentation.abha_choice.AbhaChoiceDialogFragment` | host entry point (the 3-way choice dialog) |
| `presentation.consent.ConsentDialog` | public, but only used internally |
| `data.remote.auth.TokenStore`, `data.remote.interceptor.AuthInterceptor` | public only because Hilt needs them; not host-facing |
| all `data.remote.dto.*` | public only for Gson; not host-facing |

---

## 2. Build wiring (must be replicated exactly)

### `settings.gradle`
```gradle
include ':features:ondemand:abdm'   // elcg-fhw used a top-level `include ':abdm'`
```

### Root `build.gradle`
Versions below are what Android-Mobile-Client resolved to. **Check what your build actually resolves
before pinning** — see §9 gotcha 1.
```gradle
buildscript { dependencies {
    classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24'
    classpath 'com.google.dagger:hilt-android-gradle-plugin:2.51.1'
} }
plugins {
    id 'com.google.dagger.hilt.android' version '2.51.1'       apply false
    id 'com.google.devtools.ksp'        version '1.9.24-1.0.20' apply false
    id 'org.jetbrains.kotlin.android'   version '1.9.24'        apply false
}
```
`maven { url 'https://jitpack.io' }` is required in **both** `buildscript.repositories` and
`allprojects.repositories` (for `timberkt` and `android-otpview-pinview`).

> **Migration note:** elcg-fhw went Hilt 2.47 → 2.51.1, Kotlin 1.9.10 → 1.9.22.
> Android-Mobile-Client went Hilt 2.47/2.49 → 2.51.1, Kotlin 1.8.0/1.8.10 → **1.9.24**.
> Where the Kotlin plugin is declared in both the buildscript classpath **and** the plugins DSL, both
> must move together — they disagreed in both projects before the bump.

> **Fix duplicate annotation processors before bumping.** Registering the same processor via both
> `annotationProcessor` and `kapt` in one module runs it twice; a version bump can turn that from
> tolerated into fatal. Android-Mobile-Client had this for Hilt in `features/ondemand/klivekit` and
> for Room in `database`. Keep `kapt`, drop `annotationProcessor`.

### Host `build.gradle`
```gradle
plugins {
    id 'com.google.dagger.hilt.android'
    // ksp only if the host module itself uses it; abdm applies it independently
}
dependencies {
    implementation project(':features:ondemand:abdm')
    implementation 'com.google.dagger:hilt-android:2.51.1'
    kapt 'com.google.dagger:hilt-compiler:2.51.1'   // or ksp — see below
}
```

> **Mixed kapt/KSP for Hilt across modules works.** Android-Mobile-Client runs Hilt on **kapt** in
> `:app` while `:abdm` uses **KSP**, and `:app`'s aggregating processor picks up abdm's KSP-generated
> `@AggregatedDeps` without complaint (verified by a successful `assembleNasDevDebug`). Hilt versions
> must match across the boundary. The unsupported case is both processors for the same annotation
> **within one module**. elcg-fhw instead runs Hilt on KSP in both.

> **Removed and must stay removed** — conflicts with Hilt 2.51.1 / Kotlin 1.9.x:
> ```gradle
> configurations.configureEach {
>     resolutionStrategy { force 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10' }
> }
> ```

### `ABHA_ADDRESS_SUFFIX` — one per flavor of the server dimension
```gradle
dev        { buildConfigField "String", "ABHA_ADDRESS_SUFFIX", '"@sbx"'  }
staging    { buildConfigField "String", "ABHA_ADDRESS_SUFFIX", '"@sbx"'  }   // AMC only
production { buildConfigField "String", "ABHA_ADDRESS_SUFFIX", '"@abdm"' }
```
Put it on whichever dimension carries `SERVER_URL` (`app/environment.gradle` in elcg-fhw,
`app/whitelabel.gradle` in Android-Mobile-Client, where it belongs on the **server** dimension —
the 9 client flavors do not need it, since each project ships from its own branch).

### `gradle.properties`
`android.nonTransitiveRClass=false` and `android.nonFinalResIds=false` — see §9 gotcha 7.

### Module `build.gradle`
`com.android.library`, `kotlin.android`, `ksp`, `hilt.android`, `kotlin-parcelize`;
compileSdk 36, minSdk 26, targetSdk 36, Java/JVM 17; `viewBinding = true`, `buildConfig = true`;
`consumerProguardFiles "consumer-rules.pro"` — and **that file must exist** with real keep rules if
the host minifies release builds.

Dependencies: core-ktx 1.12.0, appcompat 1.6.1, fragment-ktx 1.6.2, activity-ktx 1.8.1,
material 1.11.0, constraintlayout 2.1.4, recyclerview 1.3.2, lifecycle-* 2.6.2,
coroutines-android 1.7.1, retrofit 2.9.0 + converter-gson 2.9.0, okhttp logging-interceptor 4.10.0,
hilt-android 2.51.1 (+ ksp compiler), `com.github.ajalt:timberkt:1.5.1`,
`com.github.mukeshsolanki:android-otpview-pinview:2.1.2`.

These are version **floors**, not pins — Gradle resolves to the host's higher versions
(Android-Mobile-Client lifts appcompat to 1.7.0, material to 1.12.0, lifecycle to 2.8.6, activity to
1.9.1). androidx is binary-backward-compatible, so leave them alone rather than chasing the host.

---

## 3. Hilt graph

### Provided **by the module** (all `@InstallIn(SingletonComponent::class)`)

| Module | Provides |
|---|---|
| `CommonNetworkModule` | `HttpLoggingInterceptor` (BODY in debug, NONE in release; logs via timberkt) |
| `AuthNetworkModule` | `@AuthClient OkHttpClient`, `@AuthClient Retrofit`, `AbdmAuthApi`, `PatientApi` |
| `MainNetworkModule` | `@MainClient OkHttpClient`, `@MainClient Retrofit`, `AbhaCreateApi`, `AbhaProfileApi`, `AbhaSuggestionsApi`, `AbhaVerifyApi` |
| `RepositoryModule` (`@Binds`) | the 6 repositories → their `Impl` |

Constructor-injected `@Singleton`s: `TokenStore`, `AuthInterceptor`, `TokenManager`.

### Required **from the host** — the module will not compile without these

```
AbdmConfig            (baseUrl, abhaAddressSuffix)
AbdmSessionProvider   (getEncodedCredentials, getLocationUuid)
AbdmPatientLocalStore (5 suspend functions)
```
Plus `@HiltAndroidApp` on the host `Application`. Missing any of these ⇒ `MissingBinding` at KSP time.

### Hilt-annotated types inside the module
- `@HiltViewModel`: `AbhaCreateViewModel`, `AbhaVerifyViewModel`, `AbhaSuggestionsViewModel`, `AbhaCompareViewModel`
- `@AndroidEntryPoint`: `AbhaCreateActivity`, `AbhaVerifyActivity`, `AbhaCompareActivity`, `AbhaSuggestionsActivity`, `MobileOtpVerificationDialogFragment`
- `@EntryPoint`: `AbdmCardDownloaderEntryPoint` → `AbhaProfileRepository`, resolved via
  `EntryPointAccessors.fromApplication(...)` inside the `AbdmCardDownloader` object

> `MobileOtpVerificationDialogFragment` uses `by activityViewModels<AbhaCreateViewModel>()`, so it
> is only valid when hosted by `AbhaCreateActivity`.

### The two-client split (important)

- **`@AuthClient`** — no `AuthInterceptor`. Serves `abha/getToken` (chicken-and-egg: can't Bearer
  your way to a token) and `PatientApi` (which supplies its own `Basic` header).
- **`@MainClient`** — has `AuthInterceptor`, which adds `Authorization: Bearer <token>` from
  `TokenStore` **unless the request already carries an `Authorization` header**. If `TokenStore` is
  empty it **throws `IllegalStateException`**.

Therefore every `@MainClient` repository method calls `tokenManager.ensureValidToken()` first.
`TokenManager` is `Mutex`-guarded with a double-check so concurrent callers refresh once.
`TokenStore` is **in-memory only** (`@Volatile`, 60 s expiry safety buffer) — nothing persists
across process death, and nothing needs to.

---

## 4. Network contract

All paths are relative to `AbdmConfig.baseUrl` (**must end with `/`**).

### `@AuthClient`
| Verb | Path | Notes |
|---|---|---|
| GET | `abha/getToken` | → `TokenDto` (`accessToken`, `expiresIn`) |
| GET | `{baseUrl}EMR-Middleware/webapi/check/id/{abhaNumber}` | `@Url` absolute; `Authorization: Basic <encoded>`; → `UserStatusResponseDto` (`data.uuid`, `data.openmrsid`) |
| POST | `openmrs/ws/rest/v1/patient/{patientUuid}/identifier` | `Authorization: Basic`; body `{identifier, identifierType, location}` |

### `@MainClient` (Bearer injected automatically)
| Verb | Path | Used by |
|---|---|---|
| POST | `abha/enrollOTPReq` | Create: request Aadhaar OTP (`scope=aadhar`) **and** mobile OTP (`scope=mobile` + `txnId`) |
| POST | `abha/enrollByAadhar` | Create: verify Aadhaar OTP → `AbhaCreateSession` |
| POST | `abha/enrollByAbdm` | Create: verify the new mobile's OTP |
| POST | `abha/enrollSuggestion` | Suggestions: fetch address list |
| POST | `abha/setPreferredAddress` | Suggestions: register chosen address |
| POST | `abha/profile` | + `X-TOKEN` header → full ABHA profile |
| GET | `abha/getCard?scope=` | + `X-TOKEN` header → base64 card image |
| POST | `abha/searchAbhaProfiles` | Verify/Mobile → `Map<String, SearchProfileResponseDto>`, **only key `"0"` is read** |
| POST | `abha/loginOTPReq` | Verify: request OTP |
| POST | `abha/loginOTPVerify` | Verify: verify OTP → `AbhaVerifySession` |
| POST | `abha/fetchAuthModes` | + `X-TOKEN` header; **called with `xToken = ""`** (pre-login) |

### Error model (`data/remote/extensions/ResponseExtensions.kt`)
`requireBody()` / `requireSuccess()` throw:
- `HttpException(httpCode, errorMessage, serverMessage)` — `serverMessage` is a best-effort parse of
  `{"message": …}` from the error body
- `EmptyResponseException`, `MalformedResponseException`
- `OtpVerificationFailedException` — HTTP 200 but `authResult == "failed"`

Repositories wrap everything in `runCatching` and return `Result<T>`; ViewModels map codes to string
resources (400 → invalid Aadhaar/mobile, 422 → invalid OTP, 401/403 → auth failed, 404 → "no ABHA
records" / prompt-to-create, 409 → address exists, 429 → rate-limited + kills the resend counter,
5xx → server unavailable).

---

## 5. Constants that must match the server

| Constant | Value | Where |
|---|---|---|
| ABHA **address** identifier type | `59077d8f-8bee-4a6f-a1a8-64365a297da6` | `AbhaCreateViewModel`, `AbhaVerifyViewModel`, host `PatientsFrameJson` |
| ABHA **number** identifier type | `6ad4e308-33aa-4afc-9879-6033d1984876` | host `PatientsFrameJson` only |
| Scopes | `aadhar`, `mobile`, `index`, `abha-address`, `abha-number` | ViewModels / repos |
| Auth methods | `AADHAAR_OTP`, `MOBILE_OTP` | `AbhaVerifyViewModel` |
| Card scopes | `AbdmResult.CARD_SCOPE_CREATE = "aadhar"`, `CARD_SCOPE_VERIFY = "mobile"` | result mappers |
| Sentinels | `"NA"` (absent uuid/openMrsId), `authResult == "failed"`, `kycStatus == "PENDING"` | ViewModels |
| Address suffix | `@sbx` (dev) / `@abdm` (prod) | `AbdmConfig.abhaAddressSuffix` |

Validation rules baked in: Aadhaar = 12 digits + **Verhoeff** checksum (`util/VerhoeffAlgorithm`);
mobile = 10 digits; OTP = 6 digits; ABHA number = 14 digits, reformatted to `NN-NNNN-NNNN-NNNN`;
ABHA address = 8–18 chars, `[A-Za-z0-9._]`, max one `.` and one `_`, not leading/trailing.
Resend: 60 s countdown, `MAX_RESEND_ATTEMPTS = 2`.

---

## 6. The host ⇄ module data contract

### In — via Hilt
The three `config` interfaces (§3).

### In — via Intent extras
| Key | Const | Set by |
|---|---|---|
| `"patientName"` | `AbhaCreateActivity.EXTRA_PATIENT_NAME` | `AbdmLauncher` |
| `"patientName"` | `AbhaVerifyActivity.EXTRA_PATIENT_NAME` | `AbdmLauncher` |

### Out — via Activity result
`RESULT_OK` + `intent.getParcelableExtra(AbdmResult.EXTRA_ABDM_RESULT /* "abdm_result" */)`

```kotlin
AbdmResult(
    outcome: AbdmOutcomes,
    accessToken: String?,   // always null today
    xToken: String?,        // "Bearer …" — feeds getCard
    txnId: String?,
    isNew: Boolean,
    profile: AbdmAbhaProfile?,
    uuid: String?,          // existing local/HMIS patient
    openMrsId: String?,
    cardScope: String?,     // "aadhar" (create) | "mobile" (verify)
)
```

`AbdmAbhaProfile(abhaNumber, firstName, middleName, lastName, dateOfBirth, gender, mobile, address,
pinCode, profilePhoto, phrAddresses, preferredAbhaAddress)`

`AbdmOutcomes` (names kept verbatim from the legacy module):
1. `NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION`
2. `NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION`
3. `NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION`
4. `NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_NEW_PATIENT_FOR_VERIFICATION`
5. `NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON`

> **`dateOfBirth` is not one format.** Create returns the server's `dob` (`dd-MM-yyyy`); Verify
> composes `"$year-$month-$day"` (`yyyy-M-d`). The host parses both leniently.

### Internal activity-to-activity contracts (not host-facing)
- `AbhaSuggestionsActivity` — in: `extra_txn_id`, `extra_default_address`; out: `extra_chosen_address`
  (`EXTRA_CHOSEN_ADDRESS`, the only public one)
- `AbhaCompareActivity` — in: `extra_local_record`, `extra_abha_record` (both `LocalPatientRecord`),
  `extra_x_token`, `extra_txn_id`; out: `AbdmResult`

### Fragment Result API channels
| Fragment | Request key | Result key |
|---|---|---|
| `AbhaChoiceDialogFragment` (public) | `abdm_abha_choice_request` | `abdm_abha_choice_result` → `Choice{VERIFY_ABHA, CREATE_ABHA, CONTINUE_WITHOUT_ABHA}` |
| `PatientNameDialogFragment` (internal) | `abdm_patient_name_request` | `name` (null ⇒ cancelled) |
| `AbhaAddressChecklistDialogFragment`, `AbhaOtpTypeDialogFragment`, `AccountSelectDialogFragment` | internal | — |

---

## 7. What the host must implement

### 7.1 `AbdmConfig`
```kotlin
baseUrl = BuildConfig.SERVER_URL.ensureTrailingSlash()   // String.ensureTrailingSlash() in StringExtensions.kt
abhaAddressSuffix = BuildConfig.ABHA_ADDRESS_SUFFIX
```

### 7.2 `AbdmSessionProvider`
Must read **fresh** on every call (login/logout/token refresh change it):
`getEncodedCredentials()` → `SessionManager.encoded` (Basic credentials — `null` makes
`PatientApi` calls throw `IllegalStateException`, so the user must be logged in);
`getLocationUuid()` → `SessionManager.getLocationUuid()`.

### 7.3 `AbdmPatientLocalStore` — five suspend functions, all off the main thread, none may throw

| Method | eZazi implementation (`PatientsDAO`) |
|---|---|
| `isPatientLinkedWithAbhaAddress(openMrsId, abhaAddress)` | `SELECT COUNT(*) … openmrs_id = ? AND abha_address LIKE %?% COLLATE NOCASE` |
| `linkAbha(patientUuid, abhaNumber, abhaAddress)` | `updatePatientAbha` — writes both columns, marks the row **unsynced** |
| `findPatientForComparison(abhaNumber, phoneNumber)` | by `abha_number LIKE %?%`, else by phone; `ORDER BY modified_date DESC LIMIT 1`; `PatientDTO → LocalPatientRecord` |
| `isPatientRegisteredLocally(abhaNumberLastFour, firstName, lastName)` | `abha_number LIKE %last4% AND first_name = ? AND last_name = ?` |
| `savePatientAfterComparison(record)` | `bifurcateAddress()` then `updatePatientAfterAbhaComparison(...)`, marks unsynced |

`bifurcateAddress()` splits `"addr…, city, district, state"` back into columns. **eZazi has no
district column, so district lands in `address2`** — re-check this against the target project's
schema.

### 7.4 Local database
`tbl_patient` gains `abha_number TEXT`, `abha_address TEXT`.
`AppConstants.DATABASE_VERSION` 4 → **5**, with the fall-through migration:
```java
case 4:
    db.execSQL("ALTER TABLE tbl_patient ADD COLUMN abha_number TEXT");
    db.execSQL("ALTER TABLE tbl_patient ADD COLUMN abha_address TEXT");
case 5:
    break;
```
`PatientDTO` gains `abhaNumber` / `abhaAddress` (`@SerializedName("abha_number"/"abha_address")`),
wired into the DAO insert/update/read paths.

### 7.5 Server sync
`PatientsFrameJson` pushes both as OpenMRS identifiers using the two type UUIDs (§5) plus
`session.getLocationUuid()`, skipping empty and `"NA"`.
`Identifier.java` gained an `identifier` field + accessors.

### 7.6 FileProvider (required for the ABHA card)
```xml
<provider android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false" android:grantUriPermissions="true">
    <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />
</provider>
```
```xml
<!-- res/xml/file_paths.xml -->
<external-files-path name="abha_cards" path="Pictures/Intelehealth_AbhaCard/" />
```
The authority string `"${context.packageName}.fileprovider"` and the path
`getExternalFilesDir(DIRECTORY_PICTURES)/Intelehealth_AbhaCard/{abhaNumber}.png` are **hardcoded**
in `AbdmCardDownloader`. Both must match or `viewCard` throws (it's `runCatching`-swallowed, so the
failure is silent).

### 7.7 UI integration points (eZazi)
| File | Role |
|---|---|
| `PrivacyNoticeActivity` | Shows `AbhaChoiceDialogFragment`; calls `AbdmLauncher.startVerifyAbha/startCreateAbha` with one shared `ActivityResultLauncher`; routes the result |
| `PatientPersonalInfoFragment` | Prefills + **locks** first/middle/last name (bypassing the upper-case filter), mobile, DOB |
| `PatientAddressInfoFragment` | Bifurcates the ABHA address, fuzzy-matches state/district, locks the fields |
| `PatientOtherInfoFragment` | Read-only ABHA number/address block; on save writes them to `PatientDTO` and fires `AbdmCardDownloader.downloadInBackground` |
| `PatientDetailActivity` | "View ABHA Card" button → `AbdmCardDownloader.viewCard` when `abha_number` is present and ≠ `"NA"` |

> **Java hosts must call `intent.setExtrasClassLoader(AbdmResult.class.getClassLoader())` before
> `IntentCompat.getParcelableExtra(...)`** — otherwise the `:abdm` Parcelable fails to unparcel on
> pre-API-33. This is done in all three fragments; keep it.

Host result routing:
- outcome 5 → `PatientDetailActivity` (`patientUuid`, `patientName`, `hasPrescription=false`, `tag=newPatient`, `privacy`)
- all others → `AddNewPatientActivity` with the parcelled `AbdmResult` forwarded under `EXTRA_ABDM_RESULT`

### 7.8 UI integration points (Android-Mobile-Client) — a better pattern

The equivalent files differ in language and architecture (elcg-fhw's registration is Java activity +
fragments; this host is Kotlin fragments over a nav-graph with a shared `patientViewModel`), so this
is a reimplementation, not a transplant. The nav-graph made two improvements possible that are worth
carrying to any future host:

| File | Role |
|---|---|
| `PersonalConsentActivity.kt` | Shows `AbhaChoiceDialogFragment` (listener registered in `onCreate`, so the choice survives rotation); routes the result |
| `PatientRegistrationActivity.kt` | `startPatientRegistrationFromAbha()` + `seedFromAbhaIfPresent()` |
| `BasePatientFragment.kt` | `hasAbha()` — the single locking predicate, shared by all three stages |
| `PatientPersonalInfoFragment.kt` | Locks name/phone/DOB/age **last** in the config observer |
| `PatientAddressInfoFragment.kt` | Locks address fields, per-field, after the dropdowns resolve |
| `PatientOtherInfoFragment.kt` | Fires `AbdmCardDownloader.downloadInBackground` on successful save |
| `PatientDetailActivity2.java` | "View ABHA Card" button |

**1. Seed the patient record at entry; fragments only disable.** `seedFromAbhaIfPresent()` copies the
profile onto the fresh `PatientDTO` before `patientViewModel.updatedPatient(it)`, alongside the
existing family-member pre-fill that does the same from a parent patient. Every stage then sees ABHA
data through the shared view model, so no fragment reads `AbdmResult` for display. Two payoffs:
- the address stage needs **no custom fuzzy matching** — `setupStates()`/`setupDistricts()` already
  resolve their dropdowns from `patient.stateprovince`/`patient.district` via `LanguageUtils`;
- the read-only ABHA fields need no fragment code at all — bind visibility to the record:
  `android:visibility="@{patient.abhaNumber != null &amp;&amp; !patient.abhaNumber.empty ? View.VISIBLE : View.GONE}"`.

Not seeded: **gender** (deferred pending PM) and, in this host, whatever the config forces — see the
caveat below.

**2. Lock on record state, not on an intent extra.** `hasAbha()` is `patient.abhaNumber` non-empty, so
locking holds on **every** entry path including edit mode. elcg-fhw keys off `AbdmResult` in the intent
and explicitly skips the edit path (`if (fromSummary) return;`), which means a verified patient's
identity fields stay editable afterwards. The state-based predicate is the intended behaviour.
It only works if the edit-mode read path maps the columns — §9 gotcha 4.

**Two things to get right when implementing the lock:**
- **Order.** Apply it *last*, after the stage's setup has attached its pickers, filters and adapters —
  otherwise they leave the fields interactive. In both fragments here that means the end of the config
  observer / rebind callback.
- **Lock per field, only when it has a value.** The ABHA address is free text, so a component may fail
  to match the state/district masters; locking an empty field would leave the user unable to supply it.

**Entry point: add a separately named function, not a parameter.** `startPatientRegistrationFromAbha`
exists rather than a fourth argument on `startPatientRegistration` because that method has 9 call
sites, 7 of them Java — and Kotlin default arguments do not exist on the JVM, so adding a parameter
breaks those 7 unless `@JvmOverloads` is added. A distinct name also avoids overload ambiguity between
`(Context, String?, PatientRegStage)` and the ABHA signature, and reflects the different semantics
(always a fresh PERSONAL-stage registration).

**Caveat — host config can override the seeded values.** In this host, `setupStates()`/`setupDistricts()`
discard the patient's state/district when config marks them non-editable and a default exists (the
code comment cites NAS explicitly). Existing behaviour, left alone, but it means the stored
state/district may not be ABHA's.

**Caveat — phone formatting.** `AbdmAbhaProfile.mobile` is a bare 10-digit number; this host's phone
field runs through an hbb20 `CountryCodePicker` and saves `fullNumberWithPlus`, so the stored value
gains a country code and will not match ABHA byte-for-byte. Every picker here sets only
`ccp_countryPreference="us,in"` with **no** `ccp_defaultNameCode`, so nothing pins India — and because
the ABHA path locks the picker, a wrong default cannot be corrected by the user. Pin
`ccp_defaultNameCode="IN"` for an India-only deployment.

---

## 8. Flow walkthroughs

### Entry
`AbhaChoiceDialogFragment` → host picks → `AbdmLauncher` shows `PatientNameDialogFragment` **once**,
then launches Verify or Create with the name attached. (A Verify → Create redirect reuses the same
name, so the user is never asked twice.) Both activities check `NetworkConnection.isOnline` on start
and hard-block when offline.

### Create (`AbhaCreateActivity` + `AbhaCreateViewModel`)
1. Consent checkbox → `ConsentDialog` (7 checkboxes, all must be ticked)
2. Aadhaar (12 digits + Verhoeff) → `enrollOTPReq(scope=aadhar)` → Aadhaar locks
3. Aadhaar OTP + mobile → `enrollByAadhar` → `AbhaCreateSession`
4. If the server's profile mobile ≠ the entered mobile → `enrollOTPReq(scope=mobile)` +
   `MobileOtpVerificationDialogFragment` → `enrollByAbdm`
5. `routeAfterVerification`:
   - `isNew` **and** the only `phrAddress` is `{abhaNumber}{suffix}` → **Suggestions screen**
   - else → `checkExistingUser(abhaNumber)`:
     - `uuid == null` → outcome **2**
     - `uuid != null` → **address checklist**:
       - pick existing → link server identifier (skipped if `isPatientLinkedWithAbhaAddress`) → `linkAbha` locally → outcome **1**
       - "create new" → Suggestions → link → `linkAbha` → outcome **3**

Suggestions: `enrollSuggestion` → user picks/types → `setPreferredAddress` → returns
`extra_chosen_address`. Choosing the default address short-circuits the register call.

### Verify (`AbhaVerifyActivity` + `AbhaVerifyViewModel`) — three tabs
| Tab | Path |
|---|---|
| **Aadhaar** | `loginOTPReq(scope=aadhar)` → `loginOTPVerify` → first account → `profile(scope=aadhar)` |
| **Mobile** | `searchAbhaProfiles` → account picker (rows show "registered with Intelehealth" via `isPatientRegisteredLocally`) → `loginOTPReq(scope=index, authMethod=MOBILE_OTP, txnId=searchTxnId)` → verify `scope=mobile` → 1 account → `profile(scope=abha-number)`; >1 → picker again |
| **ABHA number** | 14 digits → format `NN-NNNN-NNNN-NNNN` → OTP-type dialog → `loginOTPReq(scope=abha-number)` |
| **ABHA address** | must end with `abhaAddressSuffix` → `fetchAuthModes` → OTP-type dialog → `loginOTPReq(scope=abha-address)` → verify → **KYC `PENDING` aborts** → `profile(scope=abha-address)` |

Then `routeAfterProfile` → `checkExistingUser`:
- no `uuid` / `"NA"` → outcome **4**
- else → `findPatientForComparison` → link the server identifier → **`AbhaCompareActivity`**
  (field-by-field HMIS vs ABHA) → `savePatientAfterComparison` → outcome **5**

A 404 on mobile-search or Aadhaar-verify raises `PromptCreateAbha` → dialog → launches
`AbhaCreateActivity` nested, and its result is passed straight back up to the host.

### Card
`AbdmCardDownloader.downloadInBackground(context, xToken, cardScope, abhaNumber)` — fire-and-forget
on a `SupervisorJob + Dispatchers.IO` scope, no-ops if the PNG already exists, decodes base64 →
PNG. All failures swallowed; a missing card never blocks registration.
`AbdmCardDownloader.viewCard(context, abhaNumber)` — `FileProvider` + `ACTION_VIEW`, or a toast
(`abdm_card_not_available`) if absent.

---

## 9. Porting checklist & known gotchas

**Checklist**
1. `include` the module (`:features:ondemand:abdm` in Android-Mobile-Client); copy the directory verbatim,
   but **delete any `build/` directory that came along** — stale KSP caches from another project root
   cause bogus incremental failures.
2. Align root plugin versions and add jitpack. **Do not assume 1.9.22** — see gotcha 1.
3. Add the Hilt plugin and deps to the host module, plus `implementation project('<module path>')`.
   The dependency must land **with** `AbdmConfigModule`, not after it: the module's config classes
   cannot resolve without it, and the bindings cannot resolve without them.
4. Remove any `force 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:…'` resolution strategy.
5. `@HiltAndroidApp` on the `Application`.
6. Port `AbdmConfigModule` + `SessionModule` (or equivalents) — all three interfaces must be bound.
7. Add the `ABHA_ADDRESS_SUFFIX` buildConfigField to **every** flavor of the dimension that carries
   the server URL.
8. Add the `abha_number` / `abha_address` columns + DB version bump + migration, the DTO fields, the
   DAO methods, and the sync identifiers.
9. Declare the FileProvider path with the exact authority and directory.
10. Wire the host UI: choice dialog + launcher + result handling + prefill/lock + card button.
11. **Validate with `assemble<Variant>`, never a compile task** — see gotcha 3.
12. **Check the host's app theme.** If it is not a `Theme.MaterialComponents` descendant, the ABHA
    choice dialog crashes the first time it opens — see gotcha 16.

**Gotchas**

1. **Pin the Kotlin version to whatever the build actually resolves, then match KSP to it.**
   In Android-Mobile-Client, `androidx.navigation:navigation-safe-args-gradle-plugin:2.8.1`
   hard-requires `kotlin-gradle-plugin:1.9.24`, silently upgrading a declared 1.9.22. kapt tolerates
   the drift; **KSP does not** — the first KSP module to compile dies with
   `NoSuchMethodError: IncrementalCompilationContext.<init>` from `com.google.devtools.ksp`.
   Diagnose with `./gradlew buildEnvironment`, which prints `ksp-X is too old for kotlin-Y` and the
   `1.9.22 -> 1.9.24` resolution arrow. Landed here on **Kotlin 1.9.24 + KSP 1.9.24-1.0.20 + Hilt 2.51.1**.

2. **The module binds an unqualified third-party type.** `CommonNetworkModule` provides
   `okhttp3.logging.HttpLoggingInterceptor`, which collides with any host that also contributes one —
   in Android-Mobile-Client, `org.intelehealth.klivekit.di.NetworkModule` →
   `error: [Dagger/DuplicateBindings]`. Resolved by qualifying it with
   `di/qualifiers/AbdmLogging.kt`, applied to the provider and to the consuming parameter in
   `AuthNetworkModule` and `MainNetworkModule`. This is the module's **only** unqualified third-party
   binding — `OkHttpClient` and `Retrofit` are already `@AuthClient`/`@MainClient` — so it is the only
   collision of this kind. elcg-fhw never hit it because its klivekit has no such Hilt module.

3. **`compile<Variant>JavaWithJavac` does not validate the Dagger graph.** With the Hilt Gradle
   plugin's aggregating task enabled, the graph is resolved in `hiltJavaCompile<Variant>`, which a
   compile task never triggers. A broken graph compiles clean. Always validate with
   `assemble<Variant>`.

4. **For every ABHA write path, find the matching read path.** This is the failure mode that bit
   three times in one port, and it is always silent — build green, feature dead:
   - `PatientsDAO.unsyncedPatients()` must map both columns, or the sync push skips both identifiers
     via its own null guards.
   - the registration edit-mode reader (`PatientQueryBuilder.buildPatientDetailsQuery` **and**
     `PatientsDAO.retrievePatientDetails`) must select and map them, or any "does this patient have an
     ABHA" check is dead code.
   - other `PatientDTO` readers (list, search, appointment, visit-creation) deliberately do **not**
     map them; if a new screen needs ABHA from one of those, it needs the same addition.

5. **`INSERT OR REPLACE` on sync-down wipes columns absent from the statement.** In
   Android-Mobile-Client, `SyncDAO` → `insertPatients()` → `createPatientMap()` →
   `BaseDao.buildInsertQuery()` emits `INSERT OR REPLACE`, so a pull of an already-local patient nulls
   anything not listed. Both columns were added to `createPatientMap`, but that only completes the
   round-trip **if the middleware emits `abha_number`/`abha_address` on the patient object of the pull
   response** — the payload is flat (that is how `openmrs_id` arrives). Without that, Gson leaves them
   null and the pull still clears them. Do not "fix" this by changing `buildInsertQuery`; every DAO
   extending `BaseDao` depends on its semantics.

6. **The identifier-type UUIDs are per-server records, not constants** (§5). They must be created in
   OpenMRS admin on every target server. Verify with
   `curl -u '<user>' '<server>/openmrs/ws/rest/v1/patientidentifiertype/<uuid>'` (404 = absent). A
   missing type makes `updatePatientIdentifier` fail visibly, but a bad type in the sync push can fail
   the **entire** patient push.

7. **Resource-name collisions with the host.** With `nonTransitiveRClass=false`, library resources
   merge into the app's namespace and **the app's definition wins** — so the module renders with the
   host's values for any name they share. Verified byte-identical in both eZazi and
   Android-Mobile-Client for `white`, `primary_text`, `error_red`, `margin_10`, `margin_24`, `ok`,
   `submit`, `error_network`, so this is currently a non-issue and **renaming is not worth it** (~98
   call sites for zero behavioural change). Re-check if a future host defines any of them differently
   or sets `nonTransitiveRClass=true`. The dead `app_name` and `ic_launcher*` template leftovers have
   been deleted. To enforce the module's own `abdm_` prefix convention going forward, add
   `android.resourcePrefix 'abdm_'` — lint only.

8. **`AbdmConfig.baseUrl` must end with `/`** — Retrofit throws otherwise. That's what
   `ensureTrailingSlash()` is for; port it too.

9. **`AbdmSessionProvider.getEncodedCredentials()` returning null throws.** `basicAuthHelper` raises
   `IllegalStateException("No session — patient endpoint called without credentials")`, which
   surfaces as a generic error. The ABHA flow must only be reachable while logged in.

10. **`AuthInterceptor` throws when `TokenStore` is empty.** Any new `@MainClient` call must call
    `tokenManager.ensureValidToken()` first — follow the existing repository pattern.

11. **`searchAbhaProfiles` only reads map key `"0"`.** Any other key in the server's response is
    silently dropped.

12. **`fetchAuthModes` is invoked with an empty `X-TOKEN`.** Intentional — it runs before login — but
    it will look like a bug to anyone reading the API in isolation.

13. **Two DOB formats** on `AbdmAbhaProfile.dateOfBirth` (§6). Any new consumer must parse both.

14. **`MobileOtpVerificationDialogFragment` is bound to `AbhaCreateActivity`** via
    `activityViewModels()`; it cannot be reused elsewhere as-is.

15. **The FileProvider authority is hardcoded** as `"${context.packageName}.fileprovider"` in
    `AbdmCardDownloader`. If the host's manifest hardcodes a single flavor's authority instead of
    `${applicationId}`, card viewing silently fails on every other flavor (`getUriForFile` throws, and
    it is `runCatching`-swallowed). Promote it to `AbdmConfig` only if the module must ship on more
    than one flavor of the same host.

16. **The host's app theme is NOT guaranteed to be `Theme.MaterialComponents`, and the module's
    dialogs must not depend on it.** Every Material component in this module (`MaterialCardView`,
    `MaterialButton`, `TextInputLayout`, …) throws
    `IllegalArgumentException: The style on this component requires your app theme to be
    Theme.MaterialComponents (or a descendant)` from Material's `ThemeEnforcement` when inflated
    against a plain AppCompat theme. Android-Mobile-Client's `AppTheme` is
    `Theme.AppCompat.Light.DarkActionBar`; elcg-fhw's was a MaterialComponents descendant, which is
    why this only surfaced on the second port — as an immediate crash the first time the ABHA choice
    dialog opened.

    The module's **activities** are safe: the manifest assigns them `Theme.Abdm.NoActionBar`. The
    exposure is **dialogs shown from a host activity**, which inherit the host's theme:

    - `AbhaChoiceDialogFragment` and `PatientNameDialogFragment` are shown by the host / `AbdmLauncher`
      → these are the ones that actually break.
    - Everything else is shown from a module activity that already carries the module theme.

    **`getTheme()` does not fix an `onCreateDialog` dialog.** At that point the dialog does not exist,
    so `DialogFragment.onGetLayoutInflater` returns the *host activity's* inflater
    (`onGetLayoutInflater → prepareDialog → onCreateDialog → inflate`). Those must inflate from an
    explicitly themed context — `presentation/common/abdmDialogInflater()`, a `ContextThemeWrapper`
    over `Theme.Abdm.Dialog`. Passing the theme to `AlertDialog.Builder` is not enough: that themes
    the dialog *window*, not the separately inflated view.

    For `onCreateView` dialogs, `override fun getTheme(): Int = R.style.Theme_Abdm_Dialog` is
    sufficient — the dialog exists by then and its inflater is cloned into the themed context. This is
    what the three `BottomSheetDialogFragment`s already do with `Theme_Abdm_BottomSheet`.

    **Rule for any new dialog in this module:** inflate via `abdmDialogInflater()` if it builds its
    view in `onCreateDialog`, otherwise override `getTheme()`. Never rely on the host.

**Resolved in the Android-Mobile-Client port** (previously listed as gotchas): `consumer-rules.pro`
now exists with real keep rules — required because that host minifies release builds; the unused Room
dependencies have been removed; RecyclerView is declared explicitly; and `compileSdk` is 36, matching
every other module.

---

## 10. File map

```
abdm/src/main/java/org/intelehealth/abdm/
├── config/          AbdmConfig, AbdmSessionProvider, AbdmPatientLocalStore, LocalPatientRecord
├── result/          AbdmResult, AbdmAbhaProfile, AbdmOutcomes
├── di/
│   ├── network/     Common/Auth/MainNetworkModule, NetworkDefaults
│   ├── qualifiers/  @AuthClient, @MainClient
│   └── repository/  RepositoryModule
├── data/
│   ├── remote/api/         6 Retrofit interfaces
│   ├── remote/dto/         22 DTO files
│   ├── remote/auth/        TokenStore, TokenManager, BasicAuthHelper
│   ├── remote/interceptor/ AuthInterceptor
│   ├── remote/extensions/  ResponseExtensions (exception types + requireBody/requireSuccess)
│   ├── mapper/             7 DTO → domain mappers
│   └── repository/         6 repository impls
├── domain/
│   ├── repository/  6 interfaces (all internal)
│   └── model/       11 model files (all internal)
├── presentation/
│   ├── AbdmLauncher, AbdmCardDownloader          ← public entry points
│   ├── abha_choice/     AbhaChoiceDialogFragment ← public entry point
│   ├── abha_create/     Activity, ViewModel, UiState, Event, Step, InputField, ResultMapper,
│   │                    PatientNameDialog, MobileOtpVerificationDialog,
│   │                    AbhaAddressChecklistDialog + Adapter
│   ├── abha_verify/     Activity, ViewModel, UiState, Event, Step, Method, InputField,
│   │                    ResultMapper, AbhaOtpTypeDialog, AccountSelectDialog + Adapter,
│   │                    CompareActivity, CompareViewModel, CompareResultMapper,
│   │                    CompareField + Adapter
│   ├── abha_suggestions/Activity, ViewModel, UiState, Event, AbhaAddressSuggestionDialog
│   ├── consent/         ConsentDialog, CheckboxAdapter, CheckBoxRecyclerModel
│   └── common/          UiState, AbdmSnackbar
└── util/            VerhoeffAlgorithm, NetworkConnection

src/main/res/  4 activity layouts, 7 dialog layouts, 3 item layouts, 2 include layouts,
               colors/dimens/strings/themes, drawables, lato_regular.ttf
               (the ic_launcher mipmaps/drawables and the `app_name` string were template
                leftovers and have been deleted — the module declares no android:icon)
src/main/AndroidManifest.xml   4 activities, all exported=false, Theme.Abdm.NoActionBar
consumer-rules.pro             keep rules shipped to the host's R8 run (DTOs, Parcelables,
                               enums resolved by name, Retrofit interfaces)
```

### App-side files touched by the ABDM integration
```
settings.gradle                          build.gradle                app/build.gradle
app/environment.gradle                   app/src/main/AndroidManifest.xml
app/src/main/res/xml/file_paths.xml      app/src/main/res/values/strings.xml
app/src/main/res/layout/activity_patient_summary.xml
app/src/main/res/ezazi/layout{,-sw600dp}/fragment_patient_other_info.xml
java/…/di/AbdmConfigModule.kt            java/…/di/SessionModule.kt
java/…/utilities/StringExtensions.kt     java/…/utilities/PatientsFrameJson.java
java/…/app/IntelehealthApplication.java  java/…/app/AppConstants.java
java/…/database/InteleHealthDatabaseHelper.java
java/…/database/dao/PatientsDAO.java     java/…/models/dto/PatientDTO.java
java/…/models/pushRequestApiCall/Identifier.java
java/…/activities/privacyNoticeActivity/PrivacyNoticeActivity.java
java/…/activities/addNewPatient/Patient{Personal,Address,Other}InfoFragment.java
java/…/activities/patientDetailActivity/PatientDetailActivity.java
```

---

## 11. Known incomplete work

**Never exercised end to end, in any host**
- **Verify → compare → save.** Blocked on middleware in elcg-fhw and unproven in
  Android-Mobile-Client. If something is broken, expect it here first.
- **Sync round-trip.** The client persists ABHA on pull, but the middleware must emit
  `abha_number`/`abha_address` on the patient object of the pull response — §9 gotcha 5.
- **Release-build runtime.** `consumer-rules.pro` is proven to parse and resolve under R8, but the
  ABHA network layer has never been *run* minified. R8 can strip a Gson field and still ship a
  working APK.

**Parked pending a decision**
- **Gender** is not prefilled (pending PM).
- **Deactivated-account / reactivation flow.** `enrollByAadhar` can return 200 with
  `message="This account is deactivated…"`, `abhaStatus="DEACTIVATED"` and **no `tokens`** object, which
  `AbhaCreateMapper` maps to null → generic "something went wrong". Either surface the server message
  or build a real reactivation flow.
- **Address fuzzy-matching.** Approved in principle, never built. Currently an exact match against the
  master JSON, so an ABHA address component that does not match leaves its field empty (and unlocked).
- **Stale ABHA phone number.** Accepted per PRD — the ABHA number is used and locked. Revisit if
  reported.

**Cosmetic / dangling**
- `AbdmResult.accessToken` is always `null`; only `xToken` is used.
- `select_valid_date` was added to Android-Mobile-Client's strings for elcg parity but is **unused**
  there — the DOB validation path it belongs to was not ported, since the seeded DOB is locked.
- `adapterPosition` is deprecated in `AbhaAddressChecklistAdapter` and `AccountSelectAdapter`
  (`bindingAdapterPosition` replaces it). Warning only.
- elcg-fhw's `PrivacyNoticeActivity.handleAbhaResult` carries a `TODO: pre-fill AddNewPatientActivity
  from abdmResult.getProfile() based on abdmResult.getOutcome()`; all four non-compare outcomes are
  handled identically. Android-Mobile-Client does not have this TODO — seeding at entry removed the need.
- Android-Mobile-Client never populates the `privacy` intent extra on **any** path (the only reference
  is commented out in `SearchPatientAdapter_New` with a "todo: uncomment later"), so encounters record a
  null privacy notice. Pre-existing and unrelated to ABDM.
