package com.example.tailorbillpro.ui.screens.newbill

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBillScreen(
    onNavigateUp: () -> Unit,
    onBillGenerated: () -> Unit,
    viewModel: NewBillViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showServiceDialog by remember { mutableStateOf(false) }
    var showQuantityDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startSpeechRecognition(context)
        } else {
            Toast.makeText(
                context,
                "Voice input requires microphone permission",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Client Name Section
            OutlinedTextField(
                value = uiState.clientName,
                onValueChange = { viewModel.updateClientName(it) },
                label = { Text("Client Name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) -> {
                                    viewModel.startSpeechRecognition(context)
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (uiState.isListening) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Services Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Services", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showServiceDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Service")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.selectedServices.isEmpty()) {
                        Text(
                            "No services added",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn {
                            items(uiState.selectedServices) { selectedService ->
                                ListItem(
                                    headlineContent = {
                                        Text(selectedService.service.name)
                                    },
                                    supportingContent = {
                                        Text("Quantity: ${selectedService.quantity}")
                                    },
                                    trailingContent = {
                                        Text("₹${selectedService.service.price * selectedService.quantity}")
                                    },
                                    modifier = Modifier.clickable {
                                        showQuantityDialog = selectedService.service.id to selectedService.service.name
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Amount
            Text(
                "Total Amount: ₹${uiState.totalAmount}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            // Generate Bill Button
            Button(
                onClick = {
                    scope.launch {
                        viewModel.generateBill(context)?.let {
                            onBillGenerated()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Generating..." else "Generate Bill")
            }
        }

        // Service Selection Dialog
        if (showServiceDialog) {
            AlertDialog(
                onDismissRequest = { showServiceDialog = false },
                title = { Text("Select Service") },
                text = {
                    LazyColumn {
                        items(uiState.availableServices) { service ->
                            ListItem(
                                headlineContent = { Text(service.name) },
                                supportingContent = { Text("₹${service.price}") },
                                modifier = Modifier.clickable {
                                    showQuantityDialog = service.id to service.name
                                    showServiceDialog = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showServiceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Quantity Dialog
        showQuantityDialog?.let { (serviceId, serviceName) ->
            var quantity by remember { mutableStateOf("1") }
            AlertDialog(
                onDismissRequest = { showQuantityDialog = null },
                title = { Text("Enter Quantity for $serviceName") },
                text = {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) quantity = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Quantity") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val service = uiState.availableServices.find { it.id == serviceId }
                            service?.let {
                                viewModel.addService(it, quantity.toIntOrNull() ?: 1)
                            }
                            showQuantityDialog = null
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuantityDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
