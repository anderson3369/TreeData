package com.orchardlog.treedata.ui.pesticide

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.orchardlog.treedata.shared.model.PesticideApplicationWithPesticides
import com.orchardlog.treedata.shared.viewmodels.PesticideViewModel
import com.orchardlog.treedata.shared.viewmodels.OrchardViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.utils.DatePickerFragment
import com.orchardlog.treedata.utils.InstantUtils
import java.io.File
import java.io.FileOutputStream

class PesticideReportComposeFragment : Fragment() {

    private val pesticideViewModel: PesticideViewModel = ViewModelProvider.pesticideViewModel
    private val orchardViewModel: OrchardViewModel = ViewModelProvider.orchardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    PesticideReportScreen(
                        pesticideViewModel,
                        orchardViewModel,
                        fragment = this@PesticideReportComposeFragment
                    )
                }
            }
        }
    }

    companion object {
        const val PESTICIDEREPORT = "PesticideReport.pdf"
        const val PESTICIDEDATEKEY = "pesticideDate"
        const val PESTICIDEFROMDATEREQUESTKEY = "requestPesticideFromDateKey"
        const val PESTICIDETODATEREQUESTKEY = "requestPesticideToDateKey"
    }
}

@Composable
fun PesticideReportScreen(
    pesticideViewModel: PesticideViewModel,
    orchardViewModel: OrchardViewModel,
    fragment: Fragment
) {
    val farmOrchardsMap by orchardViewModel.farmWithOrchardsMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val context = LocalContext.current
    
    var selectedOrchardId by remember { mutableStateOf(0L) }
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    var startDate by remember { mutableStateOf(InstantUtils.startOfYear(currentYear)) }
    var endDate by remember { mutableStateOf(InstantUtils.endOfYear(currentYear)) }

    val reportData by pesticideViewModel.getPesticideApplicationWithPesticides(selectedOrchardId, startDate, endDate)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Handle Picker Results
    LaunchedEffect(Unit) {
        fragment.childFragmentManager.setFragmentResultListener(PesticideReportComposeFragment.PESTICIDEFROMDATEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(PesticideReportComposeFragment.PESTICIDEDATEKEY)?.let { dateStr ->
                InstantUtils.parseStartOfDay(dateStr)?.let { startDate = it }
            }
        }
        fragment.childFragmentManager.setFragmentResultListener(PesticideReportComposeFragment.PESTICIDETODATEREQUESTKEY, fragment) { _, bundle ->
            bundle.getString(PesticideReportComposeFragment.PESTICIDEDATEKEY)?.let { dateStr ->
                InstantUtils.parseEndOfDay(dateStr)?.let { endDate = it }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Orchard Selection
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
            Text(stringResource(R.string.pesticide_report))
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            // Start Date
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
                        DatePickerFragment(PesticideReportComposeFragment.PESTICIDEFROMDATEREQUESTKEY, PesticideReportComposeFragment.PESTICIDEDATEKEY)
                            .show(fragment.childFragmentManager, "From Date")
                    }) {
                        Icon(painterResource(R.drawable.ic_baseline_calendar_month_24), null)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            // End Date
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
                        DatePickerFragment(PesticideReportComposeFragment.PESTICIDETODATEREQUESTKEY, PesticideReportComposeFragment.PESTICIDEDATEKEY)
                            .show(fragment.childFragmentManager, "To Date")
                    }) {
                        Icon(painterResource(R.drawable.ic_baseline_calendar_month_24), null)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            reportData.forEach { appWithPest ->
                item {
                    PesticideGroupItem(appWithPest)
                }
                items(appWithPest.pesticides) { pesticide ->
                    val item = appWithPest.items.find { it.pesticideId == pesticide.id }
                    PesticideChildItem(pesticide, item)
                }
            }
        }
    }
}

@Composable
fun PesticideGroupItem(appWithPest: PesticideApplicationWithPesticides) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primaryVariant
    ) {
        Text(
            text = appWithPest.pesticideApplication.description(),
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun PesticideChildItem(pesticide: com.orchardlog.treedata.shared.model.Pesticide, item: com.orchardlog.treedata.shared.model.PesticideApplicationItem?) {
    val dosage = if (item != null) " (${item.applied} ${item.appliedUnit})" else ""
    Text(
        text = "${pesticide.productName}$dosage",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
    )
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
        val report = File(path, PesticideReportComposeFragment.PESTICIDEREPORT)
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
