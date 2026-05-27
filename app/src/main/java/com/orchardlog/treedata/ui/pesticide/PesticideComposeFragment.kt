package com.orchardlog.treedata.ui.pesticide

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
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
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.PesticideViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.TimePickerFragment
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone as KtTimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant as KtInstant

class PesticideComposeFragment : Fragment() {

    private val pesticideViewModel: PesticideViewModel = ViewModelProvider.pesticideViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    PesticideTabbedScreen(
                        pesticideViewModel,
                        orchardViewModel,
                        fragment = this@PesticideComposeFragment
                    )
                }
            }
        }
    }

    companion object {
        const val STARTDATE_REQ = "pesticideStartDateKey"
        const val STOPDATE_REQ = "pesticideStopDateKey"
        const val STARTTIME_REQ = "pesticideStartTimeKey"
        const val STOPTIME_REQ = "pesticideStopTimeKey"
        const val DATE_KEY = "pesticideDate"
        const val TIME_KEY = "pesticideTime"
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun PesticideTabbedScreen(
    pesticideViewModel: PesticideViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pesticides", "Applications", "Report")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.pesticide),
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
            0 -> PesticideFormTab(pesticideViewModel)
            1 -> PesticideApplicationFormTab(pesticideViewModel, orchardViewModel, fragment)
            2 -> PesticideReportScreen(pesticideViewModel, orchardViewModel, fragment)
        }
    }
}

// MARK: - Tab 0: Pesticide Form

@Composable
fun PesticideFormTab(viewModel: PesticideViewModel) {
    val pesticides by viewModel.pesticides.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedPesticide by remember { mutableStateOf<Pesticide?>(null) }
    var productName by remember { mutableStateOf("") }
    var eparegno by remember { mutableStateOf("") }
    var selectedSignalWord by remember { mutableStateOf(SignalWord.CAUTION) }
    var rei by remember { mutableStateOf("") }
    var selectedReiUnit by remember { mutableStateOf(REIUnit.HOUR) }

    fun resetForm() {
        selectedPesticide = null
        productName = ""
        eparegno = ""
        selectedSignalWord = SignalWord.CAUTION
        rei = ""
        selectedReiUnit = REIUnit.HOUR
    }

    LaunchedEffect(selectedPesticide) {
        selectedPesticide?.let {
            productName = it.productName
            eparegno = it.eparegno
            selectedSignalWord = it.signalWord
            rei = it.rei.toString()
            selectedReiUnit = it.reiUnit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Existing Pesticides
        FieldGroup(title = "EXISTING PESTICIDES") {
            var expanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Select Pesticide",
                    value = selectedPesticide?.productName ?: "New Pesticide",
                    onClick = { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { resetForm(); expanded = false }) {
                        Text("New Pesticide")
                    }
                    pesticides.forEach { p ->
                        DropdownMenuItem(onClick = { selectedPesticide = p; expanded = false }) {
                            Text(p.productName)
                        }
                    }
                }
            }
        }

        // Pesticide Details
        FieldGroup(title = "PESTICIDE DETAILS") {
            TransparentInputField("Product Name", productName) { productName = it }
            Divider(modifier = Modifier.padding(start = 16.dp))
            TransparentInputField("EPA Reg. No.", eparegno) { eparegno = it }
        }

        // Safety Information
        FieldGroup(title = "SAFETY INFORMATION") {
            var signalExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Signal Word",
                    value = selectedSignalWord.name,
                    onClick = { signalExpanded = true }
                )
                DropdownMenu(expanded = signalExpanded, onDismissRequest = { signalExpanded = false }) {
                    SignalWord.entries.forEach { sw ->
                        DropdownMenuItem(onClick = { selectedSignalWord = sw; signalExpanded = false }) {
                            Text(sw.name)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("REI", rei, isNumber = true, modifier = Modifier.weight(1f)) { rei = it }
                var reiUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = selectedReiUnit.name, onClick = { reiUnitExpanded = true })
                    DropdownMenu(expanded = reiUnitExpanded, onDismissRequest = { reiUnitExpanded = false }) {
                        REIUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { selectedReiUnit = unit; reiUnitExpanded = false }) {
                                Text(unit.name)
                            }
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
                    if (productName.isBlank()) return@Button
                    val p = Pesticide(
                        id = selectedPesticide?.id ?: 0L,
                        productName = productName,
                        eparegno = eparegno,
                        signalWord = selectedSignalWord,
                        rei = rei.toIntOrNull() ?: 0,
                        reiUnit = selectedReiUnit
                    )
                    if (p.id > 0) viewModel.updatePesticide(p) else viewModel.addPesticide(p)
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
                    selectedPesticide?.let {
                        viewModel.deletePesticide(it)
                        resetForm()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedPesticide != null
            ) {
                Text(stringResource(R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// MARK: - Tab 1: Pesticide Application Form

@OptIn(ExperimentalTime::class)
@Composable
fun PesticideApplicationFormTab(
    pesticideViewModel: PesticideViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    val applications by pesticideViewModel.pesticideApplicationsWithItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val pesticides by pesticideViewModel.pesticides.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedAppWithItems by remember { mutableStateOf<PesticideApplicationWithItems?>(null) }
    var currentItems by remember { mutableStateOf<List<PesticideApplicationItem>>(emptyList()) }

    // Application fields
    var selectedOrchardId by remember { mutableStateOf(0L) }
    val initialNow = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(KtTimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(initialNow.date) }
    var startTime by remember { mutableStateOf(initialNow.time) }
    var stopDate by remember { mutableStateOf(initialNow.date) }
    var stopTime by remember { mutableStateOf(initialNow.time) }
    var areaTreated by remember { mutableStateOf("") }
    var areaUnit by remember { mutableStateOf(OrchardUnit.ACRE) }
    var dilution by remember { mutableStateOf("") }
    var dilutionUnit by remember { mutableStateOf(WeightOrMeasureUnit.GALLONS) }
    var appMethod by remember { mutableStateOf(ApplicationMethod.AIRBLAST) }

    // Item fields
    var selectedPesticideId by remember { mutableStateOf(0L) }
    var itemApplied by remember { mutableStateOf("") }
    var itemAppliedUnit by remember { mutableStateOf(WeightOrMeasureUnit.POUNDS) }

    fun resetForm() {
        selectedAppWithItems = null
        currentItems = emptyList()
        selectedOrchardId = 0L
        areaTreated = ""
        areaUnit = OrchardUnit.ACRE
        dilution = ""
        dilutionUnit = WeightOrMeasureUnit.GALLONS
        appMethod = ApplicationMethod.AIRBLAST
        itemApplied = ""
        val now = KtInstant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(KtTimeZone.currentSystemDefault())
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
            val startLocal = app.applicationStart.toLocalDateTime(KtTimeZone.currentSystemDefault())
            startDate = startLocal.date
            startTime = startLocal.time
            val stopLocal = app.applicationStop.toLocalDateTime(KtTimeZone.currentSystemDefault())
            stopDate = stopLocal.date
            stopTime = stopLocal.time
            dilution = app.dilution.toString()
            dilutionUnit = app.dilutionUnit
            areaTreated = app.areaTreated.toString()
            areaUnit = app.areaTreatedUnit
            appMethod = app.applicationMethod
            currentItems = appWithItems.items
        }
    }

    // Handle Picker Results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(PesticideComposeFragment.STARTDATE_REQ, fragment) { _, bundle ->
            bundle.getString(PesticideComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) startDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(PesticideComposeFragment.STOPDATE_REQ, fragment) { _, bundle ->
            bundle.getString(PesticideComposeFragment.DATE_KEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) stopDate = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(PesticideComposeFragment.STARTTIME_REQ, fragment) { _, bundle ->
            bundle.getString(PesticideComposeFragment.TIME_KEY)?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) startTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                } catch (_: Exception) {}
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(PesticideComposeFragment.STOPTIME_REQ, fragment) { _, bundle ->
            bundle.getString(PesticideComposeFragment.TIME_KEY)?.let { timeStr ->
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
                onDateClick = { DatePickerFragment(PesticideComposeFragment.STARTDATE_REQ, PesticideComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Start Date") },
                onTimeClick = { TimePickerFragment(PesticideComposeFragment.STARTTIME_REQ, PesticideComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Start Time") }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Stop Date & Time
            DateTimeRow(
                label = "Stop",
                dateValue = dateToShortString(stopDate),
                timeValue = timeToShortString(stopTime),
                onDateClick = { DatePickerFragment(PesticideComposeFragment.STOPDATE_REQ, PesticideComposeFragment.DATE_KEY).show(fragment.childFragmentManager, "Stop Date") },
                onTimeClick = { TimePickerFragment(PesticideComposeFragment.STOPTIME_REQ, PesticideComposeFragment.TIME_KEY).show(fragment.childFragmentManager, "Stop Time") }
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

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Dilution
            Row(verticalAlignment = Alignment.CenterVertically) {
                TransparentInputField("Dilution", dilution, isNumber = true, modifier = Modifier.weight(1f)) { dilution = it }
                var dilutionUnitExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.width(100.dp)) {
                    FieldRow(label = "", value = dilutionUnit.toString(), onClick = { dilutionUnitExpanded = true })
                    DropdownMenu(expanded = dilutionUnitExpanded, onDismissRequest = { dilutionUnitExpanded = false }) {
                        WeightOrMeasureUnit.entries.forEach { unit ->
                            DropdownMenuItem(onClick = { dilutionUnit = unit; dilutionUnitExpanded = false }) { Text(unit.toString()) }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Application Method
            var methodExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Application Method",
                    value = appMethod.toString(),
                    onClick = { methodExpanded = true }
                )
                DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                    ApplicationMethod.entries.forEach { method ->
                        DropdownMenuItem(onClick = { appMethod = method; methodExpanded = false }) { Text(method.toString()) }
                    }
                }
            }
        }

        // Pesticides in this Application
        FieldGroup(title = "PESTICIDES IN THIS APPLICATION") {
            currentItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pesticides.find { it.id == item.pesticideId }?.productName ?: "Unknown",
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
                        currentItems = currentItems.filter { it.pesticideId != item.pesticideId }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colors.error)
                    }
                }
                if (index < currentItems.size - 1) Divider(modifier = Modifier.padding(start = 16.dp))
            }

            if (currentItems.isEmpty()) {
                Text(
                    "No pesticides added.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Divider()

            // Add pesticide item
            var pestExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Add Pesticide",
                    value = pesticides.find { it.id == selectedPesticideId }?.productName ?: "Select",
                    onClick = { pestExpanded = true }
                )
                DropdownMenu(expanded = pestExpanded, onDismissRequest = { pestExpanded = false }) {
                    pesticides.forEach { p ->
                        DropdownMenuItem(onClick = { selectedPesticideId = p.id; pestExpanded = false }) {
                            Text(p.productName)
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
                        if (selectedPesticideId == 0L) return@IconButton
                        val newItem = PesticideApplicationItem(
                            pesticideApplicationId = selectedAppWithItems?.application?.id ?: 0L,
                            pesticideId = selectedPesticideId,
                            applied = amount,
                            appliedUnit = itemAppliedUnit
                        )
                        val existing = currentItems.indexOfFirst { it.pesticideId == selectedPesticideId }
                        currentItems = if (existing >= 0) {
                            currentItems.toMutableList().also { it[existing] = newItem }
                        } else {
                            currentItems + newItem
                        }
                        itemApplied = ""
                    },
                    enabled = itemApplied.isNotEmpty() && selectedPesticideId != 0L
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
                        Toast.makeText(context, "Please select an orchard and add pesticides", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val startInstant = KtInstant.fromEpochMilliseconds(LocalDateTime(startDate, startTime).toInstant(KtTimeZone.currentSystemDefault()).toEpochMilliseconds())
                    val stopInstant = KtInstant.fromEpochMilliseconds(LocalDateTime(stopDate, stopTime).toInstant(KtTimeZone.currentSystemDefault()).toEpochMilliseconds())
                    val app = PesticideApplication(
                        id = selectedAppWithItems?.application?.id ?: 0L,
                        orchardId = selectedOrchardId,
                        applicationStart = startInstant,
                        applicationStop = stopInstant,
                        dilution = dilution.toIntOrNull() ?: 0,
                        dilutionUnit = dilutionUnit,
                        areaTreated = areaTreated.toDoubleOrNull() ?: 0.0,
                        areaTreatedUnit = areaUnit,
                        applicationMethod = appMethod
                    )
                    if (app.id > 0L) {
                        pesticideViewModel.updatePesticideApplicationWithItems(app, currentItems)
                    } else {
                        pesticideViewModel.savePesticideApplicationWithItems(app, currentItems)
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
                        pesticideViewModel.deletePesticideApplicationWithItems(it.application)
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
