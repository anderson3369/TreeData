package com.orchardlog.treedata.ui.irrigation

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.model.FlowRateUnit
import com.orchardlog.treedata.shared.viewmodels.IrrigationViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.InstantUtils
import java.io.File
import java.io.FileOutputStream

class IrrigationReportComposeFragment : Fragment() {

    private val irrigationViewModel: IrrigationViewModel = ViewModelProvider.irrigationViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    IrrigationReportScreen(
                        irrigationViewModel,
                        orchardViewModel,
                        fragment = this@IrrigationReportComposeFragment
                    )
                }
            }
        }
    }

    companion object {
        const val IRRIGATIONREPORT = "IrrigationReport.pdf"
        const val IRRIGATIONDATEKEY = "irrigationDate"
        const val IRRIGATIONFROMDATEREQUESTKEY = "requestIrrigationFromDateKey"
        const val IRRIGATIONTODATEREQUESTKEY = "requestIrrigationToDateKey"
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun IrrigationReportScreen(
    irrigationViewModel: IrrigationViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val context = LocalContext.current
    
    var selectedOrchardId by remember { mutableStateOf(0L) }
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    var startDate by remember { mutableStateOf(InstantUtils.startOfYear(currentYear)) }
    var endDate by remember { mutableStateOf(InstantUtils.endOfYear(currentYear)) }

    val totalHours by irrigationViewModel.getIrrigationsTotalHours(selectedOrchardId, startDate, endDate)
        .collectAsStateWithLifecycle(initialValue = 0L)

    val pumpWithSystem by irrigationViewModel.getPumpWithIrrigationSystem(selectedOrchardId)
        .collectAsStateWithLifecycle(initialValue = null)

    val totalGallons = remember(totalHours, pumpWithSystem) {
        pumpWithSystem?.let {
            val flowRate = it.pump.flowRate
            val flowUnit = it.pump.flowRateUnit
            if (flowUnit == FlowRateUnit.GALLONSPERMINUTE) {
                flowRate * 60 * totalHours
            } else {
                flowRate * totalHours
            }
        } ?: 0.0
    }

    // Handle Picker Results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(IrrigationReportComposeFragment.IRRIGATIONFROMDATEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(IrrigationReportComposeFragment.IRRIGATIONDATEKEY)?.let { dateStr ->
                InstantUtils.parseStartOfDay(dateStr)?.let { startDate = it }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(IrrigationReportComposeFragment.IRRIGATIONTODATEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(IrrigationReportComposeFragment.IRRIGATIONDATEKEY)?.let { dateStr ->
                InstantUtils.parseEndOfDay(dateStr)?.let { endDate = it }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.select_an_orchard), fontWeight = FontWeight.Bold)
        var orchardExpanded by remember { mutableStateOf(false) }
        val orchardsMap = farmOrchardsMap ?: emptyMap()
        Box {
            OutlinedTextField(
                value = orchardsMap[selectedOrchardId] ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { orchardExpanded = true }) }
            )
            DropdownMenu(expanded = orchardExpanded, onDismissRequest = { orchardExpanded = false }) {
                orchardsMap.forEach { (id, name) ->
                    DropdownMenuItem(onClick = {
                        selectedOrchardId = id
                        orchardExpanded = false
                    }) { Text(name) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { createPDF(fragment.view, context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.irrigation_report))
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.from_date), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = InstantUtils.formatAsDate(startDate),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        DatePickerFragment(IrrigationReportComposeFragment.IRRIGATIONFROMDATEREQUESTKEY, IrrigationReportComposeFragment.IRRIGATIONDATEKEY)
                            .show(fragment.childFragmentManager, "From Date")
                    }) {
                        Icon(painterResource(R.drawable.ic_baseline_calendar_month_24), null)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.to_date), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = InstantUtils.formatAsDate(endDate),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        DatePickerFragment(IrrigationReportComposeFragment.IRRIGATIONTODATEREQUESTKEY, IrrigationReportComposeFragment.IRRIGATIONDATEKEY)
                            .show(fragment.childFragmentManager, "To Date")
                    }) {
                        Icon(painterResource(R.drawable.ic_baseline_calendar_month_24), null)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row {
            Text(text = stringResource(R.string.total_irrigation_hours), fontSize = 14.sp)
            Text(text = totalHours.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row {
            Text(text = stringResource(R.string.total_gallons_pumped), fontSize = 14.sp)
            Text(text = totalGallons.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun createPDF(view: View?, context: Context) {
    if (view == null) return
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    view.draw(canvas)
    document.finishPage(page)
    
    var outputStream: FileOutputStream? = null
    try {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val report = File(path, IrrigationReportComposeFragment.IRRIGATIONREPORT)
        outputStream = FileOutputStream(report.path)
        document.writeTo(outputStream)
        Toast.makeText(context, "Report saved to Downloads", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving report", Toast.LENGTH_SHORT).show()
    } finally {
        outputStream?.flush()
        outputStream?.close()
        document.close()
    }
}
