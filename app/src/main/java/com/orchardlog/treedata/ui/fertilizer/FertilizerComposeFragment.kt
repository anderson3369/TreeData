package com.orchardlog.treedata.ui.fertilizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.*
import com.orchardlog.treedata.shared.viewmodels.FertilizerViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.TimePickerFragment
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant as KtInstant

class FertilizerComposeFragment : Fragment() {

    private val fertilizerViewModel: FertilizerViewModel = ViewModelProvider.fertilizerViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    FertilizerTabbedScreen(
                        fertilizerViewModel,
                        orchardViewModel,
                        fragment = this@FertilizerComposeFragment
                    )
                }
            }
        }
    }

    companion object {
        const val STARTDATE_REQ = "fertilizerStartDateKey"
        const val STOPDATE_REQ = "fertilizerStopDateKey"
        const val STARTTIME_REQ = "fertilizerStartTimeKey"
        const val STOPTIME_REQ = "fertilizerStopTimeKey"
        const val DATE_KEY = "fertilizerDate"
        const val TIME_KEY = "fertilizerTime"
    }
}

@Composable
fun FertilizerTabbedScreen(
    fertilizerViewModel: FertilizerViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Fertilizers", "Applications", "Report")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.fertilizers),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.White,
            contentColor = MaterialTheme.colors.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> FertilizerFormTab(fertilizerViewModel)
            1 -> FertilizerApplicationFormTab(fertilizerViewModel, orchardViewModel, fragment)
            2 -> FertilizerReportScreen(fertilizerViewModel, orchardViewModel, fragment)
        }
    }
}

// MARK: - Tab 0: Fertilizer Form

@Composable
fun FertilizerFormTab(viewModel: FertilizerViewModel) {
    val fertilizers by viewModel.fertilizers.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedFertilizer by remember { mutableStateOf<Fertilizer?>(null) }
    var name by remember { mutableStateOf("") }
    var nitrogen by remember { mutableStateOf("") }
    var phosphorous by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var sulfur by remember { mutableStateOf("") }
    var calcium by remember { mutableStateOf("") }
    var magnesium by remember { mutableStateOf("") }
    var iron by remember { mutableStateOf("") }
    var zinc by remember { mutableStateOf("") }
    var manganese by remember { mutableStateOf("") }
    var boron by remember { mutableStateOf("") }
    var molybdenum by remember { mutableStateOf("") }
    var chloride by remember { mutableStateOf("") }
    var copper by remember { mutableStateOf("") }
    var selenium by remember { mutableStateOf("") }
    var nickel by remember { mutableStateOf("") }
    var organicMatter by remember { mutableStateOf("") }

    fun resetForm() {
        selectedFertilizer = null
        name = ""; nitrogen = ""; phosphorous = ""; potassium = ""
        sulfur = ""; calcium = ""; magnesium = ""; iron = ""
        zinc = ""; manganese = ""; boron = ""; molybdenum = ""
        chloride = ""; copper = ""; selenium = ""; nickel = ""
        organicMatter = ""
    }

    LaunchedEffect(selectedFertilizer) {
        selectedFertilizer?.let {
            name = it.name
            nitrogen = it.nitrogen.toString()
            phosphorous = it.phosphorous.toString()
            potassium = it.potassium.toString()
            sulfur = it.sulfur.toString()
            calcium = it.calcium.toString()
            magnesium = it.magnesium.toString()
            iron = it.iron.toString()
            zinc = it.zinc.toString()
            manganese = it.manganese.toString()
            boron = it.boron.toString()
            molybdenum = it.molybdenum.toString()
            chloride = it.chloride.toString()
            copper = it.copper.toString()
            selenium = it.selenium.toString()
            nickel = it.nickel.toString()
            organicMatter = it.organicMatter.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Existing Fertilizers
        FieldGroup(title = "EXISTING FERTILIZERS") {
            var expanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Fertilizer",
                    value = selectedFertilizer?.name ?: "New Fertilizer",
                    onClick = { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { resetForm(); expanded = false }) {
                        Text("New Fertilizer")
                    }
                    fertilizers.forEach { f ->
                        DropdownMenuItem(onClick = { selectedFertilizer = f; expanded = false }) {
                            Text(f.name)
                        }
                    }
                }
            }
        }

        // Fertilizer Details
        FieldGroup(title = "FERTILIZER DETAILS") {
            TransparentInputField("Product Name", name) { name = it }
        }

        // Primary Nutrients
        FieldGroup(title = "PRIMARY NUTRIENTS (%)") {
            TransparentInputField("Nitrogen (N)", nitrogen, isNumber = true) { nitrogen = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Phosphorous (P)", phosphorous, isNumber = true) { phosphorous = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Potassium (K)", potassium, isNumber = true) { potassium = it }
        }

        // Secondary Nutrients
        FieldGroup(title = "SECONDARY NUTRIENTS (%)") {
            TransparentInputField("Sulfur (S)", sulfur, isNumber = true) { sulfur = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Calcium (Ca)", calcium, isNumber = true) { calcium = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Magnesium (Mg)", magnesium, isNumber = true) { magnesium = it }
        }

        // Micronutrients
        FieldGroup(title = "MICRONUTRIENTS (%)") {
            TransparentInputField("Iron (Fe)", iron, isNumber = true) { iron = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Zinc (Zn)", zinc, isNumber = true) { zinc = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Manganese (Mn)", manganese, isNumber = true) { manganese = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Boron (B)", boron, isNumber = true) { boron = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Molybdenum (Mo)", molybdenum, isNumber = true) { molybdenum = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Chloride (Cl)", chloride, isNumber = true) { chloride = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Copper (Cu)", copper, isNumber = true) { copper = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Selenium (Se)", selenium, isNumber = true) { selenium = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("Nickel (Ni)", nickel, isNumber = true) { nickel = it }
        }

        // Other
        FieldGroup(title = "OTHER") {
            TransparentInputField("Organic Matter (%)", organicMatter, isNumber = true) { organicMatter = it }
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val f = Fertilizer(
                        id = selectedFertilizer?.id ?: 0L,
                        name = name,
                        nitrogen = nitrogen.toDoubleOrNull() ?: 0.0,
                        phosphorous = phosphorous.toDoubleOrNull() ?: 0.0,
                        potassium = potassium.toDoubleOrNull() ?: 0.0,
                        sulfur = sulfur.toDoubleOrNull() ?: 0.0,
                        calcium = calcium.toDoubleOrNull() ?: 0.0,
                        magnesium = magnesium.toDoubleOrNull() ?: 0.0,
                        iron = iron.toDoubleOrNull() ?: 0.0,
                        zinc = zinc.toDoubleOrNull() ?: 0.0,
                        manganese = manganese.toDoubleOrNull() ?: 0.0,
                        boron = boron.toDoubleOrNull() ?: 0.0,
                        molybdenum = molybdenum.toDoubleOrNull() ?: 0.0,
                        chloride = chloride.toDoubleOrNull() ?: 0.0,
                        copper = copper.toDoubleOrNull() ?: 0.0,
                        selenium = selenium.toDoubleOrNull() ?: 0.0,
                        nickel = nickel.toDoubleOrNull() ?: 0.0,
                        organicMatter = organicMatter.toDoubleOrNull() ?: 0.0
                    )
                    if (f.id > 0) viewModel.updateFertilizer(f) else viewModel.addFertilizer(f)
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                    resetForm()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save_button_text), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { resetForm() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
            ) {
                Text(stringResource(R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedFertilizer?.let {
                        viewModel.deleteFertilizer(it)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedFertilizer != null
            ) {
                Text(stringResource(R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// MARK: - Tab 1: Fertilizer Application Form

@Composable
fun FertilizerApplicationFormTab(
    fertilizerViewModel: FertilizerViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    val applications by fertilizerViewModel.fertilizerApplicationsWithItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val fertilizers by fertilizerViewModel.fertilizers.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedAppWithItems by remember { mutableStateOf<FertilizerApplicationWithItems?>(null) }
    var currentItems by remember { mutableStateOf<List<FertilizerApplicationItem>>(emptyList()) }

    // Application fields
    var selectedOrchardId by remember { mutableStateOf(0L) }
    val initialNow = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(initialNow.date) }
    var startTime by remember { mutableStateOf(initialNow.time) }
    var stopDate by remember { mutableStateOf(initialNow.date) }
    var stopTime by remember { mutableStateOf(initialNow.time) }
    var areaTreated by remember { mutableStateOf("") }
    var areaUnit by remember { mutableStateOf(OrchardUnit.ACRE) }

    // Item fields
    var selectedFertilizerId by remember { mutableStateOf(0L) }
    var itemApplied by remember { mutableStateOf("") }
    var itemAppliedUnit by remember { mutableStateOf(WeightOrMeasureUnit.POUNDS) }

    fun resetForm() {
        selectedAppWithItems = null
        currentItems = emptyList()
        selectedOrchardId = 0L
        areaTreated = ""
        areaUnit = OrchardUnit.ACRE
        itemApplied = ""
        val now = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
        startDate = now.date
        startTime = now.time
        stopDate = now.date
        stopTime = now.time
    }

    // Update form when a saved application is selected
    LaunchedEffect(selectedAppWithItems) {
        selectedAppWithItems?.let { appWithItems ->
            val app = appWithItems.application
            selectedOrchardId = app.orchardId
            val startLocal = app.applicationStart.toLocalDateTime(TimeZone.currentSystemDefault())
            startDate = startLocal.date
            startTime = startLocal.time
            val stopLocal = app.applicationStop.toLocalDateTime(TimeZone.currentSystemDefault())
            stopDate = stopLocal.date
            stopTime = stopLocal.time
            areaTreated = app.areaTreated.toString()
            areaUnit = app.orchardUnit
            currentItems = appWithItems.items
        }
    }

    // Handle Picker Results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(FertilizerComposeFragment.STARTDATE_REQ, fragment) { _, bundle ->
            bundle.getString(FertilizerComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) startDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(FertilizerComposeFragment.STOPDATE_REQ, fragment) { _, bundle ->
            bundle.getString(FertilizerComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) stopDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(FertilizerComposeFragment.STARTTIME_REQ, fragment) { _, bundle ->
            bundle.getString(FertilizerComposeFragment.TIME_KEY)?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) startTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(FertilizerComposeFragment.STOPTIME_REQ, fragment) { _, bundle ->
            bundle.getString(FertilizerComposeFragment.TIME_KEY)?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) stopTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Existing Applications
        FieldGroup(title = "EXISTING APPLICATIONS") {
            var appExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Application",
                    value = selectedAppWithItems?.application?.description() ?: "New Application",
                    onClick = { appExpanded = true }
                )
                DropdownMenu(expanded = appExpanded, onDismissRequest = { appExpanded = false }) {
                    DropdownMenuItem(onClick = { resetForm(); appExpanded = false }) {
                        Text("New Application")
                    }
                    applications.forEach { app ->
                        DropdownMenuItem(onClick = { selectedAppWithItems = app; appExpanded = false }) {
                            val orchardName = farmOrchardsMap?.get(app.application.orchardId) ?: "Unknown"
                            Text("${app.application.description()} - $orchardName")
                        }
                    }
                }
            }
        }

        // Application Details
        FieldGroup(title = "APPLICATION DETAILS") {
            // Orchard
            var orchardExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Orchard",
                    value = farmOrchardsMap?.get(selectedOrchardId) ?: "Select",
                    onClick = { orchardExpanded = true }
                )
                DropdownMenu(expanded = orchardExpanded, onDismissRequest = { orchardExpanded = false }) {
                    farmOrchardsMap?.forEach { (id, name) ->
                        DropdownMenuItem(onClick = { selectedOrchardId = id; orchardExpanded = false }) { Text(name) }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Start Date & Time
            DateTimeRow(
                label = "Start",
                dateValue = dateToShortString(startDate),
                timeValue = timeToShortString(startTime),
                onDateClick = { DatePickerFragment(FertilizerComposeFragment.STARTDATE_REQ, FertilizerComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Start Date") },
                onTimeClick = { TimePickerFragment(FertilizerComposeFragment.STARTTIME_REQ, FertilizerComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Start Time") }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Stop Date & Time
            DateTimeRow(
                label = "Stop",
                dateValue = dateToShortString(stopDate),
                timeValue = timeToShortString(stopTime),
                onDateClick = { DatePickerFragment(FertilizerComposeFragment.STOPDATE_REQ, FertilizerComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Stop Date") },
                onTimeClick = { TimePickerFragment(FertilizerComposeFragment.STOPTIME_REQ, FertilizerComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Stop Time") }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Area Treated
            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Area Treated", areaTreated, isNumber = true, modifier = Modifier.weight(1f)) { areaTreated = it }
                var areaUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = areaUnit.toString(), onClick = { areaUnitExpanded = true })
                    DropdownMenu(expanded = areaUnitExpanded, onDismissRequest = { areaUnitExpanded = false }) {
                        OrchardUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { areaUnit = unit; areaUnitExpanded = false }) { Text(unit.toString()) }
                        }
                    }
                }
            }
        }

        // Fertilizers in this Application
        FieldGroup(title = "FERTILIZERS IN THIS APPLICATION") {
            currentItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            fertilizers.find { it.id == item.fertilizerId }?.name ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${String.format("%.2f", item.applied)} ${item.appliedUnit}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        currentItems = currentItems.filter { it.fertilizerId != item.fertilizerId }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colors.error)
                    }
                }
                if (index < currentItems.size - 1) Divider(modifier = Modifier.padding(start = 16.dp))
            }

            if (currentItems.isEmpty()) {
                Text(
                    "No fertilizers added.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Divider()

            // Add fertilizer item
            var fertExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Add Fertilizer",
                    value = fertilizers.find { it.id == selectedFertilizerId }?.name ?: "Select",
                    onClick = { fertExpanded = true }
                )
                DropdownMenu(expanded = fertExpanded, onDismissRequest = { fertExpanded = false }) {
                    fertilizers.forEach { fert ->
                        DropdownMenuItem(onClick = { selectedFertilizerId = fert.id; fertExpanded = false }) {
                            Text(fert.name)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Amount", itemApplied, isNumber = true, modifier = Modifier.weight(1f)) { itemApplied = it }
                var unitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = itemAppliedUnit.toString(), onClick = { unitExpanded = true })
                    DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        WeightOrMeasureUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { itemAppliedUnit = unit; unitExpanded = false }) { Text(unit.toString()) }
                        }
                    }
                }
                IconButton(
                    onClick = {
                        val amount = itemApplied.toDoubleOrNull() ?: return@IconButton
                        if (selectedFertilizerId == 0L) return@IconButton
                        val newItem = FertilizerApplicationItem(
                            fertilizerApplicationId = selectedAppWithItems?.application?.id ?: 0L,
                            fertilizerId = selectedFertilizerId,
                            applied = amount,
                            appliedUnit = itemAppliedUnit
                        )
                        val existing = currentItems.indexOfFirst { it.fertilizerId == selectedFertilizerId }
                        currentItems = if (existing >= 0) {
                            currentItems.toMutableList().also { it[existing] = newItem }
                        } else {
                            currentItems + newItem
                        }
                        itemApplied = ""
                    },
                    enabled = itemApplied.isNotEmpty() && selectedFertilizerId != 0L
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colors.primary, modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (selectedOrchardId == 0L || currentItems.isEmpty()) {
                        Toast.makeText(context, "Please select an orchard and add fertilizers", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val startInstant = KtInstant.fromEpochMilliseconds(LocalDateTime(startDate, startTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                    val stopInstant = KtInstant.fromEpochMilliseconds(LocalDateTime(stopDate, stopTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                    val app = FertilizerApplication(
                        id = selectedAppWithItems?.application?.id ?: 0L,
                        orchardId = selectedOrchardId,
                        applicationStart = startInstant,
                        applicationStop = stopInstant,
                        areaTreated = areaTreated.toDoubleOrNull() ?: 0.0,
                        orchardUnit = areaUnit
                    )
                    if (app.id > 0L) {
                        fertilizerViewModel.updateFertilizerApplicationWithItems(app, currentItems)
                    } else {
                        fertilizerViewModel.saveFertilizerApplicationWithItems(app, currentItems)
                    }
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                    resetForm()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = currentItems.isNotEmpty()
            ) {
                Text(stringResource(R.string.save_button_text), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { resetForm() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
            ) {
                Text(stringResource(R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedAppWithItems?.let {
                        fertilizerViewModel.deleteFertilizerApplicationWithItems(it.application)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedAppWithItems != null
            ) {
                Text(stringResource(R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

