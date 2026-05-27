package com.orchardlog.treedata.ui.trees

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.TemporalUtils
import com.orchardlog.treedata.shared.model.*
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.TreeViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*
import com.orchardlog.treedata.utils.DatePickerFragment
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

class TreeComposeFragment : Fragment(), LocationListener {

    private val treeViewModel: TreeViewModel = ViewModelProvider.treeViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    // GPS state
    private var isAcquiringGps by mutableStateOf(false)
    private var accuracyMessage by mutableStateOf("")
    private var bestLocation by mutableStateOf<Location?>(null)
    private var onGpsAcquired: ((Location) -> Unit)? = null

    // Map-to-form communication
    private var treeToEdit by mutableStateOf<Tree?>(null)
    private var newTreeLatitude by mutableStateOf("")
    private var newTreeLongitude by mutableStateOf("")
    private var switchToFormTab by mutableStateOf(false)

    // Hoisted Form State
    private var formSelectedTree by mutableStateOf<Tree?>(null)
    private var formSelectedOrchardId by mutableLongStateOf(0L)
    private var formSelectedRootstockId by mutableLongStateOf(0L)
    private var formSelectedVarietyId by mutableLongStateOf(0L)
    private var formPlantedDate by mutableStateOf("")
    private var formSelectedRanking by mutableStateOf(TreeRanking.GOOD)
    private var formNotes by mutableStateOf("")
    private var formLatitude by mutableStateOf("")
    private var formLongitude by mutableStateOf("")

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            isAcquiringGps = true
            accuracyMessage = "Acquiring GPS fix..."
            startHighAccuracyGps()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    TreeTabScreen(
                        treeViewModel = treeViewModel,
                        orchardViewModel = orchardViewModel,
                        fragment = this@TreeComposeFragment,
                        isAcquiringGps = isAcquiringGps,
                        accuracyMessage = accuracyMessage,
                        bestLocation = bestLocation,
                        treeToEdit = treeToEdit,
                        newTreeLatitude = newTreeLatitude,
                        newTreeLongitude = newTreeLongitude,
                        switchToFormTab = switchToFormTab,
                        // Form state
                        formSelectedTree = formSelectedTree,
                        formSelectedOrchardId = formSelectedOrchardId,
                        formSelectedRootstockId = formSelectedRootstockId,
                        formSelectedVarietyId = formSelectedVarietyId,
                        formPlantedDate = formPlantedDate,
                        formSelectedRanking = formSelectedRanking,
                        formNotes = formNotes,
                        formLatitude = formLatitude,
                        formLongitude = formLongitude,
                        onFormStateChanged = { tree, orchardId, rootstockId, varietyId, date, ranking, notes, lat, lon ->
                            formSelectedTree = tree
                            formSelectedOrchardId = orchardId
                            formSelectedRootstockId = rootstockId
                            formSelectedVarietyId = varietyId
                            formPlantedDate = date
                            formSelectedRanking = ranking
                            formNotes = notes
                            formLatitude = lat
                            formLongitude = lon
                        },
                        onSwitchHandled = {
                            switchToFormTab = false
                            treeToEdit = null
                            newTreeLatitude = ""
                            newTreeLongitude = ""
                        },
                        onRequestNewTreeGps = {
                            requestHighAccuracyLocation { location ->
                                newTreeLatitude = location.latitude.toString()
                                newTreeLongitude = location.longitude.toString()
                                switchToFormTab = true
                            }
                        },
                        onSelectTreeFromMap = { tree ->
                            treeToEdit = tree
                            switchToFormTab = true
                        },
                        onAcceptCurrentFix = { acceptCurrentLocation() },
                        onCancelGps = { cancelGps() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Auto-request location when fragment starts to center the map
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            isAcquiringGps = true
            accuracyMessage = "Acquiring GPS fix..."
            startHighAccuracyGps()
        }
    }

    private fun requestHighAccuracyLocation(onAcquired: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        onGpsAcquired = onAcquired
        isAcquiringGps = true
        accuracyMessage = "Acquiring GPS fix..."
        bestLocation = null
        startHighAccuracyGps()
    }

    @SuppressLint("MissingPermission")
    private fun startHighAccuracyGps() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Get last known location immediately for faster startup
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && bestLocation == null) {
                bestLocation = location
            }
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Request updates from both providers to ensure a fix as fast as possible
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, this)
        }
    }

    private fun acceptCurrentLocation() {
        bestLocation?.let { location ->
            stopGps()
            onGpsAcquired?.invoke(location)
            onGpsAcquired = null
        }
    }

    private fun cancelGps() {
        stopGps()
        onGpsAcquired = null
    }

    private fun stopGps() {
        isAcquiringGps = false
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        bestLocation = location
        val accuracy = location.accuracy

        if (accuracy <= 5.0f) {
            accuracyMessage = String.format("Accuracy: %.1f m", accuracy)
            stopGps()
            onGpsAcquired?.invoke(location)
            onGpsAcquired = null
        } else {
            accuracyMessage = String.format("Accuracy: %.1f m (refining...)", accuracy)
        }
    }

    override fun onStop() {
        super.onStop()
        stopGps()
        onGpsAcquired = null
    }
}

// Tab screen with segmented picker
@Composable
fun TreeTabScreen(
    treeViewModel: TreeViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment,
    isAcquiringGps: Boolean,
    accuracyMessage: String,
    bestLocation: Location?,
    treeToEdit: Tree?,
    newTreeLatitude: String,
    newTreeLongitude: String,
    switchToFormTab: Boolean,
    // Form state
    formSelectedTree: Tree?,
    formSelectedOrchardId: Long,
    formSelectedRootstockId: Long,
    formSelectedVarietyId: Long,
    formPlantedDate: String,
    formSelectedRanking: TreeRanking,
    formNotes: String,
    formLatitude: String,
    formLongitude: String,
    onFormStateChanged: (Tree?, Long, Long, Long, String, TreeRanking, String, String, String) -> Unit,
    onSwitchHandled: () -> Unit,
    onRequestNewTreeGps: () -> Unit,
    onSelectTreeFromMap: (Tree) -> Unit,
    onAcceptCurrentFix: () -> Unit,
    onCancelGps: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Handle tab switch from map
    LaunchedEffect(switchToFormTab) {
        if (switchToFormTab) {
            selectedTab = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        // Header with Tabs
        Surface(elevation = 2.dp) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Text(
                    text = stringResource(id = R.string.trees),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    backgroundColor = Color.White,
                    contentColor = MaterialTheme.colors.primary,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Details", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Map", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                TreeFormTab(
                    treeViewModel = treeViewModel,
                    orchardViewModel = orchardViewModel,
                    fragment = fragment,
                    isAcquiringGps = isAcquiringGps,
                    accuracyMessage = accuracyMessage,
                    bestLocation = bestLocation,
                    treeToEdit = treeToEdit,
                    newTreeLatitude = newTreeLatitude,
                    newTreeLongitude = newTreeLongitude,
                    // Form state
                    selectedTree = formSelectedTree,
                    selectedOrchardId = formSelectedOrchardId,
                    selectedRootstockId = formSelectedRootstockId,
                    selectedVarietyId = formSelectedVarietyId,
                    plantedDate = formPlantedDate,
                    selectedRanking = formSelectedRanking,
                    notes = formNotes,
                    latitude = formLatitude,
                    longitude = formLongitude,
                    onFormStateChanged = onFormStateChanged,
                    onSwitchHandled = onSwitchHandled,
                    onRequestNewTreeGps = onRequestNewTreeGps,
                    onAcceptCurrentFix = onAcceptCurrentFix,
                    onCancelGps = onCancelGps
                )
            } else {
                TreeMapTab(
                    treeViewModel = treeViewModel,
                    fragment = fragment,
                    isAcquiringGps = isAcquiringGps,
                    accuracyMessage = accuracyMessage,
                    bestLocation = bestLocation,
                    onRequestNewTreeGps = onRequestNewTreeGps,
                    onSelectTree = onSelectTreeFromMap,
                    onAcceptCurrentFix = onAcceptCurrentFix,
                    onCancelGps = onCancelGps
                )
            }
        }
    }
}

// Tree Form Tab - matches iOS TreeFormView
@Composable
fun TreeFormTab(
    treeViewModel: TreeViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment,
    isAcquiringGps: Boolean,
    accuracyMessage: String,
    bestLocation: Location?,
    treeToEdit: Tree?,
    newTreeLatitude: String,
    newTreeLongitude: String,
    // Hoisted form state
    selectedTree: Tree?,
    selectedOrchardId: Long,
    selectedRootstockId: Long,
    selectedVarietyId: Long,
    plantedDate: String,
    selectedRanking: TreeRanking,
    notes: String,
    latitude: String,
    longitude: String,
    onFormStateChanged: (Tree?, Long, Long, Long, String, TreeRanking, String, String, String) -> Unit,
    onSwitchHandled: () -> Unit,
    onRequestNewTreeGps: () -> Unit,
    onAcceptCurrentFix: () -> Unit,
    onCancelGps: () -> Unit
) {
    val trees by treeViewModel.allTrees.collectAsStateWithLifecycle(initialValue = emptyList())
    val orchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val rootstocks by treeViewModel.rootstocks.collectAsStateWithLifecycle(initialValue = emptyList())
    val varieties by treeViewModel.varieties.collectAsStateWithLifecycle(initialValue = emptyList())

    val context = LocalContext.current
    val treeRankings = TreeRanking.entries.toTypedArray()
    stringArrayResource(id = R.array.tree_ranking_descriptions)

    // Helper to update state
    fun updateState(
        t: Tree? = selectedTree,
        oId: Long = selectedOrchardId,
        rId: Long = selectedRootstockId,
        vId: Long = selectedVarietyId,
        date: String = plantedDate,
        rank: TreeRanking = selectedRanking,
        n: String = notes,
        lat: String = latitude,
        lon: String = longitude
    ) {
        onFormStateChanged(t, oId, rId, vId, date, rank, n, lat, lon)
    }

    // Inline add rootstock/variety
    var showAddRootstock by remember { mutableStateOf(false) }
    var showAddVariety by remember { mutableStateOf(false) }
    var newRootstockName by remember { mutableStateOf("") }
    var newRootstockCultivar by remember { mutableStateOf("") }
    var newRootstockType by remember { mutableStateOf(RootstockType.BAREROOT) }
    var newVarietyName by remember { mutableStateOf("") }
    var newVarietyCultivar by remember { mutableStateOf("") }

    fun populateFields(tree: Tree?) {
        if (tree != null) {
            updateState(
                t = tree,
                oId = tree.orchardId,
                rId = tree.rootstockId,
                vId = tree.varietyId,
                date = tree.plantedDate.toString(),
                rank = tree.treeRanking,
                n = tree.notes,
                lat = tree.latitude.toString(),
                lon = tree.longitude.toString()
            )
        } else {
            updateState(
                t = null,
                date = "",
                rank = TreeRanking.GOOD,
                n = "",
                lat = bestLocation?.latitude?.toString() ?: "",
                lon = bestLocation?.longitude?.toString() ?: ""
            )
        }
    }

    fun resetForm() {
        populateFields(null)
    }

    // Consolidate field population to prevent race conditions and ensure coordinates are saved
    LaunchedEffect(treeToEdit, newTreeLatitude) {
        if (newTreeLatitude.isNotEmpty()) {
            updateState(
                t = null,
                date = "",
                rank = TreeRanking.GOOD,
                n = "",
                lat = newTreeLatitude,
                lon = newTreeLongitude
            )
            onSwitchHandled()
        } else if (treeToEdit != null) {
            populateFields(treeToEdit)
            onSwitchHandled()
        }
    }

    // Use rememberUpdatedState to ensure listeners always use the latest form values
    val currentOnFormStateChanged by rememberUpdatedState(onFormStateChanged)
    val latestTree by rememberUpdatedState(selectedTree)
    val latestOrchardId by rememberUpdatedState(selectedOrchardId)
    val latestRootstockId by rememberUpdatedState(selectedRootstockId)
    val latestVarietyId by rememberUpdatedState(selectedVarietyId)
    val latestDate by rememberUpdatedState(plantedDate)
    val latestRanking by rememberUpdatedState(selectedRanking)
    val latestNotes by rememberUpdatedState(notes)
    val latestLat by rememberUpdatedState(latitude)
    val latestLon by rememberUpdatedState(longitude)

    // Listen for date picker results
    DisposableEffect(fragment) {
        val listener = androidx.fragment.app.FragmentResultListener { _, bundle ->
            val newDate = bundle.getString("plantedDateKey") ?: latestDate
            currentOnFormStateChanged(
                latestTree,
                latestOrchardId,
                latestRootstockId,
                latestVarietyId,
                newDate,
                latestRanking,
                latestNotes,
                latestLat,
                latestLon
            )
        }
        fragment.childFragmentManager.setFragmentResultListener("plantedDateRequestKey", fragment, listener)
        onDispose {
            fragment.childFragmentManager.clearFragmentResultListener("plantedDateRequestKey")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // GPS acquisition banner
        if (isAcquiringGps) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(accuracyMessage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (bestLocation != null) {
                            Button(
                                onClick = onAcceptCurrentFix,
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                            ) {
                                Text("Use Current Fix", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        OutlinedButton(onClick = onCancelGps) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Existing Trees section
        FieldGroup(title = "EXISTING TREES") {
            var treeExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Tree",
                    value = selectedTree?.let { "Tree #${it.id} - ${it.treeRanking.ranking}" } ?: "New Tree",
                    onClick = { treeExpanded = true }
                )
                DropdownMenu(expanded = treeExpanded, onDismissRequest = { treeExpanded = false }) {
                    DropdownMenuItem(onClick = {
                        resetForm()
                        onRequestNewTreeGps()
                        treeExpanded = false
                    }) {
                        Text("New Tree")
                    }
                    (trees ?: emptyList()).forEach { t ->
                        DropdownMenuItem(onClick = {
                            populateFields(t)
                            treeExpanded = false
                        }) {
                            Text("Tree #${t.id} - ${t.treeRanking.ranking} (${"%.5f".format(t.latitude)}, ${"%.5f".format(t.longitude)})")
                        }
                    }
                }
            }
        }

        // Tree Details section
        FieldGroup(title = "TREE DETAILS") {
            // Orchard
            var orchardExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Orchard",
                    value = (orchardsMap ?: emptyMap())[selectedOrchardId] ?: "Select",
                    onClick = { orchardExpanded = true }
                )
                DropdownMenu(expanded = orchardExpanded, onDismissRequest = { orchardExpanded = false }) {
                    (orchardsMap ?: emptyMap()).forEach { (id, name) ->
                        DropdownMenuItem(onClick = { updateState(oId = id); orchardExpanded = false }) { Text(name) }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Planted Date
            FieldRow(
                label = stringResource(id = R.string.planted_date),
                value = plantedDate,
                isPill = true,
                onClick = {
                    DatePickerFragment("plantedDateRequestKey", "plantedDateKey")
                        .show(fragment.childFragmentManager, "plantedDate")
                }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Tree Ranking
            var rankingExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Ranking",
                    value = selectedRanking.ranking,
                    onClick = { rankingExpanded = true }
                )
                DropdownMenu(expanded = rankingExpanded, onDismissRequest = { rankingExpanded = false }) {
                    treeRankings.forEach { r ->
                        DropdownMenuItem(onClick = {
                            updateState(rank = r)
                            rankingExpanded = false
                        }) {
                            Text(r.ranking)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Notes
            TransparentInputField(
                label = stringResource(id = R.string.notes),
                value = notes,
                onValueChange = { updateState(n = it) }
            )
        }

        // Rootstock section
        FieldGroup(title = "ROOTSTOCK") {
            var rootstockExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Rootstock",
                    value = (rootstocks ?: emptyList()).find { it.id == selectedRootstockId }?.name ?: "Select",
                    onClick = { rootstockExpanded = true }
                )
                DropdownMenu(expanded = rootstockExpanded, onDismissRequest = { rootstockExpanded = false }) {
                    (rootstocks ?: emptyList()).forEach { r ->
                        DropdownMenuItem(onClick = {
                            updateState(rId = r.id)
                            rootstockExpanded = false
                        }) {
                            Text(r.name)
                        }
                    }
                }
            }
        }
        TextButton(onClick = { showAddRootstock = !showAddRootstock }) {
            Icon(
                if (showAddRootstock) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (showAddRootstock) "Hide" else "Add Rootstock")
        }
        if (showAddRootstock) {
            FieldGroup {
                TransparentInputField("Name", newRootstockName) { newRootstockName = it }
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(stringResource(id = R.string.cultivar), newRootstockCultivar) { newRootstockCultivar = it }
                Divider(modifier = Modifier.padding(start = 16.dp))
                var typeExpanded by remember { mutableStateOf(false) }
                Box {
                    FieldRow(
                        label = stringResource(id = R.string.rootstock_type),
                        value = newRootstockType.type,
                        onClick = { typeExpanded = true }
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        RootstockType.entries.forEach { type ->
                            DropdownMenuItem(onClick = {
                                newRootstockType = type
                                typeExpanded = false
                            }) {
                                Text(type.type)
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        if (newRootstockName.isNotEmpty()) {
                            val r = Rootstock(0L, newRootstockName, newRootstockCultivar, newRootstockType)
                            treeViewModel.addRootstock(r)
                            newRootstockName = ""
                            newRootstockCultivar = ""
                            showAddRootstock = false
                            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Rootstock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Variety section
        FieldGroup(title = "VARIETY") {
            var varietyExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Variety",
                    value = (varieties ?: emptyList()).find { it.id == selectedVarietyId }?.name ?: "Select",
                    onClick = { varietyExpanded = true }
                )
                DropdownMenu(expanded = varietyExpanded, onDismissRequest = { varietyExpanded = false }) {
                    (varieties ?: emptyList()).forEach { v ->
                        DropdownMenuItem(onClick = {
                            updateState(vId = v.id)
                            varietyExpanded = false
                        }) {
                            Text(v.name)
                        }
                    }
                }
            }
        }
        TextButton(onClick = { showAddVariety = !showAddVariety }) {
            Icon(
                if (showAddVariety) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (showAddVariety) "Hide" else "Add Variety")
        }
        if (showAddVariety) {
            FieldGroup {
                TransparentInputField("Name", newVarietyName) { newVarietyName = it }
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(stringResource(id = R.string.cultivar), newVarietyCultivar) { newVarietyCultivar = it }
                Button(
                    onClick = {
                        if (newVarietyName.isNotEmpty()) {
                            val v = Variety(0L, newVarietyName, newVarietyCultivar)
                            treeViewModel.addVariety(v)
                            newVarietyName = ""
                            newVarietyCultivar = ""
                            showAddVariety = false
                            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Variety", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Location section
        FieldGroup(title = "LOCATION") {
            TransparentInputField("Latitude", latitude, isNumber = true) { updateState(lat = it) }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Longitude", longitude, isNumber = true) { updateState(lon = it) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val latVal = latitude.trim().toDoubleOrNull() ?: 0.0
                    val lonVal = longitude.trim().toDoubleOrNull() ?: 0.0

                    if (selectedOrchardId == 0L) {
                        Toast.makeText(context, R.string.please_select_an_orchard, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (latVal == 0.0 || lonVal == 0.0) {
                        Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val dateStr = try {
                        val parts = plantedDate.split("-")
                        if (parts.size == 3) {
                            // MM-dd-yyyy format from DatePickerFragment
                            // Converting to ISO-8601 YYYY-MM-DD for storage
                            val year = parts[2]
                            val month = parts[0].padStart(2, '0')
                            val day = parts[1].padStart(2, '0')
                            "$year-$month-$day"
                        } else {
                            plantedDate // Already ISO or other
                        }
                    } catch (e: Exception) {
                        null
                    } ?: return@Button

                    val t = Tree(
                        id = selectedTree?.id ?: 0L,
                        persistentId = selectedTree?.persistentId ?: TemporalUtils.randomUUID(),
                        orchardId = selectedOrchardId,
                        rootstockId = selectedRootstockId,
                        varietyId = selectedVarietyId,
                        plantedDate = dateStr,
                        treeRanking = selectedRanking,
                        notes = notes,
                        latitude = latVal,
                        longitude = lonVal,
                        validFrom = selectedTree?.validFrom ?: TemporalUtils.now(),
                        validTo = selectedTree?.validTo
                    )
                    if (t.id > 0) {
                        treeViewModel.updateTree(t)
                    } else {
                        treeViewModel.addTree(t)
                    }
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(id = R.string.save_button_text), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    resetForm()
                    onRequestNewTreeGps()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
            ) {
                Text(stringResource(id = R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedTree?.let {
                        treeViewModel.deleteTree(it)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedTree != null
            ) {
                Text(stringResource(id = R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Map Tab - matches iOS TreeMapView
@Composable
fun TreeMapTab(
    treeViewModel: TreeViewModel,
    fragment: Fragment,
    isAcquiringGps: Boolean,
    accuracyMessage: String,
    bestLocation: Location?,
    onRequestNewTreeGps: () -> Unit,
    onSelectTree: (Tree) -> Unit,
    onAcceptCurrentFix: () -> Unit,
    onCancelGps: () -> Unit
) {
    val trees by treeViewModel.allTrees.collectAsStateWithLifecycle(initialValue = emptyList())
    val varieties by treeViewModel.varieties.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    // Center map on user location when first acquired
    LaunchedEffect(bestLocation, mapView) {
        val loc = bestLocation
        val map = mapView
        if (loc != null && map != null && !hasCenteredOnUser) {
            map.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
            if (loc.accuracy <= 20f) {
                hasCenteredOnUser = true
            }
        }
    }

    // Lifecycle management for MapView
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun varietyName(varietyId: Long): String {
        return (varieties ?: emptyList()).find { it.id == varietyId }?.name ?: "Unknown"
    }

    fun rankingColor(ranking: TreeRanking): Int {
        return when (ranking) {
            TreeRanking.EXCELLENT -> android.graphics.Color.rgb(76, 175, 80)   // green
            TreeRanking.GOOD -> android.graphics.Color.rgb(33, 150, 243)       // blue
            TreeRanking.MODERATE -> android.graphics.Color.rgb(255, 235, 59)   // yellow
            TreeRanking.POOR -> android.graphics.Color.rgb(255, 152, 0)        // orange
            TreeRanking.DYING -> android.graphics.Color.rgb(244, 67, 54)       // red
        }
    }

    // Update markers when trees change
    LaunchedEffect(trees, varieties, mapView) {
        mapView?.let { map ->
            val overlays = map.overlays
            overlays.removeAll { it is Marker }

            (trees ?: emptyList()).forEach { tree ->
                if (tree.latitude != 0.0 || tree.longitude != 0.0) {
                    val marker = Marker(map)
                    marker.position = GeoPoint(tree.latitude, tree.longitude)
                    marker.icon = ResourcesCompat.getDrawable(context.resources, R.drawable.baseline_edit_location_24, null)
                    marker.icon?.setTint(rankingColor(tree.treeRanking))
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = varietyName(tree.varietyId)
                    marker.snippet = "Ranking: ${tree.treeRanking.ranking}\nLat: ${"%.6f".format(tree.latitude)}\nLon: ${"%.6f".format(tree.longitude)}"
                    marker.setOnMarkerClickListener { _, _ ->
                        onSelectTree(tree)
                        true
                    }
                    map.overlayManager.add(marker)
                }
            }
            map.invalidate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid MapView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    controller.setZoom(18.5)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setMultiTouchControls(true)

                    // Copyright overlay
                    val copyright = CopyrightOverlay(ctx)
                    copyright.setAlignRight(true)
                    copyright.setAlignBottom(true)
                    overlayManager.add(copyright)

                    mapView = this

                    // Zoom to user location on startup
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        lastKnown?.let {
                            controller.animateTo(GeoPoint(it.latitude, it.longitude))
                        }
                    }

                    // Mapnik is usually the most reliable
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    
                    // Force cache clearing for this session if we suspect stale failed requests
                    tileProvider.clearTileCache()
                }
            },
            update = { /* markers handled by LaunchedEffect */ }
        )

        // Overlay controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // GPS acquisition banner
            if (isAcquiringGps) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(accuracyMessage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (bestLocation != null) {
                                Button(
                                    onClick = onAcceptCurrentFix,
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Use Current Fix", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(onClick = onCancelGps) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Zoom controls
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(
                        onClick = { mapView?.controller?.zoomIn() },
                        modifier = Modifier.size(44.dp),
                        backgroundColor = Color.White
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    FloatingActionButton(
                        onClick = { mapView?.controller?.zoomOut() },
                        modifier = Modifier.size(44.dp),
                        backgroundColor = Color.White
                    ) {
                        Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // New Tree button
                if (!isAcquiringGps) {
                    FloatingActionButton(
                        onClick = onRequestNewTreeGps,
                        backgroundColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Tree", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
