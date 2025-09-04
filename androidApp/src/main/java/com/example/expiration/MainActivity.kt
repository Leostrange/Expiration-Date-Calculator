package com.example.expiration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(vm: ExpirationViewModel = viewModel()) {
    val context = LocalContext.current
    val themePref = remember { ThemePreference(context) }
    val scope = rememberCoroutineScope()
    var isDarkMode by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        themePref.isDarkFlow.collect { isDark -> isDarkMode = isDark }
    }

    MaterialTheme(colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = stringResource(id = R.string.title)) },
                    actions = {
                        IconButton(onClick = {
                            val newTheme = !isDarkMode
                            isDarkMode = newTheme
                            scope.launch { themePref.setDark(newTheme) }
                        }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Content(vm = vm, modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun Content(vm: ExpirationViewModel, modifier: Modifier = Modifier) {
    val productionDate by vm.productionDate.collectAsState()
    val duration by vm.duration.collectAsState()
    val unit by vm.unit.collectAsState()
    val result by vm.result.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var showDatePicker by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = productionDate,
            onValueChange = { vm.updateProductionDate(it) },
            label = { Text(text = stringResource(id = R.string.production_date)) },
            placeholder = { Text("ДД.ММ.ГГГГ") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.Brightness7, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                            val year = cal.get(Calendar.YEAR).toString()
                            vm.updateProductionDate("$day.$month.$year")
                        }
                        showDatePicker = false
                    }) { Text(text = "OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(text = "Отмена") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = duration,
            onValueChange = { vm.updateDuration(it) },
            label = { Text(text = stringResource(id = R.string.shelf_life)) },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        UnitSelector(unit = unit, onChange = { vm.updateUnit(it) })

        Spacer(modifier = Modifier.height(16.dp))

        if (result != null) {
            ResultCard(result)
        }
    }
}

@Composable
fun UnitSelector(unit: UnitType, onChange: (UnitType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = when (unit) {
                UnitType.DAYS -> stringResource(R.string.days)
                UnitType.WEEKS -> stringResource(R.string.weeks)
                UnitType.MONTHS -> stringResource(R.string.months)
                UnitType.YEARS -> stringResource(R.string.years)
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.unit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.days)) },
                onClick = { onChange(UnitType.DAYS); expanded = false }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.weeks)) },
                onClick = { onChange(UnitType.WEEKS); expanded = false }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.months)) },
                onClick = { onChange(UnitType.MONTHS); expanded = false }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.years)) },
                onClick = { onChange(UnitType.YEARS); expanded = false }
            )
        }
    }
}

data class Result(val expirationDate: String, val status: Status, val daysRemaining: Int)

enum class UnitType { DAYS, WEEKS, MONTHS, YEARS }

enum class Status { FRESH, EXPIRING, EXPIRED }

fun formatDateInput(value: String): String {
    val digits = value.filter { it.isDigit() }.take(8)
    return when {
        digits.length <= 2 -> digits
        digits.length <= 4 -> digits.substring(0, 2) + "." + digits.substring(2)
        else -> digits.substring(0, 2) + "." + digits.substring(2, 4) + "." + digits.substring(4)
    }
}

fun calculateExpiration(productionDate: String, duration: Int, unit: UnitType): String {
    val parts = productionDate.split(".")
    if (parts.size != 3) return ""
    val day = parts[0].toIntOrNull() ?: return ""
    val month = parts[1].toIntOrNull() ?: return ""
    val year = parts[2].toIntOrNull() ?: return ""

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, day)

    when (unit) {
        UnitType.DAYS -> cal.add(Calendar.DAY_OF_MONTH, duration)
        UnitType.WEEKS -> cal.add(Calendar.DAY_OF_MONTH, duration * 7)
        UnitType.MONTHS -> cal.add(Calendar.MONTH, duration)
        UnitType.YEARS -> cal.add(Calendar.YEAR, duration)
    }

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
    return sdf.format(cal.time)
}

fun getStatus(expirationDate: String): Pair<Status, Int> {
    return try {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
        val expDate: Date = sdf.parse(expirationDate)!!
        val today = Calendar.getInstance().time
        val diff = expDate.time - today.time
        val days = kotlin.math.ceil(diff / (1000.0 * 60 * 60 * 24)).toInt()
        val status = when {
            days < 0 -> Status.EXPIRED
            days <= 7 -> Status.EXPIRING
            else -> Status.FRESH
        }
        status to days
    } catch (e: Exception) {
        Status.EXPIRED to -1
    }
}

@Composable
fun ResultCard(result: Result) {
    val statusText = when (result.status) {
        Status.FRESH -> stringResource(R.string.status_fresh)
        Status.EXPIRING -> stringResource(R.string.status_expiring)
        Status.EXPIRED -> stringResource(R.string.status_expired)
    }

    val statusColor = when (result.status) {
        Status.FRESH -> MaterialTheme.colorScheme.primary
        Status.EXPIRING -> MaterialTheme.colorScheme.tertiary
        Status.EXPIRED -> MaterialTheme.colorScheme.error
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.expires_on, result.expirationDate))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (result.daysRemaining >= 0)
                    stringResource(id = R.string.days_left, result.daysRemaining)
                else stringResource(id = R.string.days_overdue, kotlin.math.abs(result.daysRemaining))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Статус: ", style = MaterialTheme.typography.titleMedium)
                Text(text = statusText, color = statusColor, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

