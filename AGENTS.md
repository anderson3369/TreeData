The goal of this project is to be an activity tracker for orchard farmers
on the Android and iOS platforms using kotlin multiplatform.

The Android and iOS apps need to have a consistent theme for both applications.

TODO:   1)  Create a theme for a consistent look and feel for both applications.  The layouts,
            menubars, buttons, icons and borders should be uniform.
        2)  Implement Firebase Auth + Firestore for multi-user data sharing (see plan below).

---

## Firebase Auth + Firestore Multi-User Sync

### Goal
Allow multiple users (e.g. family members, farm workers) to share orchard data across devices
and platforms. User A logs an irrigation on Android, User B sees it on iOS.

### Architecture Overview

**Shared unit:** The Farm (identified by `siteId`) is the collaboration boundary.
Multiple authenticated users can be members of the same farm and share all data under it.

**Data flow:**
- Room remains the local source of truth on each device
- Writes go to both Room (local) and Firestore (cloud)
- Firestore listeners push other users' changes down to the local Room DB
- `persistentId` (UUID) fields on Farm, Orchard, and Tree are used as Firestore document IDs
  to avoid conflicts from auto-generated local IDs differing across devices

**Auth providers:** Google Sign-In + Apple Sign-In (both platforms)

**Existing infrastructure:**
- `dev.gitlive:firebase-auth:1.13.0` and `dev.gitlive:firebase-firestore:1.13.0` already in
  shared `build.gradle.kts` commonMain dependencies
- `google-services.json` (Android) and `GoogleService-Info.plist` (iOS) already present
- `AuthInitializer.kt` and `FireStorageInitializer.kt` exist but need rework
- `Farmer`, `Farm`, `Orchard`, `Tree` already have `@Serializable` annotations
- `Farm`, `Orchard`, `Tree` already have `persistentId: String` (UUID) fields
- `Farmer` does NOT have `persistentId` yet — needs one added

### Firestore Document Structure

```
farms/{farm.siteId}/
    metadata: { name, siteId, ownerUid, createdAt }
    members/{uid}: { role: "owner"|"member", name, joinedAt }
    farmers/{persistentId}: { name, address, city, state, zip, phone, email }
    orchards/{persistentId}: { farmSiteId, crop, plantedDate, rowWidth, ... }
    trees/{persistentId}: { orchardPersistentId, ... }
    orchardActivities/{uuid}: { orchardPersistentId, activity, notes, activityStart, activityStop }
    irrigationSystems/{autoId}: { orchardPersistentId, name, method, ... }
    irrigations/{autoId}: { irrigationSystemId, startTime, stopTime }
    fertilizerApplications/{autoId}: { orchardPersistentId, start, stop, items: [...] }
    pesticideApplications/{autoId}: { orchardPersistentId, start, stop, items: [...] }
    pumps/{autoId}: { type, flowRate, ... }
    soilMoisture/{autoId}: { orchardPersistentId, date, centibar, percent }
    fertilizers/{autoId}: { name, nitrogen, phosphorous, ... }
    pesticides/{autoId}: { productName, eparegno, ... }
```

**Key design decisions:**
- Farm `siteId` is the top-level document ID (already unique-indexed in Room)
- Entities with `persistentId` use it as the Firestore document ID
- Entities without `persistentId` (activities, irrigations, etc.) get a UUID generated at
  write time and stored both locally (new column) and as the Firestore document ID
- All Instant dates stored as Long (epoch millis) in Firestore — matches Room format
- Foreign key references in Firestore use `persistentId`/UUID, not local auto-increment IDs

---

### Phase 1: Firebase Authentication (Google + Apple Sign-In)

**Scope:** Both platforms. User can sign in but no data sync yet.

#### Step 1.1: Shared Auth Service (shared/commonMain) [DONE]
Create `shared/.../auth/AuthService.kt`:
```kotlin
// Wraps dev.gitlive firebase-auth for KMP
object AuthService {
    val auth = Firebase.auth
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn: Boolean get() = currentUser != null

    suspend fun signInWithCredential(credential: AuthCredential): AuthResult
    suspend fun signOut()
}
```

#### Step 1.2: Android — Google Sign-In [DONE]
- Add `com.google.android.gms:play-services-auth` dependency to `app/build.gradle`
- Add Google Sign-In credential helper (AndroidX Credential Manager)
- Update `LoginFragment` (or replace with Compose) to show Google + Apple sign-in buttons
- On success, get Google ID token → `GoogleAuthProvider.credential(idToken)` → `AuthService.signInWithCredential()`
- Store signed-in state; show user name/email in drawer header or settings

#### Step 1.3: Android — Apple Sign-In [DONE]
- Apple Sign-In on Android uses Firebase's built-in OAuthProvider
- `OAuthProvider.newBuilder("apple.com")` → startActivityForResult flow
- Requires Apple Developer setup: create a Service ID for Android web-redirect

#### Step 1.4: iOS — Google Sign-In
- Add GoogleSignIn-iOS SDK via SPM or CocoaPods
- Configure with `GIDClientID` from `GoogleService-Info.plist`
- Get ID token → pass to shared `AuthService.signInWithCredential()`

#### Step 1.5: iOS — Apple Sign-In
- Use `ASAuthorizationAppleIDProvider` (native AuthenticationServices framework)
- Get identity token → `OAuthProvider.credential(providerID: "apple.com", idToken:, rawNonce:)`
- Pass to shared `AuthService.signInWithCredential()`

#### Step 1.6: UI Integration
- Android: Add sign-in/sign-out to Settings or drawer menu
- iOS: Add sign-in/sign-out to `SettingsView.swift`
- Show current user info when signed in
- Gate sync features behind auth (disable backup/sync buttons when not signed in)

#### Step 1.7: Firebase Console Setup
- Enable Google and Apple sign-in providers in Firebase Console → Authentication
- For Apple Sign-In: configure Apple Developer Service ID, callback URL in Firebase
- Test on both platforms

---

### Phase 2: Firestore Write-Through (Local Changes Push to Cloud)

**Scope:** When a user saves/updates/deletes locally, also write to Firestore.
Other users' changes are NOT pulled yet (that's Phase 3).

#### Step 2.1: Add persistentId to remaining entities [DONE]
Entities that need a UUID for Firestore document identity but don't have one yet:
- `Farmer` — add `persistentId: String` field + migration
- `OrchardActivity` — add `firestoreId: String` field
- `Irrigation` — add `firestoreId: String` field
- `IrrigationSystem` — add `firestoreId: String` field
- `FertilizerApplication` — add `firestoreId: String` field
- `Fertilizer` — add `firestoreId: String` field
- `PesticideApplication` — add `firestoreId: String` field
- `Pesticide` — add `firestoreId: String` field
- `Pump` — add `firestoreId: String` field
- `SoilMoisture` — add `firestoreId: String` field
- `Variety`, `Rootstock`, `Disease`, `SoilTest` — added in Migration 6_7

DB version bump: 5 → 8 (Migration 6_7 finalized; Version 8 for schema alignment)

#### Step 2.2: Create FirestoreSync service (shared/commonMain) [DONE]
`shared/.../sync/FirestoreSync.kt`:
```kotlin
class FirestoreSync(private val db: OrchardDatabase) {
    private val firestore = Firebase.firestore

    // Get the farm's Firestore path
    private fun farmPath(siteId: String) = firestore.collection("farms").document(siteId)

    suspend fun pushFarmer(farmer: Farmer, farmSiteId: String) { ... }
    suspend fun pushOrchard(orchard: Orchard, farmSiteId: String) { ... }
    suspend fun pushOrchardActivity(activity: OrchardActivity, farmSiteId: String) { ... }
    suspend fun pushIrrigation(irrigation: Irrigation, farmSiteId: String) { ... }
    // ... etc for each entity

    suspend fun deleteFarmer(farmer: Farmer, farmSiteId: String) { ... }
    // ... etc
}
```

#### Step 2.3: Integrate sync into ViewModels [DONE]
Each ViewModel's add/update/delete methods get a sync call after the Room write:
```kotlin
fun addOrchardActivity(activity: OrchardActivity) {
    viewModelScope.launch {
        repository.createOrchardActivity(activity)
        if (AuthService.isSignedIn) {
            firestoreSync.pushOrchardActivity(activity, currentFarmSiteId)
        }
    }
}
```

#### Step 2.4: Farm registration in Firestore
When a user first signs in with existing local data:
- Create the farm document at `farms/{siteId}` with `ownerUid = currentUser.uid`
- Add the user as a member: `farms/{siteId}/members/{uid}`
- Push all existing local data to Firestore (initial sync)

#### Step 2.5: Foreign key mapping
Firestore documents reference related entities by `persistentId`/`firestoreId`, not local IDs.
Create a mapping utility:
```kotlin
// When pushing: resolve local ID → persistentId for FK fields
// When pulling: resolve persistentId → local ID
```

---

### Phase 3: Firestore Listeners (Pull Other Users' Changes)

**Scope:** Listen for changes from other users and merge into local Room DB.

#### Step 3.1: Snapshot listeners
When the app starts (and user is signed in), attach Firestore snapshot listeners
to each sub-collection under the user's farm:
```kotlin
fun startListening(farmSiteId: String) {
    farmPath(farmSiteId).collection("orchardActivities")
        .snapshots.collect { snapshot ->
            snapshot.documentChanges.forEach { change ->
                when (change.type) {
                    ADDED, MODIFIED -> upsertLocalActivity(change.document)
                    REMOVED -> deleteLocalActivity(change.document)
                }
            }
        }
}
```

#### Step 3.2: Conflict resolution strategy
- **Last-write-wins** based on a `lastModified` timestamp field on each Firestore document
- If local and remote both changed, the later timestamp wins
- Add `lastModified: Long` (epoch millis) to Firestore documents and optionally to Room entities

#### Step 3.3: ID resolution on pull
When a Firestore document arrives:
1. Look up local entity by `persistentId`/`firestoreId`
2. If found → update the local Room record
3. If not found → insert as new record, resolving FK references by looking up
   parent `persistentId` → local ID

#### Step 3.4: Offline handling
- Room is always the source of truth for the UI
- Firestore SDK has built-in offline persistence — writes queue when offline
- When connectivity returns, queued writes push automatically
- Listeners fire with cached data first, then server updates

---

### Phase 4: Farm Sharing / Invite Flow

**Scope:** Let a farm owner invite other users to collaborate.

#### Step 4.1: Invite mechanism
Options (pick one):
- **Share code:** Owner generates a short invite code (stored in Firestore). New user enters
  the code in-app → app looks up the farm by code → adds user as member.
- **Email invite:** Owner enters invitee's email → Firestore function or client writes a
  pending invite. Invitee signs in with that email → auto-joins.
- **QR code:** Encode the farm `siteId` + invite token in a QR code.

Recommended: **Share code** (simplest, no server functions needed).

#### Step 4.2: Firestore security rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /farms/{farmId} {
      // Only members can read/write farm data
      allow read, write: if request.auth != null
        && exists(/databases/$(database)/documents/farms/$(farmId)/members/$(request.auth.uid));

      match /members/{memberId} {
        // Owner can add/remove members; members can read
        allow read: if request.auth != null;
        allow write: if request.auth != null
          && get(/databases/$(database)/documents/farms/$(farmId)/members/$(request.auth.uid)).data.role == "owner";
      }

      match /{subcollection}/{docId} {
        // All members can CRUD farm data
        allow read, write: if request.auth != null
          && exists(/databases/$(database)/documents/farms/$(farmId)/members/$(request.auth.uid));
      }
    }
  }
}
```

#### Step 4.3: Role-based permissions (future)
- `owner`: full access + can invite/remove members
- `member`: can create/edit activities, irrigations, etc.
- `viewer` (future): read-only access for reports

#### Step 4.4: UI for sharing
- Android: Settings screen → "Share Farm" → shows invite code + copy button
- iOS: SettingsView → "Share Farm" → same
- "Join Farm" flow: enter invite code → lookup → confirm → add as member → start sync

---

### Entity-to-Firestore Field Mapping Reference

| Room Entity             | Firestore Collection        | Document ID        | FK Resolution                    |
|-------------------------|-----------------------------|--------------------|----------------------------------|
| Farmer                  | farmers                     | persistentId       | —                                |
| Farm                    | (top-level doc)             | siteId             | farmer → persistentId            |
| Orchard                 | orchards                    | persistentId       | farm → siteId                    |
| Tree                    | trees                       | persistentId       | orchard → persistentId           |
| OrchardActivity         | orchardActivities           | firestoreId (UUID) | orchard → persistentId           |
| IrrigationSystem        | irrigationSystems           | firestoreId (UUID) | orchard → persistentId, pump → firestoreId |
| Irrigation              | irrigations                 | firestoreId (UUID) | irrigationSystem → firestoreId   |
| Pump                    | pumps                       | firestoreId (UUID) | —                                |
| FertilizerApplication   | fertilizerApplications      | firestoreId (UUID) | orchard → persistentId           |
| FertilizerApplicationItem | (embedded in parent)      | —                  | fertilizer → firestoreId         |
| Fertilizer              | fertilizers                 | firestoreId (UUID) | —                                |
| PesticideApplication    | pesticideApplications       | firestoreId (UUID) | orchard → persistentId           |
| PesticideApplicationItem | (embedded in parent)       | —                  | pesticide → firestoreId          |
| Pesticide               | pesticides                  | firestoreId (UUID) | —                                |
| SoilMoisture            | soilMoisture                | firestoreId (UUID) | orchard → persistentId           |
| SoilTest                | (skip for now)              | —                  | —                                |
| Disease                 | (skip for now)              | —                  | —                                |
| Variety                 | (skip for now)              | —                  | —                                |
| Rootstock               | (skip for now)              | —                  | —                                |

### Notes
- All `Instant` fields stored as `Long` (epoch millis) in both Room and Firestore
- `FertilizerApplicationItem` and `PesticideApplicationItem` are embedded as arrays in their
  parent application document (not separate sub-collections) for atomic writes
- The gitlive firebase-auth/firestore KMP libraries wrap the native Firebase SDKs on each
  platform, so shared Kotlin code works on both Android and iOS
- `SoilTest`, `Disease`, `Variety`, `Rootstock` are lower-priority for sync — add later

---

## DateConverter Migration: String -> Epoch Millis (Long) [COMPLETED]

### Problem
`DateConverter` stores `Instant` as `"MM/DD/YYYY HH:MM"` strings in SQLite. SQL `BETWEEN` queries
fail because string comparison is lexicographic (e.g. `"01/15/2026" < "12/01/2025"` is wrong).
All report DAO queries are broken. iOS report views currently work around this with client-side filtering.

### Goal
Change `DateConverter` to store `Instant` as `Long` (epoch milliseconds). This makes SQL `BETWEEN`
queries work correctly with numeric comparison and eliminates all date format parsing bugs.

### Current DB version: 4
### Target DB version: 5

### Migration Steps (in order)

#### Step 1: Update DateConverter (shared/.../database/DateConverter.kt)
Change from `Instant <-> String` to `Instant <-> Long`:
```kotlin
object DateConverter {
    @TypeConverter
    fun toInstant(millis: Long): Instant = Instant.fromEpochMilliseconds(millis)

    @TypeConverter
    fun fromInstant(date: Instant): Long = date.toEpochMilliseconds()
}
```

#### Step 2: Add display formatter utility (shared/.../database/DateFormatter.kt)
Create a new `DateFormatter` object for display-only formatting (replaces what `fromInstant` used to do):
```kotlin
object DateFormatter {
    fun format(instant: Instant): String { /* MM/DD/YYYY HH:MM for display */ }
    fun formatDate(instant: Instant): String { /* MM/DD/YYYY for display */ }
}
```

#### Step 3: Update model toString() methods
These files call `DateConverter.fromInstant()` for display and need to use `DateFormatter` instead:
- `shared/.../model/Irrigation.kt` (line 20)
- `shared/.../model/FertilizerApplication.kt` (line 24)
- `shared/.../model/PesticideApplication.kt` (line 31)
- `shared/.../model/SoilMoisture.kt` (line 24)

#### Step 4: Write MIGRATION_4_5 (shared/.../database/DatabaseProvider.kt)
Converts all existing string date columns to epoch millis. Must handle the `"MM/DD/YYYY HH:MM"`
and `"MM/DD/YYYY"` formats. Affected tables and columns:

| Table                   | Columns                              |
|-------------------------|--------------------------------------|
| Farm                    | validFrom, validTo                   |
| Orchard                 | validFrom, validTo                   |
| Tree                    | validFrom, validTo                   |
| OrchardActivity         | activityStart, activityStop          |
| FertilizerApplication   | applicationStart, applicationStop    |
| PesticideApplication    | applicationStart, applicationStop    |
| Irrigation              | startTime, stopTime                  |
| SoilMoisture            | date                                 |

Migration strategy per table:
1. Add new temporary Long columns (e.g. `_millis`)
2. Parse existing string values in Kotlin (not SQL) and update the Long columns
3. Drop old string columns, rename Long columns

OR (simpler): Since `fallbackToDestructiveMigration` is enabled, consider if a destructive
migration is acceptable during development. If so, just bump version and let Room recreate.

#### Step 5: Update DatabaseProvider.kt
- Add `MIGRATION_4_5` to `.addMigrations()`
- Update `MIGRATION_3_4` reference to `DateConverter.fromInstant()` — it now returns Long,
  so the default value format in ALTER TABLE statements needs updating
- Bump database version to 5 in `OrchardDatabase.kt`

#### Step 6: Update DAO report queries
These DAOs pass date parameters to `BETWEEN` — they now receive `Long` (epoch millis) automatically
since Room uses the updated TypeConverter. The SQL stays the same, but verify the parameter types
in the ViewModel/Repository calls pass `Instant` (which Room converts to Long):
- `IrrigationDao.kt` (line 26) — `getIrrigationsBySeason(firstYear, endYear)`
- `OrchardWithOrchardActivitiesDao.kt` (line 13) — `getOrchardWithOrchardActivities(orchardId, startDate, endDate)`
- `FertilizerApplicationWithFertilizersDao.kt` (line 13) — `getFertilizerApplicationsWithFertilizers(orchardId, startDate, endDate)`
- `PesticideApplicationWithPesticidesDao.kt` (line 13) — `getPesticideApplicationWithPesticides(orchardId, startDate, endDate)`

#### Step 7: Update Android report fragments
Android Compose fragments that construct date strings for report queries need to pass `Instant` instead:
- `FertilizerComposeFragment.kt` (FertilizerReportComposeFragment)
- `PesticideComposeFragment.kt` (PesticideReportComposeFragment)
- `IrrigationComposeFragment.kt` (IrrigationReportComposeFragment)
- `OrchardActivityComposeFragment.kt` (OrchardTaskReportComposeFragment)

#### Step 8: iOS report views (already done — client-side filtering)
iOS report views already use client-side filtering with Swift Date comparison.
After migration, they could optionally switch back to using the DAO queries since
`BETWEEN` will work correctly with Long values. Not required — client-side filtering works fine.

### Notes
- The `toSwiftDate()` / `toKotlinInstant()` extensions in iOS already use epoch millis, so no iOS changes needed
- `EnumConverter` is unaffected
- Existing MIGRATION_3_4 added validFrom/validTo as TEXT with string default — migration 4->5 must handle these

