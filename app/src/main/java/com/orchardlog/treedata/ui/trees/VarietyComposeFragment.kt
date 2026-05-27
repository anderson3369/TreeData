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
import com.orchardlog.treedata.shared.model.Variety
import com.orchardlog.treedata.shared.viewmodels.TreeViewModel
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider

class VarietyComposeFragment : Fragment() {

    private val treeViewModel: TreeViewModel = ViewModelProvider.treeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    VarietyScreen(treeViewModel)
                }
            }
        }
    }
}

@Composable
fun VarietyScreen(viewModel: TreeViewModel) {
    val varieties by viewModel.varieties.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    var selectedVariety by remember { mutableStateOf<Variety?>(null) }
    var name by remember { mutableStateOf("") }
    var cultivar by remember { mutableStateOf("") }

    LaunchedEffect(selectedVariety) {
        selectedVariety?.let {
            name = it.name
            cultivar = it.cultivar
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.select_a_variety),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        var varietyExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedVariety?.name ?: "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, "dropdown", Modifier.clickable { varietyExpanded = true })
                }
            )
            DropdownMenu(expanded = varietyExpanded, onDismissRequest = { varietyExpanded = false }) {
                varieties?.forEach { v ->
                    DropdownMenuItem(onClick = {
                        selectedVariety = v
                        varietyExpanded = false
                    }) {
                        Text(v.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.variety), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.enter_the_variety_name)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.cultivar), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = cultivar,
            onValueChange = { cultivar = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.enter_the_cultivar_if_applicable)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val v = Variety(
                    id = selectedVariety?.id ?: 0L,
                    name = name,
                    cultivar = cultivar
                )
                if (v.id > 0) {
                    viewModel.updateVariety(v)
                } else {
                    viewModel.addVariety(v)
                }
                Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(id = R.string.save_button_text))
            }

            Button(onClick = {
                selectedVariety = null
                name = ""
                cultivar = ""
            }) {
                Text(stringResource(id = R.string.new_button_text))
            }

            Button(
                onClick = {
                    selectedVariety?.let {
                        viewModel.deleteVariety(it)
                        selectedVariety = null
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
