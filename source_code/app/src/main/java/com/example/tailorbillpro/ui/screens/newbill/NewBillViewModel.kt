package com.example.tailorbillpro.ui.screens.newbill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorbillpro.data.AppDatabase
import com.example.tailorbillpro.data.entity.BillEntity
import com.example.tailorbillpro.data.entity.BillItemEntity
import com.example.tailorbillpro.data.entity.ClientEntity
import com.example.tailorbillpro.data.entity.ServiceEntity
import com.example.tailorbillpro.utils.BillImageGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class SelectedService(
    val service: ServiceEntity,
    val quantity: Int
)

data class NewBillUiState(
    val clientName: String = "",
    val selectedServices: List<SelectedService> = emptyList(),
    val availableServices: List<ServiceEntity> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isListening: Boolean = false
)

@HiltViewModel
class NewBillViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewBillUiState())
    val uiState: StateFlow<NewBillUiState> = _uiState.asStateFlow()
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            database.serviceDao().getAllServices().collect { services ->
                _uiState.update { it.copy(availableServices = services) }
            }
        }
    }

    fun updateClientName(name: String) {
        _uiState.update { it.copy(clientName = name) }
    }

    fun addService(service: ServiceEntity, quantity: Int) {
        val currentServices = _uiState.value.selectedServices.toMutableList()
        val existingIndex = currentServices.indexOfFirst { it.service.id == service.id }

        if (existingIndex != -1) {
            currentServices[existingIndex] = currentServices[existingIndex].copy(quantity = quantity)
        } else {
            currentServices.add(SelectedService(service, quantity))
        }

        val totalAmount = currentServices.sumOf { it.service.price * it.quantity }
        _uiState.update {
            it.copy(
                selectedServices = currentServices,
                totalAmount = totalAmount
            )
        }
    }

    fun removeService(serviceId: Long) {
        val currentServices = _uiState.value.selectedServices.filter { it.service.id != serviceId }
        val totalAmount = currentServices.sumOf { it.service.price * it.quantity }
        _uiState.update {
            it.copy(
                selectedServices = currentServices,
                totalAmount = totalAmount
            )
        }
    }

    suspend fun generateBill(context: Context): String? {
        if (_uiState.value.clientName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter client name") }
            return null
        }

        if (_uiState.value.selectedServices.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add at least one service") }
            return null
        }

        _uiState.update { it.copy(isLoading = true) }

        return try {
            // Insert or get client
            val clientId = database.clientDao().getClientByName(_uiState.value.clientName)?.id
                ?: database.clientDao().insertClient(
                    ClientEntity(name = _uiState.value.clientName)
                )

            // Create bill
            val billId = database.billDao().insertBill(
                BillEntity(
                    clientId = clientId,
                    date = Date(),
                    totalAmount = _uiState.value.totalAmount
                )
            )

            // Insert bill items
            _uiState.value.selectedServices.forEach { selectedService ->
                database.billItemDao().insertBillItem(
                    BillItemEntity(
                        billId = billId,
                        serviceName = selectedService.service.name,
                        quantity = selectedService.quantity,
                        price = selectedService.service.price
                    )
                )
            }

            // Generate bill image
            val bill = database.billDao().getBillById(billId)!!
            val items = _uiState.value.selectedServices.map { selected ->
                BillItemEntity(
                    billId = billId,
                    serviceName = selected.service.name,
                    quantity = selected.quantity,
                    price = selected.service.price
                )
            }

            BillImageGenerator.generateBillImage(
                context,
                bill,
                items,
                _uiState.value.clientName
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to generate bill: ${e.message}"
                )
            }
            null
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun startSpeechRecognition(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.update { 
                it.copy(
                    isListening = false,
                    errorMessage = "Speech recognition is not available on this device"
                )
            }
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        _uiState.update { it.copy(isListening = true) }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _uiState.update { it.copy(isListening = true) }
            }
            
            override fun onBeginningOfSpeech() {}
            
            override fun onRmsChanged(rmsdB: Float) {}
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                _uiState.update { it.copy(isListening = false) }
            }
            
            override fun onError(error: Int) {
                _uiState.update { 
                    it.copy(
                        isListening = false,
                        errorMessage = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                            else -> "Speech recognition error"
                        }
                    )
                }
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    updateClientName(matches[0])
                }
                _uiState.update { it.copy(isListening = false) }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {}
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the client name")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        try {
            speechRecognizer?.startListening(recognizerIntent)
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isListening = false,
                    errorMessage = "Failed to start speech recognition"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
