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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.SoilMoisture
import com.orchardlog.treedata.shared.viewmodels.IrrigationViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.TimePickerFragment
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock as KtClock
import kotlin.time.Instant as KtInstant

class SoilMoistureComposeFragment : Fragment() {

    private val irrigationViewModel: IrrigationViewModel = ViewModelProvider.irrigationViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    SoilMoistureScreen(
                        irrigationViewModel,
                        orchardViewModel,
                        fragment = this@SoilMoistureComposeFragment
                    )
                }
            }
        }
    }

    companion object {
        const val SOILMOISTUREDATEKEY = "soilMoistureDate"
        const val SOILMOISTUREDATEREQUESTKEY = "soilMoistureDateKey"
        const val SOILMOISTURETIMEKEY = "soilMoistureTime"
        const val SOILMOISTURETIMEREQUESTKEY = "soilMoistureTimeKey"
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun SoilMoistureScreen(
    irrigationViewModel: IrrigationViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    val soilMoistureList by irrigationViewModel.soilMoisture.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val context = LocalContext.current

    var selectedSoilMoisture by remember { mutableStateOf<SoilMoisture?>(null) }
    var selectedOrchardId by remember { mutableStateOf(0L) }
    var centibar by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf("") }

    val initialNow = KtClock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var date by remember { mutableStateOf(initialNow.date) }
    var time by remember { mutableStateOf(initialNow.time) }

    // Update state when a saved reading is selected
    LaunchedEffect(selectedSoilMoisture) {
        selectedSoilMoisture?.let { reading ->
            selectedOrchardId = reading.orchardId
            centibar = reading.centibar.toString()
            percent = reading.percent.toString()
            val localDateTime = reading.date.toLocalDateTime(TimeZone.currentSystemDefault())
            date = localDateTime.date
            time = localDateTime.time
        }
    }

    // Listen for date/time picker results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(SoilMoistureComposeFragment.SOILMOISTUREDATEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(SoilMoistureComposeFragment.SOILMOISTUREDATEKEY)?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        date = LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(SoilMoistureComposeFragment.SOILMOISTURETIMEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(SoilMoistureComposeFragment.SOILMOISTURETIMEKEY)?.let { timeStr ->
                try {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) {
                        time = LocalTime(parts[0].toInt(), parts[1].toInt())
                    }
                } catch (e: Exception) { }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.soil_moisture),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Select a previous reading
        FieldGroup(title = stringResource(id = R.string.select_a_previous_reading).uppercase()) {
            var readingExpanded by remember { mutableStateOf(false) }
            Box {
                FieldRow(
                    label = "Reading",
                    value = selectedSoilMoisture?.toString() ?: "New Reading",
                    onClick = { readingExpanded = true }
                )
                DropdownMenu(expanded = readingExpanded, onDismissRequest = { readingExpanded = false }) {
                    DropdownMenuItem(onClick = {
                        selectedSoilMoisture = null
                        readingExpanded = false
                    }) {
                        Text("New Reading")
                    }
                    soilMoistureList.forEach { reading ->
                        DropdownMenuItem(onClick = {
                            selectedSoilMoisture = reading
                            readingExpanded = false
                        }) {
                            Text(reading.toString())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details
        FieldGroup(title = "READING DETAILS") {
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

            // Date & Time
            FieldRow(
                label = "Date/Time",
                value = "${dateToShortString(date)}  ${timeToShortString(time)}",
                isPill = true,
                onClick = {
                    DatePickerFragment(SoilMoistureComposeFragment.SOILMOISTUREDATEREQUESTKEY, SoilMoistureComposeFragment.SOILMOISTUREDATEKEY)
                        .show(fragment.childFragmentManager, context.getString(R.string.date))
                }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Centibar
            TransparentInputField(
                label = stringResource(id = R.string.enter_the_centibar_reading),
                value = centibar,
                isNumber = true,
                onValueChange = { centibar = it }
            )

            Divider(modifier = Modifier.padding(start = 16.dp))

            // Percent Soil Moisture
            TransparentInputField(
                label = stringResource(id = R.string.percent_soil_moisture),
                value = percent,
                isNumber = true,
                onValueChange = { percent = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
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

                    val instant = LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault())

                    val sm = SoilMoisture(
                        id = selectedSoilMoisture?.id ?: 0L,
                        orchardId = selectedOrchardId,
                        date = instant,
                        centibar = centibar.toIntOrNull() ?: 0,
                        percent = percent.toIntOrNull() ?: 0
                    )

                    if (sm.id > 0) {
                        irrigationViewModel.updateSoilMoisture(sm)
                    } else {
                        irrigationViewModel.addSoilMoisture(sm)
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
                    selectedSoilMoisture = null
                    centibar = ""
                    percent = ""
                    val now = KtClock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    date = now.date
                    time = now.time
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
            ) {
                Text(stringResource(id = R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedSoilMoisture?.let {
                        irrigationViewModel.deleteSoilMoisture(it)
                        selectedSoilMoisture = null
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                enabled = selectedSoilMoisture != null
            ) {
                Text(stringResource(id = R.string.delete_button_text), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
