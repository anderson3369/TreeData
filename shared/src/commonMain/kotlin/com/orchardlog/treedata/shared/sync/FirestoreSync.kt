package com.orchardlog.treedata.shared.sync

import com.orchardlog.treedata.shared.auth.AuthService
import com.orchardlog.treedata.shared.database.OrchardDatabase
import com.orchardlog.treedata.shared.model.*
import com.orchardlog.treedata.shared.TemporalUtils
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * Pushes local Room data to Firestore when the user is signed in.
 *
 * Firestore structure:
 *   farms/{siteId}                      — Farm document
 *   farms/{siteId}/farmers/{persistentId}
 *   farms/{siteId}/orchards/{persistentId}
 *   farms/{siteId}/orchardActivities/{firestoreId}
 *   farms/{siteId}/irrigationSystems/{firestoreId}
 *   farms/{siteId}/irrigations/{firestoreId}
 *   farms/{siteId}/fertilizers/{firestoreId}
 *   farms/{siteId}/fertilizerApplications/{firestoreId}
 *   farms/{siteId}/pesticides/{firestoreId}
 *   farms/{siteId}/pesticideApplications/{firestoreId}
 *   farms/{siteId}/pumps/{firestoreId}
 *   farms/{siteId}/soilMoisture/{firestoreId}
 *   farms/{siteId}/trees/{persistentId}
 *   farms/{siteId}/rootstocks/{firestoreId}
 *   farms/{siteId}/varieties/{firestoreId}
 */
object FirestoreSync {

    private val firestore = Firebase.firestore

    /** Set by FarmViewModel when farms load. All sync calls use this. */
    var currentFarmSiteId: String = ""

    private val listenerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var irrigationListenerJob: Job? = null

    private fun farmDoc(siteId: String) = firestore.collection("farms").document(siteId)

    // -- Farm --

    suspend fun pushFarm(farm: Farm) {
        if (!shouldSync() || farm.siteId.isBlank()) return
        runSync("pushFarm") {
            val uid = AuthService.currentUid ?: ""
            val doc = farmDoc(farm.siteId)

            val batch = firestore.batch()

            // 1. Create/Update the farm document
            batch.set(
                doc,
                mapOf(
                    "name" to farm.name,
                    "siteId" to farm.siteId,
                    "persistentId" to farm.persistentId,
                    "ownerUid" to uid,
                    "lastModified" to currentMillis()
                )
            )

            // 2. Add the user as the owner in the members subcollection
            if (uid.isNotEmpty()) {
                batch.set(
                    doc.collection("members").document(uid),
                    mapOf(
                        "role" to "owner",
                        "joinedAt" to currentMillis()
                    )
                )
            }

            batch.commit()
        }
    }

    suspend fun deleteFarm(farm: Farm) {
        if (!shouldSync() || farm.siteId.isBlank()) return
        runSync("deleteFarm") {
            farmDoc(farm.siteId).delete()
        }
    }

    // -- Invites --

    private const val INVITE_TTL_MILLIS: Long = 7L * 24 * 60 * 60 * 1000
    private const val INVITE_CODE_LENGTH: Int = 6
    // Crockford-ish alphabet, no 0/O/I/1/L to avoid ambiguity when read aloud.
    private const val INVITE_ALPHABET: String = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * Creates a top-level `invites/{code}` doc for the given farm and
     * returns the share code. Owner must already be a member of the farm.
     * Throws on any failure so the UI can surface the error.
     */
    @Throws(Exception::class)
    suspend fun createInvite(siteId: String): String {
        check(AuthService.isSignedIn) { "Not signed in" }
        require(siteId.isNotBlank()) { "siteId is blank" }
        val uid = AuthService.currentUid ?: error("No uid")
        val code = generateInviteCode()
        firestore.collection("invites").document(code).set(
            mapOf(
                "siteId" to siteId,
                "createdBy" to uid,
                "expiresAt" to currentMillis() + INVITE_TTL_MILLIS
            )
        )
        return code
    }

    /**
     * Redeems a share code: looks up `invites/{code}`, validates expiry,
     * adds the current user to `farms/{siteId}/members/{uid}`, and returns
     * the farm siteId that was joined. Throws on any failure.
     */
    @Throws(Exception::class)
    suspend fun redeemInvite(code: String): String {
        check(AuthService.isSignedIn) { "Not signed in" }
        val uid = AuthService.currentUid ?: error("No uid")
        val normalized = code.trim().uppercase()
        require(normalized.isNotBlank()) { "Code is blank" }

        val doc = firestore.collection("invites").document(normalized).get()
        check(doc.exists) { "Invite not found" }
        val expiresAt = doc.get<Long>("expiresAt")
        check(currentMillis() <= expiresAt) { "Invite expired" }
        val siteId = doc.get<String>("siteId")
        check(siteId.isNotBlank()) { "Invite is malformed" }

        farmDoc(siteId).collection("members").document(uid).set(
            mapOf(
                "role" to "member",
                "joinedAt" to currentMillis(),
                "invitedBy" to doc.get<String>("createdBy")
            )
        )
        return siteId
    }

    private fun generateInviteCode(): String {
        val rng = kotlin.random.Random.Default
        return buildString {
            repeat(INVITE_CODE_LENGTH) {
                append(INVITE_ALPHABET[rng.nextInt(INVITE_ALPHABET.length)])
            }
        }
    }

    // -- Farmer --

    suspend fun pushFarmer(farmer: Farmer, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushFarmer") {
            val docId = farmer.persistentId.ifEmpty { "farmer_${farmer.id}" }
            farmDoc(farmSiteId).collection("farmers").document(docId).set(
                mapOf(
                    "persistentId" to farmer.persistentId,
                    "name" to farmer.name,
                    "address" to farmer.address,
                    "city" to farmer.city,
                    "state" to farmer.state,
                    "zip" to farmer.zip,
                    "phone" to farmer.phone,
                    "email" to farmer.email,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteFarmer(farmer: Farmer, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteFarmer") {
            val docId = farmer.persistentId.ifEmpty { "farmer_${farmer.id}" }
            farmDoc(farmSiteId).collection("farmers").document(docId).delete()
        }
    }

    // -- Orchard --

    suspend fun pushOrchard(orchard: Orchard, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushOrchard") {
            farmDoc(farmSiteId).collection("orchards").document(orchard.persistentId).set(
                mapOf(
                    "persistentId" to orchard.persistentId,
                    "farmId" to orchard.farmId,
                    "crop" to orchard.crop,
                    "plantedDate" to orchard.plantedDate,
                    "rowWidth" to orchard.rowWidth,
                    "rowWidthLinearUnit" to orchard.rowWidthLinearUnit.name,
                    "distanceBetweenTrees" to orchard.distanceBetweenTrees,
                    "distanceBetweenTreesLinearUnit" to orchard.distanceBetweenTreesLinearUnit.name,
                    "sand" to orchard.sand,
                    "silt" to orchard.silt,
                    "clay" to orchard.clay,
                    "organicMatter" to orchard.organicMatter,
                    "validFrom" to orchard.validFrom.toEpochMilliseconds(),
                    "validTo" to orchard.validTo?.toEpochMilliseconds(),
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteOrchard(orchard: Orchard, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteOrchard") {
            farmDoc(farmSiteId).collection("orchards").document(orchard.persistentId).delete()
        }
    }

    // -- OrchardActivity --

    suspend fun pushOrchardActivity(activity: OrchardActivity, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushOrchardActivity") {
            val docId = activity.firestoreId.ifEmpty { "activity_${activity.id}" }
            farmDoc(farmSiteId).collection("orchardActivities").document(docId).set(
                mapOf(
                    "firestoreId" to activity.firestoreId,
                    "orchardId" to activity.orchardId,
                    "activity" to activity.activity,
                    "notes" to activity.notes,
                    "activityStart" to activity.activityStart.toEpochMilliseconds(),
                    "activityStop" to activity.activityStop.toEpochMilliseconds(),
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteOrchardActivity(activity: OrchardActivity, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteOrchardActivity") {
            val docId = activity.firestoreId.ifEmpty { "activity_${activity.id}" }
            farmDoc(farmSiteId).collection("orchardActivities").document(docId).delete()
        }
    }

    // -- IrrigationSystem --

    suspend fun pushIrrigationSystem(system: IrrigationSystem, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushIrrigationSystem") {
            val docId = system.firestoreId.ifEmpty { "irrSys_${system.id}" }
            farmDoc(farmSiteId).collection("irrigationSystems").document(docId).set(
                mapOf(
                    "firestoreId" to system.firestoreId,
                    "orchardId" to system.orchardId,
                    "pumpId" to system.pumpId,
                    "name" to system.name,
                    "irrigationMethod" to system.irrigationMethod.name,
                    "emitterFlowRate" to system.emitterFlowRate,
                    "emitterFlowUnit" to system.emitterFlowUnit.name,
                    "emitterRadius" to system.emitterRadius,
                    "emitterRadiusLinearUnit" to system.emitterRadiusLinearUnit.name,
                    "emitterSpacing" to system.emitterSpacing,
                    "emitterSpacingLinearUnit" to system.emitterSpacingLinearUnit.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteIrrigationSystem(system: IrrigationSystem, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteIrrigationSystem") {
            val docId = system.firestoreId.ifEmpty { "irrSys_${system.id}" }
            farmDoc(farmSiteId).collection("irrigationSystems").document(docId).delete()
        }
    }

    // -- Irrigation --

    suspend fun pushIrrigation(irrigation: Irrigation, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushIrrigation") {
            val docId = irrigation.firestoreId.ifEmpty { "irr_${irrigation.id}" }
            farmDoc(farmSiteId).collection("irrigations").document(docId).set(
                mapOf(
                    "firestoreId" to irrigation.firestoreId,
                    "irrigationSystemId" to irrigation.irrigationSystemId,
                    "startTime" to irrigation.startTime.toEpochMilliseconds(),
                    "stopTime" to irrigation.stopTime.toEpochMilliseconds(),
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteIrrigation(irrigation: Irrigation, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteIrrigation") {
            val docId = irrigation.firestoreId.ifEmpty { "irr_${irrigation.id}" }
            farmDoc(farmSiteId).collection("irrigations").document(docId).delete()
        }
    }

    /**
     * Smoke-test listener: subscribes to remote irrigation changes for the
     * current farm and applies them to Room. Skips events that originated
     * from this client (hasPendingWrites). Safe to call repeatedly — the
     * previous listener is cancelled before a new one starts.
     */
    fun startIrrigationListener(db: OrchardDatabase) {
        if (!shouldSync()) return
        val siteId = currentFarmSiteId
        if (siteId.isBlank()) return
        stopIrrigationListener()
        irrigationListenerJob = listenerScope.launch {
            runSync("irrigationListener") {
                farmDoc(siteId).collection("irrigations").snapshots.collect { snapshot ->
                    for (change in snapshot.documentChanges) {
                        if (change.document.metadata.hasPendingWrites) continue
                        when (change.type) {
                            ChangeType.ADDED, ChangeType.MODIFIED ->
                                applyRemoteIrrigation(db, change.document)
                            ChangeType.REMOVED ->
                                removeRemoteIrrigation(db, change.document.id)
                        }
                    }
                }
            }
        }
    }

    fun stopIrrigationListener() {
        irrigationListenerJob?.cancel()
        irrigationListenerJob = null
    }

    private suspend fun applyRemoteIrrigation(db: OrchardDatabase, doc: DocumentSnapshot) {
        runSync("applyRemoteIrrigation") {
            val firestoreId = doc.id
            val irrigationSystemId = doc.get<Long>("irrigationSystemId")
            val startMillis = doc.get<Long>("startTime")
            val stopMillis = doc.get<Long>("stopTime")
            val dao = db.irrigationDao()
            val existing = dao.getByFirestoreId(firestoreId)
            if (existing == null) {
                dao.insert(
                    Irrigation(
                        irrigationSystemId = irrigationSystemId,
                        startTime = Instant.fromEpochMilliseconds(startMillis),
                        stopTime = Instant.fromEpochMilliseconds(stopMillis),
                        firestoreId = firestoreId
                    )
                )
            } else {
                dao.update(
                    existing.copy(
                        irrigationSystemId = irrigationSystemId,
                        startTime = Instant.fromEpochMilliseconds(startMillis),
                        stopTime = Instant.fromEpochMilliseconds(stopMillis)
                    )
                )
            }
        }
    }

    private suspend fun removeRemoteIrrigation(db: OrchardDatabase, firestoreId: String) {
        runSync("removeRemoteIrrigation") {
            db.irrigationDao().deleteByFirestoreId(firestoreId)
        }
    }

    // -- Fertilizer --

    suspend fun pushFertilizer(fertilizer: Fertilizer, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushFertilizer") {
            val docId = fertilizer.firestoreId.ifEmpty { "fert_${fertilizer.id}" }
            farmDoc(farmSiteId).collection("fertilizers").document(docId).set(
                mapOf(
                    "firestoreId" to fertilizer.firestoreId,
                    "name" to fertilizer.name,
                    "nitrogen" to fertilizer.nitrogen,
                    "phosphorous" to fertilizer.phosphorous,
                    "potassium" to fertilizer.potassium,
                    "sulfur" to fertilizer.sulfur,
                    "calcium" to fertilizer.calcium,
                    "magnesium" to fertilizer.magnesium,
                    "iron" to fertilizer.iron,
                    "zinc" to fertilizer.zinc,
                    "manganese" to fertilizer.manganese,
                    "boron" to fertilizer.boron,
                    "molybdenum" to fertilizer.molybdenum,
                    "chloride" to fertilizer.chloride,
                    "copper" to fertilizer.copper,
                    "selenium" to fertilizer.selenium,
                    "nickel" to fertilizer.nickel,
                    "organicMatter" to fertilizer.organicMatter,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteFertilizer(fertilizer: Fertilizer, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteFertilizer") {
            val docId = fertilizer.firestoreId.ifEmpty { "fert_${fertilizer.id}" }
            farmDoc(farmSiteId).collection("fertilizers").document(docId).delete()
        }
    }

    // -- FertilizerApplication --

    suspend fun pushFertilizerApplication(app: FertilizerApplication, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushFertilizerApplication") {
            val docId = app.firestoreId.ifEmpty { "fertApp_${app.id}" }
            farmDoc(farmSiteId).collection("fertilizerApplications").document(docId).set(
                mapOf(
                    "firestoreId" to app.firestoreId,
                    "orchardId" to app.orchardId,
                    "applicationStart" to app.applicationStart.toEpochMilliseconds(),
                    "applicationStop" to app.applicationStop.toEpochMilliseconds(),
                    "areaTreated" to app.areaTreated,
                    "orchardUnit" to app.orchardUnit.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteFertilizerApplication(app: FertilizerApplication, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteFertilizerApplication") {
            val docId = app.firestoreId.ifEmpty { "fertApp_${app.id}" }
            farmDoc(farmSiteId).collection("fertilizerApplications").document(docId).delete()
        }
    }

    // -- Pesticide --

    suspend fun pushPesticide(pesticide: Pesticide, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushPesticide") {
            val docId = pesticide.firestoreId.ifEmpty { "pest_${pesticide.id}" }
            farmDoc(farmSiteId).collection("pesticides").document(docId).set(
                mapOf(
                    "firestoreId" to pesticide.firestoreId,
                    "productName" to pesticide.productName,
                    "eparegno" to pesticide.eparegno,
                    "signalWord" to pesticide.signalWord.name,
                    "rei" to pesticide.rei,
                    "reiUnit" to pesticide.reiUnit.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deletePesticide(pesticide: Pesticide, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deletePesticide") {
            val docId = pesticide.firestoreId.ifEmpty { "pest_${pesticide.id}" }
            farmDoc(farmSiteId).collection("pesticides").document(docId).delete()
        }
    }

    // -- PesticideApplication --

    suspend fun pushPesticideApplication(app: PesticideApplication, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushPesticideApplication") {
            val docId = app.firestoreId.ifEmpty { "pestApp_${app.id}" }
            farmDoc(farmSiteId).collection("pesticideApplications").document(docId).set(
                mapOf(
                    "firestoreId" to app.firestoreId,
                    "orchardId" to app.orchardId,
                    "applicationStart" to app.applicationStart.toEpochMilliseconds(),
                    "applicationStop" to app.applicationStop.toEpochMilliseconds(),
                    "dilution" to app.dilution,
                    "dilutionUnit" to app.dilutionUnit.name,
                    "areaTreated" to app.areaTreated,
                    "areaTreatedUnit" to app.areaTreatedUnit.name,
                    "applicationMethod" to app.applicationMethod.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deletePesticideApplication(app: PesticideApplication, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deletePesticideApplication") {
            val docId = app.firestoreId.ifEmpty { "pestApp_${app.id}" }
            farmDoc(farmSiteId).collection("pesticideApplications").document(docId).delete()
        }
    }

    // -- Pump --

    suspend fun pushPump(pump: Pump, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushPump") {
            val docId = pump.firestoreId.ifEmpty { "pump_${pump.id}" }
            farmDoc(farmSiteId).collection("pumps").document(docId).set(
                mapOf(
                    "firestoreId" to pump.firestoreId,
                    "type" to pump.type,
                    "horsepower" to pump.horsepower,
                    "phase" to pump.phase,
                    "flowRate" to pump.flowRate,
                    "flowRateUnit" to pump.flowRateUnit.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deletePump(pump: Pump, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deletePump") {
            val docId = pump.firestoreId.ifEmpty { "pump_${pump.id}" }
            farmDoc(farmSiteId).collection("pumps").document(docId).delete()
        }
    }

    // -- SoilMoisture --

    suspend fun pushSoilMoisture(moisture: SoilMoisture, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushSoilMoisture") {
            val docId = moisture.firestoreId.ifEmpty { "sm_${moisture.id}" }
            farmDoc(farmSiteId).collection("soilMoisture").document(docId).set(
                mapOf(
                    "firestoreId" to moisture.firestoreId,
                    "orchardId" to moisture.orchardId,
                    "date" to moisture.date.toEpochMilliseconds(),
                    "centibar" to moisture.centibar,
                    "percent" to moisture.percent,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteSoilMoisture(moisture: SoilMoisture, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteSoilMoisture") {
            val docId = moisture.firestoreId.ifEmpty { "sm_${moisture.id}" }
            farmDoc(farmSiteId).collection("soilMoisture").document(docId).delete()
        }
    }

    // -- Tree --

    suspend fun pushTree(tree: Tree, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushTree") {
            farmDoc(farmSiteId).collection("trees").document(tree.persistentId).set(
                mapOf(
                    "persistentId" to tree.persistentId,
                    "orchardId" to tree.orchardId,
                    "rootstockId" to tree.rootstockId,
                    "varietyId" to tree.varietyId,
                    "plantedDate" to tree.plantedDate,
                    "treeRanking" to tree.treeRanking.name,
                    "notes" to tree.notes,
                    "latitude" to tree.latitude,
                    "longitude" to tree.longitude,
                    "validFrom" to tree.validFrom.toEpochMilliseconds(),
                    "validTo" to tree.validTo?.toEpochMilliseconds(),
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteTree(tree: Tree, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteTree") {
            farmDoc(farmSiteId).collection("trees").document(tree.persistentId).delete()
        }
    }

    // -- Variety --

    suspend fun pushVariety(variety: Variety, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushVariety") {
            val docId = variety.firestoreId.ifEmpty { "var_${variety.id}" }
            farmDoc(farmSiteId).collection("varieties").document(docId).set(
                mapOf(
                    "firestoreId" to variety.firestoreId,
                    "name" to variety.name,
                    "cultivar" to variety.cultivar,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteVariety(variety: Variety, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteVariety") {
            val docId = variety.firestoreId.ifEmpty { "var_${variety.id}" }
            farmDoc(farmSiteId).collection("varieties").document(docId).delete()
        }
    }

    // -- Rootstock --

    suspend fun pushRootstock(rootstock: Rootstock, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushRootstock") {
            val docId = rootstock.firestoreId.ifEmpty { "root_${rootstock.id}" }
            farmDoc(farmSiteId).collection("rootstocks").document(docId).set(
                mapOf(
                    "firestoreId" to rootstock.firestoreId,
                    "name" to rootstock.name,
                    "cultivar" to rootstock.cultivar,
                    "rootstockType" to rootstock.rootstockType.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteRootstock(rootstock: Rootstock, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteRootstock") {
            val docId = rootstock.firestoreId.ifEmpty { "root_${rootstock.id}" }
            farmDoc(farmSiteId).collection("rootstocks").document(docId).delete()
        }
    }

    // -- Disease --

    suspend fun pushDisease(disease: Disease, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushDisease") {
            val docId = disease.firestoreId.ifEmpty { "dis_${disease.id}" }
            farmDoc(farmSiteId).collection("diseases").document(docId).set(
                mapOf(
                    "firestoreId" to disease.firestoreId,
                    "treeId" to disease.treeId,
                    "name" to disease.name,
                    "scientificName" to disease.scientificName,
                    "description" to disease.description,
                    "diseaseType" to disease.diseaseType.name,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteDisease(disease: Disease, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteDisease") {
            val docId = disease.firestoreId.ifEmpty { "dis_${disease.id}" }
            farmDoc(farmSiteId).collection("diseases").document(docId).delete()
        }
    }

    // -- SoilTest --

    suspend fun pushSoilTest(test: SoilTest, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("pushSoilTest") {
            val docId = test.firestoreId.ifEmpty { "st_${test.id}" }
            farmDoc(farmSiteId).collection("soilTests").document(docId).set(
                mapOf(
                    "firestoreId" to test.firestoreId,
                    "orchardId" to test.orchardId,
                    "testDate" to test.testDate,
                    "nitrate" to test.nitrate,
                    "nitrateUnit" to test.nitrateUnit.name,
                    "phosphorousBray" to test.phosphorousBray,
                    "phosphorousBrayUnit" to test.phosphorousBrayUnit.name,
                    "phosphorousNaHCO3" to test.phosphorousNaHCO3,
                    "phosphorousNaHCO3Unit" to test.phosphorousNaHCO3Unit.name,
                    "potassium" to test.potassium,
                    "potassiumUnit" to test.potassiumUnit.name,
                    "calcium" to test.calcium,
                    "calciumUnit" to test.calciumUnit.name,
                    "magnesium" to test.magnesium,
                    "magnesiumUnit" to test.magnesiumUnit.name,
                    "sodium" to test.sodium,
                    "sodiumUnit" to test.sodiumUnit.name,
                    "sulfur" to test.sulfur,
                    "sulfurUnit" to test.sulfurUnit.name,
                    "zinc" to test.zinc,
                    "zincUnit" to test.zincUnit.name,
                    "manganese" to test.manganese,
                    "manganeseUnit" to test.manganeseUnit.name,
                    "iron" to test.iron,
                    "ironUnit" to test.ironUnit.name,
                    "copper" to test.copper,
                    "copperUnit" to test.copperUnit.name,
                    "boron" to test.boron,
                    "boronUnit" to test.boronUnit.name,
                    "chloride" to test.chloride,
                    "chlorideUnit" to test.chlorideUnit.name,
                    "potassiumCation" to test.potassiumCation,
                    "magnesiumCation" to test.magnesiumCation,
                    "calciumCation" to test.calciumCation,
                    "sodiumCation" to test.sodiumCation,
                    "lastModified" to currentMillis()
                )
            )
        }
    }

    suspend fun deleteSoilTest(test: SoilTest, farmSiteId: String) {
        if (!shouldSync() || farmSiteId.isBlank()) return
        runSync("deleteSoilTest") {
            val docId = test.firestoreId.ifEmpty { "st_${test.id}" }
            farmDoc(farmSiteId).collection("soilTests").document(docId).delete()
        }
    }

    // -- Initial Sync (push all local data after first sign-in) --

    /**
     * Reads all local Room data and pushes it to Firestore.
     * Call this once after the user first signs in so that any data
     * created before authentication (e.g. during SetupWizard) gets synced.
     */
    suspend fun pushAllLocalData(db: OrchardDatabase) = runSync("pushAllLocalData") {
        kotlinx.coroutines.withContext(Dispatchers.Default) {
            if (!shouldSync()) return@withContext
            
            // Give the Auth token 500ms to propagate to the Firestore service
            // to avoid PERMISSION_DENIED on the very first write after login.
            kotlinx.coroutines.delay(500)

            val now = TemporalUtils.now()

        // Farms
        val farms = db.farmDao().getFarms(now).first()
        for (farm in farms) {
            pushFarm(farm)
            // Set siteId so subcollection pushes work
            if (currentFarmSiteId.isBlank() && farm.siteId.isNotBlank()) {
                currentFarmSiteId = farm.siteId
            }
        }

        val siteId = currentFarmSiteId
        if (siteId.isBlank()) return@withContext

        // Farmers
        val farmers = db.farmerDao().getFarmers().first()
        for (farmer in farmers) { pushFarmer(farmer, siteId) }

        // Orchards
        val orchards = db.orchardDao().getOrchards(now).first()
        for (orchard in orchards) { pushOrchard(orchard, siteId) }

        // Orchard Activities
        val activities = db.orchardActivityDao().getOrchardActivities().first()
        for (activity in activities) { pushOrchardActivity(activity, siteId) }

        // Trees
        val trees = db.treeDao().getAllTrees(now).first()
        for (tree in trees) { pushTree(tree, siteId) }

        // Rootstocks
        val rootstocks = db.rootstockDao().getRootstocks().first()
        for (rootstock in rootstocks) { pushRootstock(rootstock, siteId) }

        // Varieties
        val varieties = db.varietyDao().getVarieties().first()
        for (variety in varieties) { pushVariety(variety, siteId) }

        // Pumps
        val pumps = db.pumpDao().getPumps().first()
        for (pump in pumps) { pushPump(pump, siteId) }

        // Irrigation Systems
        val systems = db.irrigationSystemDao().getIrrigationSystems().first()
        for (system in systems) { pushIrrigationSystem(system, siteId) }

        // Irrigations
        val irrigations = db.irrigationDao().getIrrigations().first()
        for (irrigation in irrigations) { pushIrrigation(irrigation, siteId) }

        // Fertilizers
        val fertilizers = db.fertilizerDao().getFertilizers().first()
        for (fertilizer in fertilizers) { pushFertilizer(fertilizer, siteId) }

        // Fertilizer Applications
        val fertApps = db.fertilizerApplicationDao().getFertilizerApplications().first()
        for (app in fertApps) { pushFertilizerApplication(app, siteId) }

        // Pesticides
        val pesticides = db.pesticideDao().getPesticides().first()
        for (pesticide in pesticides) { pushPesticide(pesticide, siteId) }

        // Pesticide Applications
        val pestApps = db.pesticideApplicationDao().getPesticideApplications().first()
        for (app in pestApps) { pushPesticideApplication(app, siteId) }

        // Soil Moisture
        val soilMoistures = db.soilMoistureDao().getSoilMoisture().first()
        for (moisture in soilMoistures) { pushSoilMoisture(moisture, siteId) }

        startIrrigationListener(db)
    }
}

    // -- Helpers --

    private fun shouldSync(): Boolean = AuthService.isSignedIn

    private fun currentMillis(): Long =
        TemporalUtils.now().toEpochMilliseconds()

    /**
     * Wraps a Firestore write so that any failure (permission denied,
     * network, stale auth token, etc.) is logged but does NOT propagate
     * out into the caller's coroutine. Room is the source of truth; a
     * dropped push will be re-attempted by pushAllLocalData on the next
     * sign-in. CancellationException is always re-thrown so scope
     * cancellation still works.
     */
    private suspend inline fun runSync(operation: String, crossinline block: suspend () -> Unit) {
        try {
            block()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            println("[FirestoreSync] $operation failed: ${e::class.simpleName}: ${e.message}")
        }
    }
}
