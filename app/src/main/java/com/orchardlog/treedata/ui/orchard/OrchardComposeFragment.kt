package com.orchardlog.treedata.ui.orchard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.Farm
import com.orchardlog.treedata.shared.model.LinearUnit
import com.orchardlog.treedata.shared.model.Orchard
import com.orchardlog.treedata.shared.model.OrchardActivity
import com.orchardlog.treedata.shared.model.OrchardWithFarm
import com.orchardlog.treedata.shared.viewmodels.FarmViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.shared.TemporalUtils
import com.orchardlog.treedata.utils.DatePickerFragment
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class OrchardComposeFragment : Fragment() {

    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel
    private val farmViewModel: FarmViewModel = ViewModelProvider.farmViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    OrchardScreen(orchardViewModel, farmViewModel, this@OrchardComposeFragment)
                }
            }
        }
    }
}

@Composable
fun OrchardScreen(orchardViewModel: OrchardViewModel, farmViewModel: FarmViewModel, fragment: Fragment) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Orchards") })
                TabRow(
                    selectedTabIndex = selectedTab,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.primary
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Orchards", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Activities", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                OrchardForm(orchardViewModel, farmViewModel, fragment)
            } else {
                OrchardActivityForm(orchardViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrchardForm(orchardViewModel: OrchardViewModel, farmViewModel: FarmViewModel, fragment: Fragment) {
    val orchards by orchardViewModel.orchardsWithFarm.collectAsStateWithLifecycle(initialValue = emptyList())
    val farms by farmViewModel.farms.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedOrchardOWF by remember { mutableStateOf<OrchardWithFarm?>(null) }

    // Form fields
    var selectedFarmId by remember { mutableLongStateOf(0L) }
    var crop by remember { mutableStateOf("") }
    
    // UI state for plantedDate (MM-DD-YYYY)
    var plantedDate by remember { mutableStateOf("") }
    
    var rowWidth by remember { mutableStateOf("") }
    var rowWidthUnit by remember { mutableStateOf(LinearUnit.FEET) }
    var distanceBetweenTrees by remember { mutableStateOf("") }
    var distanceBetweenTreesUnit by remember { mutableStateOf(LinearUnit.FEET) }
    var sand by remember { mutableStateOf("") }
    var silt by remember { mutableStateOf("") }
    var clay by remember { mutableStateOf("") }
    var organicMatter by remember { mutableStateOf("") }

    val context = LocalContext.current

    fun resetForm() {
        selectedOrchardOWF = null
        selectedFarmId = farms?.firstOrNull()?.id ?: 0L
        crop = ""
        plantedDate = ""
        rowWidth = ""
        distanceBetweenTrees = ""
        sand = ""
        silt = ""
        clay = ""
        organicMatter = ""
        rowWidthUnit = LinearUnit.FEET
        distanceBetweenTreesUnit = LinearUnit.FEET
    }

    fun populateFields(owf: OrchardWithFarm?) {
        if (owf != null) {
            val o = owf.orchard
            selectedFarmId = o.farmId
            crop = o.crop
            // Convert YYYY-MM-DD to MM-DD-YYYY for UI
            val parts = o.plantedDate.split("-")
            plantedDate = if (parts.size == 3 && parts[0].length == 4) {
                "${parts[1]}-${parts[2]}-${parts[0]}"
            } else {
                o.plantedDate
            }
            rowWidth = o.rowWidth.toString()
            rowWidthUnit = o.rowWidthLinearUnit
            distanceBetweenTrees = o.distanceBetweenTrees.toString()
            distanceBetweenTreesUnit = o.distanceBetweenTreesLinearUnit
            sand = o.sand.toString()
            silt = o.silt.toString()
            clay = o.clay.toString()
            organicMatter = o.organicMatter.toString()
        } else {
            resetForm()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Existing Orchards", style = MaterialTheme.typography.caption)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOrchardOWF?.orchard?.crop ?: "New Orchard",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select an Orchard") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        selectedOrchardOWF = null
                        populateFields(null)
                        expanded = false
                    }) {
                        Text("New Orchard")
                    }
                    orchards.forEach { owf ->
                        DropdownMenuItem(onClick = {
                            selectedOrchardOWF = owf
                            populateFields(owf)
                            expanded = false
                        }) {
                            Text("${owf.orchard.crop} (${owf.farm.name})")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Orchard Details", style = MaterialTheme.typography.caption)
            var farmExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = farmExpanded,
                onExpandedChange = { farmExpanded = !farmExpanded }
            ) {
                OutlinedTextField(
                    value = farms?.find { it.id == selectedFarmId }?.name ?: "Select Farm",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Farm") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = farmExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = farmExpanded,
                    onDismissRequest = { farmExpanded = false }
                ) {
                    farms?.forEach { farm ->
                        DropdownMenuItem(onClick = {
                            selectedFarmId = farm.id
                            farmExpanded = false
                        }) {
                            Text(farm.name)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = crop,
                onValueChange = { crop = it },
                label = { Text("Crop") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = plantedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Planted Date") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerFragment("orchardPlantedDateRequestKey", "orchardPlantedDateKey")
                            .show(fragment.childFragmentManager, "plantedDate")
                    }) {
                        Icon(painterResource(id = R.drawable.ic_baseline_calendar_month_24), contentDescription = "Pick date")
                    }
                }
            )

            // Date picker result listener
            DisposableEffect(fragment) {
                val listener = androidx.fragment.app.FragmentResultListener { _, bundle ->
                    plantedDate = bundle.getString("orchardPlantedDateKey") ?: ""
                }
                fragment.childFragmentManager.setFragmentResultListener("orchardPlantedDateRequestKey", fragment, listener)
                onDispose {
                    fragment.childFragmentManager.clearFragmentResultListener("orchardPlantedDateRequestKey")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Row Spacing", style = MaterialTheme.typography.caption)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = rowWidth,
                    onValueChange = { rowWidth = it },
                    label = { Text("Row Width") },
                    modifier = Modifier.weight(1f)
                )
                UnitPicker(rowWidthUnit) { rowWidthUnit = it }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = distanceBetweenTrees,
                    onValueChange = { distanceBetweenTrees = it },
                    label = { Text("Distance Between Trees") },
                    modifier = Modifier.weight(1f)
                )
                UnitPicker(distanceBetweenTreesUnit) { distanceBetweenTreesUnit = it }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Soil Composition (%)", style = MaterialTheme.typography.caption)
            OutlinedTextField(value = sand, onValueChange = { sand = it }, label = { Text("Sand") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = silt, onValueChange = { silt = it }, label = { Text("Silt") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            OutlinedTextField(value = clay, onValueChange = { clay = it }, label = { Text("Clay") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = organicMatter, onValueChange = { organicMatter = it }, label = { Text("Organic Matter") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
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
                            id = selectedOrchardOWF?.orchard?.id ?: 0L,
                            persistentId = selectedOrchardOWF?.orchard?.persistentId ?: TemporalUtils.randomUUID(),
                            farmId = selectedFarmId,
                            crop = crop,
                            plantedDate = isoDate,
                            rowWidth = rowWidth.toDoubleOrNull() ?: 0.0,
                            rowWidthLinearUnit = rowWidthUnit,
                            distanceBetweenTrees = distanceBetweenTrees.toDoubleOrNull() ?: 0.0,
                            distanceBetweenTreesLinearUnit = distanceBetweenTreesUnit,
                            sand = sand.toDoubleOrNull() ?: 0.0,
                            silt = silt.toDoubleOrNull() ?: 0.0,
                            clay = clay.toDoubleOrNull() ?: 0.0,
                            organicMatter = organicMatter.toDoubleOrNull() ?: 0.0,
                            validFrom = selectedOrchardOWF?.orchard?.validFrom ?: TemporalUtils.now(),
                            validTo = selectedOrchardOWF?.orchard?.validTo
                        )
                        if (orchard.id > 0) orchardViewModel.updateOrchard(orchard)
                        else orchardViewModel.addOrchard(orchard)
                        resetForm()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White)
                ) {
                    Text("Save")
                }
                OutlinedButton(onClick = { resetForm() }) {
                    Text("New")
                }
                Button(
                    onClick = {
                        selectedOrchardOWF?.orchard?.let { orchardViewModel.deleteOrchard(it) }
                        resetForm()
                    },
                    enabled = selectedOrchardOWF != null,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnitPicker(selectedUnit: LinearUnit, onUnitSelected: (LinearUnit) -> Unit) {
    val units = listOf(LinearUnit.FEET, LinearUnit.INCHES, LinearUnit.METERS)
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.width(120.dp).padding(start = 8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedUnit.unit,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }) {
                        Text(unit.unit)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrchardActivityForm(orchardViewModel: OrchardViewModel) {
    val activities by orchardViewModel.orchardActivities.collectAsStateWithLifecycle(initialValue = emptyList())
    val orchards by orchardViewModel.orchardsWithFarm.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedActivity by remember { mutableStateOf<OrchardActivity?>(null) }

    // Form fields
    var selectedOrchardId by remember { mutableLongStateOf(0L) }
    var activity by remember { mutableStateOf("Mowing") }
    var notes by remember { mutableStateOf("") }
    var activityStart by remember { mutableStateOf<Instant>(Instant.fromEpochMilliseconds(System.currentTimeMillis())) }
    var activityStop by remember { mutableStateOf<Instant>(Instant.fromEpochMilliseconds(System.currentTimeMillis())) }

    val context = LocalContext.current
    val activityTypes = listOf("Mowing", "Pruning", "Discing", "Harvesting", "Equipment Maintenance", "Soil Moisture", "Weather Event")

    fun resetForm() {
        selectedActivity = null
        activity = "Mowing"
        notes = ""
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        activityStart = now
        activityStop = now
    }

    fun populateFields(a: OrchardActivity?) {
        if (a != null) {
            selectedOrchardId = a.orchardId
            activity = a.activity
            notes = a.notes
            activityStart = a.activityStart
            activityStop = a.activityStop
        } else {
            resetForm()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Existing Activities", style = MaterialTheme.typography.caption)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedActivity?.let { "${it.activity} - ${it.activityStart.toLocalDateTime(
                        TimeZone.currentSystemDefault())}" } ?: "New Activity",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select an Activity") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { populateFields(null); expanded = false }) { Text("New Activity") }
                    activities.forEach { act ->
                        DropdownMenuItem(onClick = { selectedActivity = act; populateFields(act); expanded = false }) {
                            Text("${act.activity} - ${act.activityStart.toLocalDateTime(TimeZone.currentSystemDefault())}")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Activity Details", style = MaterialTheme.typography.caption)
            var orchardExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = orchardExpanded, onExpandedChange = { orchardExpanded = !orchardExpanded }) {
                OutlinedTextField(
                    value = orchards.find { it.orchard.id == selectedOrchardId }?.orchard?.crop ?: "Select Orchard",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Orchard") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orchardExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = orchardExpanded, onDismissRequest = { orchardExpanded = false }) {
                    orchards.forEach { owf ->
                        DropdownMenuItem(onClick = { selectedOrchardId = owf.orchard.id; orchardExpanded = false }) {
                            Text("${owf.orchard.crop} (${owf.farm.name})")
                        }
                    }
                }
            }

            var activityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = activityExpanded, onExpandedChange = { activityExpanded = !activityExpanded }) {
                OutlinedTextField(
                    value = activity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                ExposedDropdownMenu(expanded = activityExpanded, onDismissRequest = { activityExpanded = false }) {
                    activityTypes.forEach { type ->
                        DropdownMenuItem(onClick = { activity = type; activityExpanded = false }) { Text(type) }
                    }
                }
            }

            OutlinedButton(onClick = {
                showDateTimePicker(context, activityStart) { activityStart = it }
            }, modifier = Modifier.fillMaxWidth()) {
                val dateTime = activityStart.toLocalDateTime(TimeZone.currentSystemDefault())
                Text("Start: $dateTime")
            }
            OutlinedButton(onClick = {
                showDateTimePicker(context, activityStop) { activityStop = it }
            }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                val dateTime = activityStop.toLocalDateTime(TimeZone.currentSystemDefault())
                Text("Stop: $dateTime")
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        val orchardActivity = OrchardActivity(
                            id = selectedActivity?.id ?: 0L,
                            orchardId = selectedOrchardId,
                            activity = activity,
                            notes = notes,
                            activityStart = activityStart,
                            activityStop = activityStop,
                            firestoreId = selectedActivity?.firestoreId ?: ""
                        )
                        if (orchardActivity.id > 0L) orchardViewModel.updateOrchardActivity(orchardActivity)
                        else orchardViewModel.addOrchardActivity(orchardActivity)
                        resetForm()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White)
                ) { Text("Save") }
                OutlinedButton(onClick = { resetForm() }) { Text("New") }
                Button(
                    onClick = {
                        selectedActivity?.let { orchardViewModel.deleteOrchardActivity(it) }
                        resetForm()
                    },
                    enabled = selectedActivity != null,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                ) { Text("Delete") }
            }
        }
    }
}

private fun showDateTimePicker(context: android.content.Context, initial: Instant, onSelected: (Instant) -> Unit) {
    val dateTime = initial.toLocalDateTime(TimeZone.currentSystemDefault())
    DatePickerDialog(context, { _, year, month, day ->
        TimePickerDialog(context, { _, hour, minute ->
            val newDateTime = LocalDateTime(year, month + 1, day, hour, minute)
            onSelected(newDateTime.toInstant(TimeZone.currentSystemDefault()))
        }, dateTime.hour, dateTime.minute, false).show()
    }, dateTime.year, dateTime.monthNumber - 1, dateTime.dayOfMonth).show()
}
