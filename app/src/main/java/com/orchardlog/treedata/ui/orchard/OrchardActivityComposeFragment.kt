package com.orchardlog.treedata.ui.orchard

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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.orchardlog.treedata.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.OrchardActivity
import com.orchardlog.treedata.shared.viewmodels.FarmerViewModel
import com.orchardlog.treedata.shared.viewmodels.FarmViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.TimePickerFragment
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

class OrchardActivityComposeFragment : Fragment() {

    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel
    private val farmerViewModel: FarmerViewModel = ViewModelProvider.farmerViewModel
    private val farmViewModel: FarmViewModel = ViewModelProvider.farmViewModel
    private var hasNavigatedToSetup = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    OrchardActivityTabbedScreen(
                        orchardViewModel,
                        onNavigateToSoilMoisture = {
                            findNavController().navigate(R.id.action_nav_orchardTask_to_nav_soilMoisture)
                        },
                        fragment = this@OrchardActivityComposeFragment
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                farmerViewModel.farmers,
                farmViewModel.farms,
                orchardViewModel.allOrchards
            ) { farmers, farms, orchards ->
                if (farmers == null || farms == null || orchards == null) null
                else farmers.isEmpty() || farms.isEmpty() || orchards.isEmpty()
            }.collect { needsSetup ->
                if (needsSetup == true && !hasNavigatedToSetup) {
                    hasNavigatedToSetup = true
                    findNavController().navigate(R.id.action_nav_orchardTask_to_nav_setup)
                }
            }
        }
    }
}

@Composable
fun OrchardActivityTabbedScreen(
    viewModel: OrchardViewModel,
    onNavigateToSoilMoisture: () -> Unit,
    fragment: Fragment
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Activity", "Report")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.orchard_activity),
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
            0 -> OrchardActivityFormTab(viewModel, onNavigateToSoilMoisture, fragment)
            1 -> OrchardTaskReportScreen(viewModel, fragment)
        }
    }
}

@Composable
fun OrchardActivityFormTab(
    viewModel: OrchardViewModel,
    onNavigateToSoilMoisture: () -> Unit,
    fragment: Fragment
) {
    val activities by viewModel.orchardActivities.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmOrchardsMap by viewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val activityTypes = stringArrayResource(id = R.array.orchard_activities)
    val context = LocalContext.current

    var selectedSavedActivity by remember { mutableStateOf<OrchardActivity?>(null) }
    var selectedOrchardId by remember { mutableStateOf(0L) }
    var selectedActivityType by remember { mutableStateOf(activityTypes.firstOrNull() ?: "") }
    var notes by remember { mutableStateOf("") }

    val initialNow = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(initialNow.date) }
    var startTime by remember { mutableStateOf(initialNow.time) }
    var stopDate by remember { mutableStateOf(initialNow.date) }
    var stopTime by remember { mutableStateOf(initialNow.time) }

    // Update state when a saved activity is selected
    LaunchedEffect(selectedSavedActivity) {
        selectedSavedActivity?.let { activity ->
            selectedOrchardId = activity.orchardId
            selectedActivityType = activity.activity
            notes = activity.notes
            val start = activity.activityStart.toLocalDateTime(TimeZone.currentSystemDefault())
            startDate = start.date
            startTime = LocalTime(start.hour, start.minute)
            val stop = activity.activityStop.toLocalDateTime(TimeZone.currentSystemDefault())
            stopDate = stop.date
            stopTime = LocalTime(stop.hour, stop.minute)
        }
    }

    // Listen for date/time picker results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener("requestTaskStartDateKey", fragment) { _, bundle ->
            bundle.getString("taskDate")?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        startDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener("requestTaskStopDateKey", fragment) { _, bundle ->
            bundle.getString("taskDate")?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        stopDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener("requestTaskStartTimeKey", fragment) { _, bundle ->
            bundle.getString("taskTime")?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) {
                        startTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener("requestTaskStopTimeKey", fragment) { _, bundle ->
            bundle.getString("taskTime")?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) {
                        stopTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Existing Activities Group
        FieldGroup(title = stringResource(id = R.string.select_a_saved_activity).uppercase()) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Activity",
                    value = selectedSavedActivity?.activity ?: "New Activity",
                    onClick = { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        selectedSavedActivity = null
                        expanded = false
                    }) {
                        Text("New Activity")
                    }
                    activities.forEach { activity ->
                        DropdownMenuItem(onClick = {
                            selectedSavedActivity = activity
                            expanded = false
                        }) {
                            Text(activity.toString())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Details Group
        FieldGroup(title = "ACTIVITY DETAILS") {
            // Orchard Selector
            var orchardExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = stringResource(id = R.string.select_an_orchard),
                    value = farmOrchardsMap?.get(selectedOrchardId) ?: "Select",
                    onClick = { orchardExpanded = true }
                )
                DropdownMenu(expanded = orchardExpanded, onDismissRequest = { orchardExpanded = false }) {
                    farmOrchardsMap?.forEach { (id, name) ->
                        DropdownMenuItem(onClick = {
                            selectedOrchardId = id
                            orchardExpanded = false
                        }) {
                            Text(name)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Activity Type Selector
            var typeExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Activity",
                    value = selectedActivityType,
                    onClick = { typeExpanded = true }
                )
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    activityTypes.forEach { type ->
                        DropdownMenuItem(onClick = {
                            selectedActivityType = type
                            typeExpanded = false
                            if (type == context.getString(R.string.soil_moisture)) {
                                onNavigateToSoilMoisture()
                            }
                        }) {
                            Text(type)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Start Date/Time
            DateTimeRow(
                label = "Start",
                dateValue = dateToShortString(startDate),
                timeValue = timeToShortString(startTime),
                onDateClick = {
                    DatePickerFragment("requestTaskStartDateKey", "taskDate")
                        .show(fragment.childFragmentManager, "taskStartDate")
                },
                onTimeClick = {
                    TimePickerFragment("requestTaskStartTimeKey", "taskTime")
                        .show(fragment.childFragmentManager, "taskStartTime")
                }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Stop Date/Time
            DateTimeRow(
                label = "Stop",
                dateValue = dateToShortString(stopDate),
                timeValue = timeToShortString(stopTime),
                onDateClick = {
                    DatePickerFragment("requestTaskStopDateKey", "taskDate")
                        .show(fragment.childFragmentManager, "taskStopDate")
                },
                onTimeClick = {
                    TimePickerFragment("requestTaskStopTimeKey", "taskTime")
                        .show(fragment.childFragmentManager, "taskStopTime")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Group
        FieldGroup(title = stringResource(id = R.string.notes).uppercase()) {
            TransparentInputField(
                label = stringResource(id = R.string.enter_notes_here),
                value = notes,
                onValueChange = { notes = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (selectedOrchardId == 0L) {
                        Toast.makeText(context, R.string.please_select_an_orchard, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val startInstant = LocalDateTime(startDate, startTime).toInstant(TimeZone.currentSystemDefault())
                    val stopInstant = LocalDateTime(stopDate, stopTime).toInstant(TimeZone.currentSystemDefault())

                    val activity = OrchardActivity(
                        id = selectedSavedActivity?.id ?: 0L,
                        orchardId = selectedOrchardId,
                        activity = selectedActivityType,
                        notes = notes,
                        activityStart = startInstant,
                        activityStop = stopInstant
                    )

                    if (activity.id > 0) {
                        viewModel.updateOrchardActivity(activity)
                    } else {
                        viewModel.addOrchardActivity(activity)
                    }
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text(stringResource(id = R.string.save_button_text), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedSavedActivity = null
                    notes = ""
                    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
                    startDate = now.date
                    startTime = now.time
                    stopDate = now.date
                    stopTime = now.time
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
            ) {
                Text(stringResource(id = R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedSavedActivity?.let {
                        viewModel.deleteOrchardActivity(it)
                        selectedSavedActivity = null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFEEEEEE),
                    contentColor = Color.Gray
                ),
                enabled = selectedSavedActivity != null
            ) {
                Text(stringResource(id = R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
