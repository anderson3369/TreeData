package com.orchardlog.treedata.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.*
import com.orchardlog.treedata.shared.viewmodels.*
import com.orchardlog.treedata.shared.TemporalUtils
import com.orchardlog.treedata.utils.DatePickerFragment

/**
 * Setup wizard that guides first-time users through creating
 * their Farmer, Farm, and Orchard records.
 *
 * Design:
 * - Step-based state machine (WizardStep enum)
 * - Skips completed steps on resume
 * - Future: add WizardStep.IMPORT for Firestore import
 * - Future: cancel button to skip wizard and import instead
 */
class SetupWizardFragment : Fragment() {

    private val farmerViewModel: FarmerViewModel = ViewModelProvider.farmerViewModel
    private val farmViewModel: FarmViewModel = ViewModelProvider.farmViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SetupWizardScreen(
                        farmerViewModel = farmerViewModel,
                        farmViewModel = farmViewModel,
                        orchardViewModel = orchardViewModel,
                        fragment = this@SetupWizardFragment,
                        onComplete = {
                            findNavController().navigate(R.id.action_nav_setup_to_nav_orchardTask)
                        }
                    )
                }
            }
        }
    }
}

enum class WizardStep(val index: Int, val title: String) {
    FARMER(0, "Farmer"),
    FARM(1, "Farm"),
    ORCHARD(2, "Orchard");
    // Future: IMPORT(3, "Import Data")
}

@Composable
fun SetupWizardScreen(
    farmerViewModel: FarmerViewModel,
    farmViewModel: FarmViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment,
    onComplete: () -> Unit
) {
    val farmers by farmerViewModel.farmers.collectAsStateWithLifecycle(initialValue = null)
    val farms by farmViewModel.farms.collectAsStateWithLifecycle(initialValue = null)
    val orchards by orchardViewModel.allOrchards.collectAsStateWithLifecycle(initialValue = null)

    // Determine starting step based on existing data
    var currentStep by remember { mutableStateOf(WizardStep.FARMER) }
    var initialized by remember { mutableStateOf(false) }

    // Saved IDs from previous steps (for FK relationships)
    var savedFarmerId by remember { mutableLongStateOf(0L) }
    var savedFarmId by remember { mutableLongStateOf(0L) }

    // Skip to the first incomplete step
    LaunchedEffect(farmers, farms, orchards) {
        val f = farmers
        val fs = farms
        val o = orchards
        if (!initialized && f != null && fs != null && o != null) {
            when {
                f.isEmpty() -> currentStep = WizardStep.FARMER
                fs.isEmpty() -> {
                    f.firstOrNull()?.id?.let { savedFarmerId = it }
                    currentStep = WizardStep.FARM
                }
                o.isEmpty() -> {
                    f.firstOrNull()?.id?.let { savedFarmerId = it }
                    fs.firstOrNull()?.id?.let { savedFarmId = it }
                    currentStep = WizardStep.ORCHARD
                }
                else -> onComplete()
            }
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Orchard") },
                backgroundColor = Color(0xFF4CAF50),
                contentColor = Color.White
                // Future: add cancel/import action here
                // actions = {
                //     TextButton(onClick = { /* navigate to import screen */ }) {
                //         Text("Import", color = Color.White)
                //     }
                // }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Step indicator
            StepIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "wizard_step"
            ) { step ->
                when (step) {
                    WizardStep.FARMER -> FarmerStep(
                        farmerViewModel = farmerViewModel,
                        existingFarmer = farmers?.firstOrNull(),
                        onNext = { farmerId ->
                            savedFarmerId = farmerId
                            currentStep = WizardStep.FARM
                        }
                    )
                    WizardStep.FARM -> FarmStep(
                        farmViewModel = farmViewModel,
                        farmerId = savedFarmerId,
                        existingFarm = farms?.firstOrNull(),
                        onBack = { currentStep = WizardStep.FARMER },
                        onNext = { farmId ->
                            savedFarmId = farmId
                            currentStep = WizardStep.ORCHARD
                        }
                    )
                    WizardStep.ORCHARD -> OrchardStep(
                        orchardViewModel = orchardViewModel,
                        farmId = savedFarmId,
                        existingOrchard = orchards?.firstOrNull(),
                        fragment = fragment,
                        onBack = { currentStep = WizardStep.FARM },
                        onComplete = onComplete
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: WizardStep, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WizardStep.entries.forEachIndexed { index, step ->
            val isCompleted = step.index < currentStep.index
            val isCurrent = step == currentStep

            // Step circle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> Color(0xFF4CAF50)
                                isCurrent -> Color(0xFF2196F3)
                                else -> Color.LightGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("${step.index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    step.title,
                    fontSize = 12.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) Color(0xFF2196F3) else Color.Gray
                )
            }

            // Connector line between steps
            if (index < WizardStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .background(if (isCompleted) Color(0xFF4CAF50) else Color.LightGray)
                )
            }
        }
    }
}

// Step 1: Farmer Information
@Composable
fun FarmerStep(
    farmerViewModel: FarmerViewModel,
    existingFarmer: Farmer?,
    onNext: (Long) -> Unit
) {
    val context = LocalContext.current
    val farmers by farmerViewModel.farmers.collectAsStateWithLifecycle(initialValue = null)

    var name by remember { mutableStateOf(existingFarmer?.name ?: "") }
    var address by remember { mutableStateOf(existingFarmer?.address ?: "") }
    var city by remember { mutableStateOf(existingFarmer?.city ?: "") }
    var state by remember { mutableStateOf(existingFarmer?.state ?: "") }
    var zip by remember { mutableStateOf(existingFarmer?.zip ?: "") }
    var phone by remember { mutableStateOf(existingFarmer?.phone ?: "") }
    var email by remember { mutableStateOf(existingFarmer?.email ?: "") }

    LaunchedEffect(existingFarmer) {
        existingFarmer?.let {
            name = it.name; address = it.address; city = it.city
            state = it.state; zip = it.zip; phone = it.phone; email = it.email
        }
    }

    // Watch for newly saved farmer to advance
    var waitingForSave by remember { mutableStateOf(false) }
    LaunchedEffect(farmers, waitingForSave) {
        val f = farmers
        if (waitingForSave && f != null && f.isNotEmpty()) {
            waitingForSave = false
            onNext(f.first().id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Tell us about yourself", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("We need your basic information to get started.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(2f), singleLine = true)
                    OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Zip") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val farmer = Farmer(
                    id = existingFarmer?.id ?: 0L,
                    persistentId = existingFarmer?.persistentId ?: TemporalUtils.randomUUID(),
                    name = name, address = address, city = city,
                    state = state, zip = zip, phone = phone, email = email
                )
                if (farmer.id > 0) {
                    farmerViewModel.updateFarmer(farmer)
                    onNext(farmer.id)
                } else {
                    farmerViewModel.addFarmer(farmer)
                    waitingForSave = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Next: Add Your Farm →", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Step 2: Farm Information
@Composable
fun FarmStep(
    farmViewModel: FarmViewModel,
    farmerId: Long,
    existingFarm: Farm?,
    onBack: () -> Unit,
    onNext: (Long) -> Unit
) {
    val context = LocalContext.current
    val farms by farmViewModel.farms.collectAsStateWithLifecycle(initialValue = null)

    var farmName by remember { mutableStateOf(existingFarm?.name ?: "") }
    var siteId by remember { mutableStateOf(existingFarm?.siteId ?: "") }

    LaunchedEffect(existingFarm) {
        existingFarm?.let { farmName = it.name; siteId = it.siteId }
    }

    var waitingForSave by remember { mutableStateOf(false) }
    LaunchedEffect(farms, waitingForSave) {
        val f = farms
        if (waitingForSave && f != null && f.isNotEmpty()) {
            waitingForSave = false
            onNext(f.first().id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Where is your farm?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Give your farm a name and optional site ID.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = farmName, onValueChange = { farmName = it }, label = { Text("Farm Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = siteId, onValueChange = { siteId = it }, label = { Text("Site ID (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← Back")
            }
            Button(
                onClick = {
                    if (farmName.isBlank()) {
                        Toast.makeText(context, "Please enter a farm name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val farm = Farm(
                        id = existingFarm?.id ?: 0L,
                        persistentId = existingFarm?.persistentId ?: TemporalUtils.randomUUID(),
                        farmerId = farmerId,
                        name = farmName,
                        siteId = siteId,
                        validFrom = existingFarm?.validFrom ?: TemporalUtils.now(),
                        validTo = existingFarm?.validTo
                    )
                    if (farm.id > 0) {
                        farmViewModel.updateFarm(farm)
                        onNext(farm.id)
                    } else {
                        farmViewModel.addFarm(farm)
                        waitingForSave = true
                    }
                },
                modifier = Modifier.weight(2f).height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Next: Add Orchard →", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Step 3: Orchard Information
@Composable
fun OrchardStep(
    orchardViewModel: OrchardViewModel,
    farmId: Long,
    existingOrchard: Orchard?,
    fragment: Fragment,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val orchards by orchardViewModel.allOrchards.collectAsStateWithLifecycle(initialValue = null)

    var crop by remember { mutableStateOf(existingOrchard?.crop ?: "") }
    var plantedDate by remember { mutableStateOf("") }
    var rowWidth by remember { mutableStateOf(existingOrchard?.rowWidth?.toString() ?: "") }
    var rowWidthUnit by remember { mutableStateOf(existingOrchard?.rowWidthLinearUnit ?: LinearUnit.FEET) }
    var distanceBetweenTrees by remember { mutableStateOf(existingOrchard?.distanceBetweenTrees?.toString() ?: "") }
    var distanceUnit by remember { mutableStateOf(existingOrchard?.distanceBetweenTreesLinearUnit ?: LinearUnit.FEET) }

    LaunchedEffect(existingOrchard) {
        existingOrchard?.let {
            crop = it.crop
            // Convert YYYY-MM-DD to MM-DD-YYYY for UI
            val parts = it.plantedDate.split("-")
            plantedDate = if (parts.size == 3 && parts[0].length == 4) {
                "${parts[1]}-${parts[2]}-${parts[0]}"
            } else {
                it.plantedDate
            }
            rowWidth = it.rowWidth.toString(); rowWidthUnit = it.rowWidthLinearUnit
            distanceBetweenTrees = it.distanceBetweenTrees.toString(); distanceUnit = it.distanceBetweenTreesLinearUnit
        }
    }

    // Date picker result
    DisposableEffect(fragment) {
        val listener = androidx.fragment.app.FragmentResultListener { _, bundle ->
            plantedDate = bundle.getString("wizardPlantedDateKey") ?: ""
        }
        fragment.childFragmentManager.setFragmentResultListener("wizardPlantedDateRequestKey", fragment, listener)
        onDispose {
            fragment.childFragmentManager.clearFragmentResultListener("wizardPlantedDateRequestKey")
        }
    }

    var waitingForSave by remember { mutableStateOf(false) }
    LaunchedEffect(orchards, waitingForSave) {
        val o = orchards
        if (waitingForSave && o != null && o.isNotEmpty()) {
            waitingForSave = false
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("What are you growing?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Tell us about your first orchard or crop. You can add soil details later.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Card(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = crop, onValueChange = { crop = it }, label = { Text("Crop *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(
                    value = plantedDate,
                    onValueChange = { },
                    label = { Text("Planted Date *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            DatePickerFragment("wizardPlantedDateRequestKey", "wizardPlantedDateKey")
                                .show(fragment.childFragmentManager, "plantedDate")
                        }) {
                            Icon(painterResource(id = R.drawable.ic_baseline_calendar_month_24), contentDescription = "Pick date")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Spacing (optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rowWidth,
                        onValueChange = { rowWidth = it },
                        label = { Text("Row Width") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    UnitSelector(selected = rowWidthUnit, onSelect = { rowWidthUnit = it }, modifier = Modifier.weight(1f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = distanceBetweenTrees,
                        onValueChange = { distanceBetweenTrees = it },
                        label = { Text("Tree Spacing") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    UnitSelector(selected = distanceUnit, onSelect = { distanceUnit = it }, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← Back")
            }
            Button(
                onClick = {
                    if (crop.isBlank()) {
                        Toast.makeText(context, "Please enter a crop name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val isoDate = try {
                        val parts = plantedDate.split("-")
                        if (parts.size == 3 && parts[2].length == 4) {
                            // Convert MM-DD-YYYY to YYYY-MM-DD
                            "${parts[2]}-${parts[0]}-${parts[1]}"
                        } else if (parts.size == 3 && parts[0].length == 4) {
                            // Already YYYY-MM-DD
                            plantedDate
                        } else {
                            throw Exception("Invalid format")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Please select a valid planted date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val orchard = Orchard(
                        id = existingOrchard?.id ?: 0L,
                        persistentId = existingOrchard?.persistentId ?: TemporalUtils.randomUUID(),
                        farmId = farmId,
                        crop = crop,
                        plantedDate = isoDate,
                        rowWidth = rowWidth.toDoubleOrNull() ?: 0.0,
                        rowWidthLinearUnit = rowWidthUnit,
                        distanceBetweenTrees = distanceBetweenTrees.toDoubleOrNull() ?: 0.0,
                        distanceBetweenTreesLinearUnit = distanceUnit,
                        sand = 0.0, silt = 0.0, clay = 0.0, organicMatter = 0.0,
                        validFrom = existingOrchard?.validFrom ?: TemporalUtils.now(),
                        validTo = existingOrchard?.validTo
                    )
                    if (orchard.id > 0) {
                        orchardViewModel.updateOrchard(orchard)
                        onComplete()
                    } else {
                        orchardViewModel.addOrchard(orchard)
                        waitingForSave = true
                    }
                },
                modifier = Modifier.weight(2f).height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UnitSelector(selected: LinearUnit, onSelect: (LinearUnit) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected.unit,
            onValueChange = { },
            readOnly = true,
            label = { Text("Unit") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select unit")
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LinearUnit.entries.forEach { unit ->
                DropdownMenuItem(onClick = { onSelect(unit); expanded = false }) {
                    Text(unit.unit)
                }
            }
        }
    }
}
