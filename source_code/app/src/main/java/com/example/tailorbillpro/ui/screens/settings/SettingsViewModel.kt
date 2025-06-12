package com.example.tailorbillpro.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorbillpro.data.AppDatabase
import com.example.tailorbillpro.data.entity.ServiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val services: List<ServiceEntity> = emptyList(),
    val showServiceDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingService: ServiceEntity? = null,
    val serviceToDelete: ServiceEntity? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            database.serviceDao().getAllServices().collect { services ->
                _uiState.update { it.copy(services = services) }
            }
        }
    }

    fun showAddServiceDialog() {
        _uiState.update { it.copy(
            showServiceDialog = true,
            editingService = null
        ) }
    }

    fun showEditServiceDialog(service: ServiceEntity) {
        _uiState.update { it.copy(
            showServiceDialog = true,
            editingService = service
        ) }
    }

    fun hideServiceDialog() {
        _uiState.update { it.copy(
            showServiceDialog = false,
            editingService = null
        ) }
    }

    fun showDeleteServiceDialog(service: ServiceEntity) {
        _uiState.update { it.copy(
            showDeleteDialog = true,
            serviceToDelete = service
        ) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(
            showDeleteDialog = false,
            serviceToDelete = null
        ) }
    }

    fun addService(name: String, price: Double) {
        viewModelScope.launch {
            database.serviceDao().insertService(
                ServiceEntity(
                    name = name,
                    price = price
                )
            )
            hideServiceDialog()
        }
    }

    fun updateService(name: String, price: Double) {
        viewModelScope.launch {
            _uiState.value.editingService?.let { service ->
                database.serviceDao().updateService(
                    service.copy(
                        name = name,
                        price = price
                    )
                )
            }
            hideServiceDialog()
        }
    }

    fun deleteService() {
        viewModelScope.launch {
            _uiState.value.serviceToDelete?.let { service ->
                database.serviceDao().deleteService(service)
            }
        }
    }
}
