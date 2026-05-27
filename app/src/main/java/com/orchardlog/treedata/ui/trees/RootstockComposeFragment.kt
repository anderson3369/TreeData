package com.orchardlog.treedata.ui.trees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.orchardlog.treedata.shared.model.Rootstock
import com.orchardlog.treedata.shared.model.RootstockType
import com.orchardlog.treedata.shared.viewmodels.TreeViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider

class RootstockComposeFragment : Fragment() {

    private val treeViewModel: TreeViewModel = ViewModelProvider.treeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RootstockScreen(treeViewModel)
                }
            }
        }
    }
}

@Composable
fun RootstockScreen(viewModel: TreeViewModel) {
    val rootstocks by viewModel.rootstocks.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val rootstockTypes = RootstockType.entries.toTypedArray()

    var selectedRootstock by remember { mutableStateOf<Rootstock?>(null) }
    var name by remember { mutableStateOf("") }
    var cultivar by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(rootstockTypes.first()) }

    LaunchedEffect(selectedRootstock) {
        selectedRootstock?.let {
            name = it.name
            cultivar = it.cultivar
            selectedType = it.rootstockType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.select_a_rootstock),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        var rootstockExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedRootstock?.name ?: "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, "dropdown", Modifier.clickable { rootstockExpanded = true })
                }
            )
            DropdownMenu(expanded = rootstockExpanded, onDismissRequest = { rootstockExpanded = false }) {
                rootstocks?.forEach { r ->
                    DropdownMenuItem(onClick = {
                        selectedRootstock = r
                        rootstockExpanded = false
                    }) {
                        Text(r.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.rootstock), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.enter_the_name_of_the_rootstock)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.cultivar), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = cultivar,
            onValueChange = { cultivar = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.rootstock_type), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        var typeExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedType.type,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, "dropdown", Modifier.clickable { typeExpanded = true })
                }
            )
            DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                rootstockTypes.forEach { type ->
                    DropdownMenuItem(onClick = {
                        selectedType = type
                        typeExpanded = false
                    }) {
                        Text(type.type)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val r = Rootstock(
                    id = selectedRootstock?.id ?: 0L,
                    name = name,
                    cultivar = cultivar,
                    rootstockType = selectedType
                )
                if (r.id > 0) {
                    viewModel.updateRootstock(r)
                } else {
                    viewModel.addRootstock(r)
                }
                Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(id = R.string.save_button_text))
            }

            Button(onClick = {
                selectedRootstock = null
                name = ""
                cultivar = ""
                selectedType = rootstockTypes.first()
            }) {
                Text(stringResource(id = R.string.new_button_text))
            }

            Button(
                onClick = {
                    selectedRootstock?.let {
                        viewModel.deleteRootstock(it)
                        selectedRootstock = null
                        name = ""
                        cultivar = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
            ) {
                Text(stringResource(id = R.string.delete_button_text))
            }
        }
    }
}
