package com.orchardlog.treedata.ui.farmer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.orchardlog.treedata.shared.TemporalUtils
import com.orchardlog.treedata.shared.model.Farmer
import com.orchardlog.treedata.shared.viewmodels.FarmerViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.*

class FarmerComposeFragment : Fragment() {

    private val farmerViewModel: FarmerViewModel = ViewModelProvider.farmerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    FarmerScreen(farmerViewModel)
                }
            }
        }
    }
}

@Composable
fun FarmerScreen(viewModel: FarmerViewModel) {
    val farmers by viewModel.farmers.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Populate from existing farmer if available
    LaunchedEffect(farmers) {
        val f = farmers
        if (!f.isNullOrEmpty()) {
            val farmer = f[0]
            name = farmer.name
            address = farmer.address
            city = farmer.city
            state = farmer.state
            zip = farmer.zip
            phone = farmer.phone
            email = farmer.email
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        Text(
            text = "Farmer",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FieldGroup(title = "FARMER INFORMATION") {
                TransparentInputField(
                    label = "Name",
                    value = name,
                    onValueChange = { name = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = "Address",
                    value = address,
                    onValueChange = { address = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = "City",
                    value = city,
                    onValueChange = { city = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = "State",
                    value = state,
                    onValueChange = { state = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = "Zip",
                    value = zip,
                    onValueChange = { zip = it }
                )
            }

            FieldGroup(title = "CONTACT") {
                TransparentInputField(
                    label = "Phone",
                    value = phone,
                    onValueChange = { phone = it }
                )
                Divider(modifier = Modifier.padding(start = 16.dp))
                TransparentInputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )
            }

            Button(
                onClick = {
                    val f = farmers
                    val farmer = Farmer(
                        id = if (!f.isNullOrEmpty()) f[0].id else 0,
                        persistentId = if (!f.isNullOrEmpty()) f[0].persistentId
                                        else TemporalUtils.randomUUID(),
                        name = name,
                        address = address,
                        city = city,
                        state = state,
                        zip = zip,
                        phone = phone,
                        email = email
                    )
                    if (farmer.id > 0) {
                        viewModel.updateFarmer(farmer = farmer)
                    } else {
                        viewModel.addFarmer(farmer = farmer)
                    }
                    Toast.makeText(context, "Farmer saved!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White)
            ) {
                Text("Save Farmer", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
