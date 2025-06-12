package com.example.tailorbillpro.ui.screens.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorbillpro.data.AppDatabase
import com.example.tailorbillpro.data.entity.BillEntity
import com.example.tailorbillpro.data.entity.BillItemEntity
import com.example.tailorbillpro.data.entity.ClientEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BillItem(
    val id: Long = 0,
    val serviceName: String,
    val quantity: Int,
    val price: Double
)

data class BillingUiState(
    val clients: List<ClientEntity> = emptyList(),
    val selectedClient: ClientEntity? = null,
    val items: List<BillItem> = emptyList(),
    val totalAmount: Double = 0.0
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState

    init {
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            database.clientDao().getAllClients().collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
    }

    fun selectClient(client: ClientEntity) {
        _uiState.update { it.copy(selectedClient = client) }
    }

    fun addClient(name: String) {
        viewModelScope.launch {
            val client = ClientEntity(name = name)
            database.clientDao().insertClient(client)
        }
    }

    fun incrementQuantity(item: BillItem) {
        _uiState.update { state ->
            val updatedItems = state.items.map { 
                if (it == item) it.copy(quantity = it.quantity + 1)
                else it
            }
            state.copy(
                items = updatedItems,
                totalAmount = calculateTotal(updatedItems)
            )
        }
    }

    fun decrementQuantity(item: BillItem) {
        if (item.quantity <= 1) return
        _uiState.update { state ->
            val updatedItems = state.items.map { 
                if (it == item) it.copy(quantity = it.quantity - 1)
                else it
            }
            state.copy(
                items = updatedItems,
                totalAmount = calculateTotal(updatedItems)
            )
        }
    }

    fun removeItem(item: BillItem) {
        _uiState.update { state ->
            val updatedItems = state.items.filter { it != item }
            state.copy(
                items = updatedItems,
                totalAmount = calculateTotal(updatedItems)
            )
        }
    }

    private fun calculateTotal(items: List<BillItem>): Double {
        return items.sumOf { it.price * it.quantity }
    }

    fun generateBill() {
        viewModelScope.launch {
            val client = _uiState.value.selectedClient ?: return@launch
            val items = _uiState.value.items
            val totalAmount = calculateTotal(items)

            // Create bill
            val billId = database.billDao().insertBill(
                BillEntity(
                    clientId = client.id,
                    date = Date(),
                    totalAmount = totalAmount
                )
            )

            // Create bill items
            items.forEach { item ->
                database.billItemDao().insertBillItem(
                    BillItemEntity(
                        billId = billId,
                        serviceName = item.serviceName,
                        quantity = item.quantity,
                        price = item.price
                    )
                )
            }

            // Reset state
            _uiState.update {
                BillingUiState(clients = it.clients)
            }
        }
    }
}
