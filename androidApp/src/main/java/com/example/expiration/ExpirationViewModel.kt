package com.example.expiration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpirationViewModel : ViewModel() {
    private val _productionDate = MutableStateFlow("")
    val productionDate: StateFlow<String> = _productionDate.asStateFlow()

    private val _duration = MutableStateFlow("")
    val duration: StateFlow<String> = _duration.asStateFlow()

    private val _unit = MutableStateFlow(UnitType.DAYS)
    val unit: StateFlow<UnitType> = _unit.asStateFlow()

    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result.asStateFlow()

    fun updateProductionDate(raw: String) {
        val formatted = formatDateInput(raw)
        _productionDate.value = formatted
        recalc()
    }

    fun updateDuration(raw: String) {
        _duration.value = raw.filter { it.isDigit() }.take(4)
        recalc()
    }

    fun updateUnit(newUnit: UnitType) {
        _unit.value = newUnit
        recalc()
    }

    private fun recalc() {
        viewModelScope.launch {
            val date = _productionDate.value
            val dur = _duration.value.toIntOrNull()
            val unit = _unit.value
            _result.value = if (date.isNotBlank() && dur != null) {
                val expiration = calculateExpiration(date, dur, unit)
                val (status, days) = getStatus(expiration)
                Result(expiration, status, days)
            } else null
        }
    }
}

