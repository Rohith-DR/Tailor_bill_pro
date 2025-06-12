package com.example.tailorbillpro.ui.screens.billing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tailorbillpro.data.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    var showClientDialog by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var newClientName by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Client Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Client",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedClient?.name ?: "Select Client",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row {
                            IconButton(onClick = { showAddClientDialog = true }) {
                                Icon(Icons.Default.PersonAdd, "Add new client")
                            }
                            IconButton(onClick = { showClientDialog = true }) {
                                Icon(Icons.Default.Person, "Select client")
                            }
                        }
                    }
                }
            }

            // Bill Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    text = item.serviceName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "₹${item.price}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.decrementQuantity(item) },
                                    enabled = item.quantity > 1
                                ) {
                                    Icon(Icons.Default.Remove, "Decrease quantity")
                                }
                                Text(
                                    text = "${item.quantity}",
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { viewModel.incrementQuantity(item) }) {
                                    Icon(Icons.Default.Add, "Increase quantity")
                                }
                                IconButton(onClick = { viewModel.removeItem(item) }) {
                                    Icon(Icons.Default.Delete, "Remove item")
                                }
                            }
                        }
                    }
                }
            }

            // Total Amount
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "₹${uiState.totalAmount}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Generate Bill Button
            Button(
                onClick = { viewModel.generateBill() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState.selectedClient != null && uiState.items.isNotEmpty()
            ) {
                Text("Generate Bill")
            }
        }

        // Client Selection Dialog
        if (showClientDialog) {
            AlertDialog(
                onDismissRequest = { showClientDialog = false },
                title = { Text("Select Client") },
                text = {
                    LazyColumn {
                        items(uiState.clients) { client ->
                            TextButton(
                                onClick = {
                                    viewModel.selectClient(client)
                                    showClientDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(client.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showClientDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Client Dialog
        if (showAddClientDialog) {
            AlertDialog(
                onDismissRequest = { showAddClientDialog = false },
                title = { Text("Add New Client") },
                text = {
                    OutlinedTextField(
                        value = newClientName,
                        onValueChange = { newClientName = it },
                        label = { Text("Client Name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newClientName.isNotBlank()) {
                                viewModel.addClient(newClientName)
                                newClientName = ""
                                showAddClientDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddClientDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
