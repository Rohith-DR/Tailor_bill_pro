package com.example.tailorbillpro.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tailorbillpro.data.entity.ServiceEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddServiceDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Services",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "₹${service.price}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { viewModel.showEditServiceDialog(service) }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Service")
                                }
                                IconButton(
                                    onClick = { viewModel.showDeleteServiceDialog(service) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Service")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Service Dialog (Add/Edit)
        if (uiState.showServiceDialog) {
            ServiceDialog(
                service = uiState.editingService,
                onDismiss = { viewModel.hideServiceDialog() },
                onConfirm = { name, price ->
                    if (uiState.editingService != null) {
                        viewModel.updateService(name, price)
                    } else {
                        viewModel.addService(name, price)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteDialog() },
                title = { Text("Delete Service") },
                text = { Text("Are you sure you want to delete this service?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteService()
                            viewModel.hideDeleteDialog()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDialog(
    service: ServiceEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double) -> Unit
) {
    var serviceName by remember { mutableStateOf(service?.name ?: "") }
    var servicePrice by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service == null) "Add Service" else "Edit Service") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = servicePrice,
                    onValueChange = { servicePrice = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (showError) {
                    Text(
                        text = "Please fill all fields correctly",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = servicePrice.toDoubleOrNull()
                    if (serviceName.isNotBlank() && price != null && price > 0) {
                        onConfirm(serviceName, price)
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
