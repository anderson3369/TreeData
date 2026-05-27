package com.orchardlog.treedata.ui.irrigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.fragment.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.*
import com.orchardlog.treedata.shared.viewmodels.IrrigationViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.PumpViewModel
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
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant as KtInstant

class IrrigationComposeFragment : Fragment() {

    private val irrigationViewModel: IrrigationViewModel = ViewModelProvider.irrigationViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel
    private val pumpViewModel: PumpViewModel = ViewModelProvider.pumpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    IrrigationTabbedScreen(
                        irrigationViewModel = irrigationViewModel,
                        orchardViewModel = orchardViewModel,
                        pumpViewModel = pumpViewModel,
                        fragment = this@IrrigationComposeFragment,
                        onNavigateToAddPump = {
                            findNavController().navigate(IrrigationComposeFragmentDirections.actionNavIrrigationToNavPump())
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val STARTDATE_REQ = "irrigationStartDateKey"
        const val STOPDATE_REQ = "irrigationStopDateKey"
        const val STARTTIME_REQ = "irrigationStartTimeKey"
        const val STOPTIME_REQ = "irrigationStopTimeKey"
        const val DATE_KEY = "dateKey"
        const val TIME_KEY = "timeKey"
    }
}

@Composable
fun IrrigationTabbedScreen(
    irrigationViewModel: IrrigationViewModel,
    orchardViewModel: OrchardViewModel,
    pumpViewModel: PumpViewModel,
    fragment: Fragment,
    onNavigateToAddPump: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Systems", "Irrigations", "Report")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.irrigation),
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
            0 -> IrrigationSystemFormTab(
                irrigationViewModel, orchardViewModel, pumpViewModel, onNavigateToAddPump
            )
            1 -> IrrigationEventFormTab(irrigationViewModel, fragment)
            2 -> IrrigationReportScreen(irrigationViewModel, orchardViewModel, fragment)
        }
    }
}

// MARK: - Tab 0: Irrigation System Form

@Composable
fun IrrigationSystemFormTab(
    irrigationViewModel: IrrigationViewModel,
    orchardViewModel: OrchardViewModel,
    pumpViewModel: PumpViewModel,
    onNavigateToAddPump: () -> Unit
) {
    val irrigationSystems by irrigationViewModel.irrigationSystems.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val pumpsMap by pumpViewModel.pumpsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val context = LocalContext.current

    var selectedSystem by remember { mutableStateOf<IrrigationSystem?>(null) }
    var systemName by remember { mutableStateOf("") }
    var selectedOrchardId by remember { mutableStateOf(0L) }
    var selectedPumpId by remember { mutableStateOf(0L) }
    var selectedMethod by remember { mutableStateOf(IrrigationMethod.DRIP) }
    var emitterFlowRate by remember { mutableStateOf("") }
    var selectedFlowUnit by remember { mutableStateOf(FlowRateUnit.GALLONSPERHOUR) }
    var emitterRadius by remember { mutableStateOf("") }
    var selectedRadiusUnit by remember { mutableStateOf(LinearUnit.FEET) }
    var emitterSpacing by remember { mutableStateOf("") }
    var selectedSpacingUnit by remember { mutableStateOf(LinearUnit.FEET) }

    fun resetForm() {
        selectedSystem = null
        systemName = ""
        selectedOrchardId = 0L
        selectedPumpId = 0L
        selectedMethod = IrrigationMethod.DRIP
        emitterFlowRate = ""
        selectedFlowUnit = FlowRateUnit.GALLONSPERHOUR
        emitterRadius = ""
        selectedRadiusUnit = LinearUnit.FEET
        emitterSpacing = ""
        selectedSpacingUnit = LinearUnit.FEET
    }

    LaunchedEffect(selectedSystem) {
        selectedSystem?.let {
            systemName = it.name
            selectedOrchardId = it.orchardId
            selectedPumpId = it.pumpId
            selectedMethod = it.irrigationMethod
            emitterFlowRate = it.emitterFlowRate.toString()
            selectedFlowUnit = it.emitterFlowUnit
            emitterRadius = it.emitterRadius.toString()
            selectedRadiusUnit = it.emitterRadiusLinearUnit
            emitterSpacing = it.emitterSpacing.toString()
            selectedSpacingUnit = it.emitterSpacingLinearUnit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Existing Systems
        FieldGroup(title = "EXISTING IRRIGATION SYSTEMS") {
            var systemExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select System",
                    value = selectedSystem?.name ?: "New System",
                    onClick = { systemExpanded = true }
                )
                DropdownMenu(expanded = systemExpanded, onDismissRequest = { systemExpanded = false }) {
                    DropdownMenuItem(onClick = { resetForm(); systemExpanded = false }) {
                        Text("New System")
                    }
                    irrigationSystems.forEach { system ->
                        DropdownMenuItem(onClick = { selectedSystem = system; systemExpanded = false }) {
                            Text(system.name)
                        }
                    }
                }
            }
        }

        // System Details
        FieldGroup(title = "SYSTEM DETAILS") {
            TransparentInputField("Name", systemName) { systemName = it }
            
            Divider(modifier = Modifier.padding(start = 16.dp))
            
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

            // Pump
            Row(verticalAlignment = Alignment.CenterVertically) {
                var pumpExpanded by remember { mutableStateOf(false) }
                Box(Modifier.weight(1f)) {
                    FieldRow(
                        label = "Pump",
                        value = pumpsMap?.get(selectedPumpId)?.type ?: "Select",
                        onClick = { pumpExpanded = true }
                    )
                    DropdownMenu(expanded = pumpExpanded, onDismissRequest = { pumpExpanded = false }) {
                        pumpsMap?.values?.forEach { pump ->
                            DropdownMenuItem(onClick = { selectedPumpId = pump.id; pumpExpanded = false }) {
                                Text(pump.type)
                            }
                        }
                    }
                }
                IconButton(onClick = onNavigateToAddPump) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Pump", tint = MaterialTheme.colors.primary)
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Irrigation Method
            var methodExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Method",
                    value = selectedMethod.method,
                    onClick = { methodExpanded = true }
                )
                DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                    IrrigationMethod.entries.forEach { method ->
                        DropdownMenuItem(onClick = { selectedMethod = method; methodExpanded = false }) {
                            Text(method.method)
                        }
                    }
                }
            }
        }

        // Emitter Details
        FieldGroup(title = "EMITTER DETAILS") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Flow Rate", emitterFlowRate, isNumber = true, modifier = Modifier.weight(1f)) { emitterFlowRate = it }
                var flowUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(150.dp)) {
                    FieldRow(label = "", value = selectedFlowUnit.unit, onClick = { flowUnitExpanded = true })
                    DropdownMenu(expanded = flowUnitExpanded, onDismissRequest = { flowUnitExpanded = false }) {
                        FlowRateUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { selectedFlowUnit = unit; flowUnitExpanded = false }) { Text(unit.unit) }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Radius", emitterRadius, isNumber = true, modifier = Modifier.weight(1f)) { emitterRadius = it }
                var radiusUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = selectedRadiusUnit.unit, onClick = { radiusUnitExpanded = true })
                    DropdownMenu(expanded = radiusUnitExpanded, onDismissRequest = { radiusUnitExpanded = false }) {
                        LinearUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { selectedRadiusUnit = unit; radiusUnitExpanded = false }) { Text(unit.unit) }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Spacing", emitterSpacing, isNumber = true, modifier = Modifier.weight(1f)) { emitterSpacing = it }
                var spacingUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = selectedSpacingUnit.unit, onClick = { spacingUnitExpanded = true })
                    DropdownMenu(expanded = spacingUnitExpanded, onDismissRequest = { spacingUnitExpanded = false }) {
                        LinearUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { selectedSpacingUnit = unit; spacingUnitExpanded = false }) { Text(unit.unit) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (systemName.isBlank() || selectedOrchardId == 0L || selectedPumpId == 0L) {
                        Toast.makeText(context, "Please fill in name, orchard, and pump", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val system = IrrigationSystem(
                        id = selectedSystem?.id ?: 0L,
                        orchardId = selectedOrchardId,
                        pumpId = selectedPumpId,
                        name = systemName,
                        irrigationMethod = selectedMethod,
                        emitterFlowRate = emitterFlowRate.toDoubleOrNull() ?: 0.0,
                        emitterFlowUnit = selectedFlowUnit,
                        emitterRadius = emitterRadius.toDoubleOrNull() ?: 0.0,
                        emitterRadiusLinearUnit = selectedRadiusUnit,
                        emitterSpacing = emitterSpacing.toDoubleOrNull() ?: 0.0,
                        emitterSpacingLinearUnit = selectedSpacingUnit
                    )
                    if (system.id > 0) irrigationViewModel.updateIrrigationSystem(system)
                    else irrigationViewModel.addIrrigationSystem(system)
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
                    selectedSystem?.let {
                        irrigationViewModel.deleteIrrigationSystem(it)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedSystem != null
            ) {
                Text(stringResource(R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// MARK: - Tab 1: Irrigation Event Form

@Composable
fun IrrigationEventFormTab(
    irrigationViewModel: IrrigationViewModel,
    fragment: Fragment
) {
    val irrigations by irrigationViewModel.irrigations.collectAsStateWithLifecycle(initialValue = emptyList())
    val irrigationSystems by irrigationViewModel.irrigationSystems.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedIrrigation by remember { mutableStateOf<Irrigation?>(null) }
    var selectedSystemId by remember { mutableStateOf(0L) }

    val initialNow = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(initialNow.date) }
    var startTime by remember { mutableStateOf(initialNow.time) }
    var stopDate by remember { mutableStateOf(initialNow.date) }
    var stopTime by remember { mutableStateOf(initialNow.time) }

    fun resetForm() {
        selectedIrrigation = null
        selectedSystemId = 0L
        val now = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
        startDate = now.date
        startTime = now.time
        stopDate = now.date
        stopTime = now.time
    }

    LaunchedEffect(selectedIrrigation) {
        selectedIrrigation?.let {
            selectedSystemId = it.irrigationSystemId
            val startLocal = it.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
            startDate = startLocal.date
            startTime = startLocal.time
            val stopLocal = it.stopTime.toLocalDateTime(TimeZone.currentSystemDefault())
            stopDate = stopLocal.date
            stopTime = stopLocal.time
        }
    }

    // Handle Picker Results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(IrrigationComposeFragment.STARTDATE_REQ, fragment) { _, bundle ->
            bundle.getString(IrrigationComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) startDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(IrrigationComposeFragment.STOPDATE_REQ, fragment) { _, bundle ->
            bundle.getString(IrrigationComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) stopDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(IrrigationComposeFragment.STARTTIME_REQ, fragment) { _, bundle ->
            bundle.getString(IrrigationComposeFragment.TIME_KEY)?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) startTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(IrrigationComposeFragment.STOPTIME_REQ, fragment) { _, bundle ->
            bundle.getString(IrrigationComposeFragment.TIME_KEY)?.let { timeStr ->
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
        // Existing Irrigations
        FieldGroup(title = "EXISTING IRRIGATIONS") {
            var expanded by remember { mutableStateOf(false) }
            Box {
                val label = if (selectedIrrigation != null) {
                    val systemName = irrigationSystems.find { it.id == selectedIrrigation!!.irrigationSystemId }?.name ?: "System"
                    val startLocal = selectedIrrigation!!.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    "$systemName - ${dateToShortString(startLocal.date)}"
                } else "New Irrigation"
                FieldRow(
                    label = "Select Irrigation",
                    value = label,
                    onClick = { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { resetForm(); expanded = false }) {
                        Text("New Irrigation")
                    }
                    irrigations.forEach { irrigation ->
                        val systemName = irrigationSystems.find { it.id == irrigation.irrigationSystemId }?.name ?: "System"
                        val startLocal = irrigation.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                        val dateStr = "${dateToShortString(startLocal.date)} ${timeToShortString(startLocal.time)}"
                        DropdownMenuItem(onClick = { selectedIrrigation = irrigation; expanded = false }) {
                            Text("$systemName - $dateStr")
                        }
                    }
                }
            }
        }

        // Irrigation Details
        FieldGroup(title = "IRRIGATION DETAILS") {
            // System picker
            var systemExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Irrigation System",
                    value = irrigationSystems.find { it.id == selectedSystemId }?.name ?: "Select",
                    onClick = { systemExpanded = true }
                )
                DropdownMenu(expanded = systemExpanded, onDismissRequest = { systemExpanded = false }) {
                    irrigationSystems.forEach { system ->
                        DropdownMenuItem(onClick = { selectedSystemId = system.id; systemExpanded = false }) {
                            Text(system.name)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Start Date & Time
            DateTimeRow(
                label = "Start",
                dateValue = dateToShortString(startDate),
                timeValue = timeToShortString(startTime),
                onDateClick = { DatePickerFragment(IrrigationComposeFragment.STARTDATE_REQ, IrrigationComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Start Date") },
                onTimeClick = { TimePickerFragment(IrrigationComposeFragment.STARTTIME_REQ, IrrigationComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Start Time") }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Stop Date & Time
            DateTimeRow(
                label = "Stop",
                dateValue = dateToShortString(stopDate),
                timeValue = timeToShortString(stopTime),
                onDateClick = { DatePickerFragment(IrrigationComposeFragment.STOPDATE_REQ, IrrigationComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Stop Date") },
                onTimeClick = { TimePickerFragment(IrrigationComposeFragment.STOPTIME_REQ, IrrigationComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Stop Time") }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (selectedSystemId == 0L) {
                        Toast.makeText(context, "Please select an irrigation system", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val startInstant = KtInstant.fromEpochMilliseconds(
                        LocalDateTime(startDate, startTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                    val stopInstant = KtInstant.fromEpochMilliseconds(
                        LocalDateTime(stopDate, stopTime).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    )
                    val irrigation = Irrigation(
                        id = selectedIrrigation?.id ?: 0L,
                        irrigationSystemId = selectedSystemId,
                        startTime = startInstant,
                        stopTime = stopInstant
                    )
                    if (irrigation.id > 0) irrigationViewModel.updateIrrigation(irrigation)
                    else irrigationViewModel.addIrrigation(irrigation)
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
                    selectedIrrigation?.let {
                        irrigationViewModel.deleteIrrigation(it)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedIrrigation != null
            ) {
                Text(stringResource(R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
