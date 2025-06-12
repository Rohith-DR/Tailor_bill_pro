package com.example.tailorbillpro.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorbillpro.data.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class BillItem(
    val serviceName: String,
    val quantity: Int,
    val price: Double
)

data class BillUiState(
    val id: Long,
    val clientName: String,
    val date: String,
    val totalAmount: Double,
    val items: List<BillItem>
)

data class DashboardUiState(
    val bills: List<BillUiState> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadBills()
    }

    private fun loadBills() {
        viewModelScope.launch {
            database.billDao().getAllBillsWithDetails().collect { bills ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val billUiStates = bills.map { bill ->
                    BillUiState(
                        id = bill.bill.id,
                        clientName = bill.client.name,
                        date = dateFormat.format(bill.bill.date),
                        totalAmount = bill.bill.totalAmount,
                        items = bill.items.map { item ->
                            BillItem(
                                serviceName = item.serviceName,
                                quantity = item.quantity,
                                price = item.price
                            )
                        }
                    )
                }
                _uiState.update { it.copy(bills = billUiStates) }
            }
        }
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            database.billDao().deleteBill(billId)
            loadBills() // Reload data after deletion
        }
    }
}
