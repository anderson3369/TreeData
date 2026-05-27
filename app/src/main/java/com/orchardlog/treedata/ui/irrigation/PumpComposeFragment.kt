package com.orchardlog.treedata.ui.irrigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.orchardlog.treedata.shared.model.FlowRateUnit
import com.orchardlog.treedata.shared.model.Pump
import com.orchardlog.treedata.shared.viewmodels.PumpViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*

class PumpComposeFragment : Fragment() {

    private val pumpViewModel: PumpViewModel = ViewModelProvider.pumpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    PumpScreen(pumpViewModel)
                }
            }
        }
    }
}

@Composable
fun PumpScreen(viewModel: PumpViewModel) {
    val pumps by viewModel.pumps.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val flowRateUnits = FlowRateUnit.entries.toTypedArray()

    var selectedPump by remember { mutableStateOf<Pump?>(null) }
    var pumpType by remember { mutableStateOf("") }
    var horsepower by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("") }
    var flowRate by remember { mutableStateOf("") }
    var selectedFlowRateUnit by remember { mutableStateOf(FlowRateUnit.GALLONSPERHOUR) }

    fun populateFields(pump: Pump?) {
        if (pump != null) {
            pumpType = pump.type
            horsepower = pump.horsepower.toString()
            phase = pump.phase.toString()
            flowRate = pump.flowRate.toString()
            selectedFlowRateUnit = pump.flowRateUnit
        } else {
            pumpType = ""
            horsepower = ""
            phase = ""
            flowRate = ""
            selectedFlowRateUnit = FlowRateUnit.GALLONSPERHOUR
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.pumps),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section: Existing Pumps
            FieldGroup(title = "EXISTING PUMPS") {
                var pumpExpanded by remember { mutableStateOf(false) }
                Box {
                    FieldRow(
                        label = "Select Pump",
                        value = selectedPump?.type ?: "New Pump",
                        onClick = { pumpExpanded = true }
                    )
                    DropdownMenu(expanded = pumpExpanded, onDismissRequest = { pumpExpanded = false }) {
                        DropdownMenuItem(onClick = {
                            selectedPump = null
                            populateFields(null)
                            pumpExpanded = false
                        }) {
                            Text("New Pump")
                        }
                        pumps.forEach { p ->
                            DropdownMenuItem(onClick = {
                                selectedPump = p
                                populateFields(p)
                                pumpExpanded = false
                            }) {
                                Text(p.type)
                            }
                        }
                    }
                }
            }

            // Section: Pump Details
            FieldGroup(title = "PUMP DETAILS") {
                TransparentInputField(
                    label = stringResource(id = R.string.pump_type),
                    value = pumpType,
                    onValueChange = { pumpType = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = stringResource(id = R.string.pump_horsepower),
                    value = horsepower,
                    isNumber = true,
                    onValueChange = { horsepower = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = stringResource(id = R.string.pump_phase),
                    value = phase,
                    isNumber = true,
                    onValueChange = { phase = it }
                )
            }

            // Section: Flow Rate
            FieldGroup(title = "FLOW RATE") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TransparentInputField(
                        label = "Flow Rate",
                        value = flowRate,
                        isNumber = true,
                        modifier = Modifier.weight(1f),
                        onValueChange = { flowRate = it }
                    )
                    var flowRateUnitExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.width(150.dp)) {
                        FieldRow(
                            label = "",
                            value = selectedFlowRateUnit.unit,
                            onClick = { flowRateUnitExpanded = true }
                        )
                        DropdownMenu(expanded = flowRateUnitExpanded, onDismissRequest = { flowRateUnitExpanded = false }) {
                            flowRateUnits.forEach { unit ->
                                DropdownMenuItem(onClick = {
                                    selectedFlowRateUnit = unit
                                    flowRateUnitExpanded = false
                                }) {
                                    Text(unit.unit)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val p = Pump(
                            id = selectedPump?.id ?: 0L,
                            type = pumpType,
                            horsepower = horsepower.toDoubleOrNull() ?: 0.0,
                            phase = phase.toIntOrNull() ?: 0,
                            flowRate = flowRate.toDoubleOrNull() ?: 0.0,
                            flowRateUnit = selectedFlowRateUnit
                        )
                        if (p.id > 0) {
                            viewModel.updatePump(p)
                        } else {
                            viewModel.addPump(p)
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
                        selectedPump = null
                        populateFields(null)
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE8F5E9))
                ) {
                    Text(stringResource(id = R.string.new_button_text), color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedPump?.let {
                            viewModel.deletePump(it)
                            selectedPump = null
                            populateFields(null)
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE), contentColor = Color.Gray),
                    enabled = selectedPump != null
                ) {
                    Text(stringResource(id = R.string.delete_button_text), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
