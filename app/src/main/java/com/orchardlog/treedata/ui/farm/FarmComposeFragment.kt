package com.orchardlog.treedata.ui.farm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orchardlog.treedata.shared.model.Farm
import com.orchardlog.treedata.shared.viewmodels.FarmViewModel
import com.orchardlog.treedata.shared.viewmodels.FarmerViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.shared.TemporalUtils
import com.orchardlog.treedata.ui.theme.*

class FarmComposeFragment : Fragment() {

    private val farmViewModel: FarmViewModel = ViewModelProvider.farmViewModel
    private val farmerViewModel: FarmerViewModel = ViewModelProvider.farmerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    FarmScreen(farmViewModel, farmerViewModel)
                }
            }
        }
    }
}

@Composable
fun FarmScreen(farmViewModel: FarmViewModel, farmerViewModel: FarmerViewModel) {
    val farms by farmViewModel.farms.collectAsStateWithLifecycle(initialValue = emptyList())
    val farmers by farmerViewModel.farmers.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var farmName by remember { mutableStateOf("") }
    var siteId by remember { mutableStateOf("") }

    val farmsList = farms ?: emptyList()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Farms",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )

            if (farmsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No farms found. Add one to get started.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(farmsList) { farm ->
                        FarmItem(farm, onDelete = { farmViewModel.deleteFarm(farm) })
                    }
                }
            }
            
            // Add Farm Button at bottom like iOS
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Farm", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Farm") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            label = { Text("Farm Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = siteId,
                            onValueChange = { siteId = it },
                            label = { Text("Site ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val f = farmers ?: emptyList()
                        val farmerId = if (f.isNotEmpty()) f[0].id else 0L
                        if (farmerId == 0L) {
                            Toast.makeText(context, "Please create a farmer first.", Toast.LENGTH_SHORT).show()
                        } else {
                            val farm = Farm(
                                persistentId = TemporalUtils.randomUUID(),
                                farmerId = farmerId,
                                name = farmName,
                                siteId = siteId,
                                validFrom = TemporalUtils.now()
                            )
                            farmViewModel.addFarm(farm)
                            farmName = ""
                            siteId = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FarmItem(farm: Farm, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = farm.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                Text(text = "Site ID: ${farm.siteId}", fontSize = 13.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Farm", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
