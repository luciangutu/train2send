package com.train4send.ui.screens.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train4send.Train4SendApp
import com.train4send.data.model.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanSetupScreen(
    planId: String?,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as Train4SendApp
    val scope = rememberCoroutineScope()

    var planTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(planId != null) }
    var existingPlan by remember { mutableStateOf<TrainingPlanEntity?>(null) }

    // Day configuration state
    var dayConfigs by remember {
        mutableStateOf(
            (1..7).map { dow ->
                DayConfig(
                    dayOfWeek = dow,
                    enabled = dow <= 5, // Mon-Fri enabled by default
                    title = getDefaultDayTitle(dow)
                )
            }
        )
    }

    // Load existing plan if editing
    LaunchedEffect(planId) {
        if (planId != null) {
            val plan = app.trainingPlanRepository.getPlanById(planId)
            if (plan != null) {
                existingPlan = plan
                planTitle = plan.title
            }
            isLoading = false
        }
    }

    // Load existing days when plan is loaded
    val existingDays by existingPlan?.let {
        app.trainingPlanRepository.getDaysForPlan(it.id)
    }?.collectAsStateWithLifecycle(initialValue = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    // Sync loaded days into config
    LaunchedEffect(existingDays) {
        if (existingDays.isNotEmpty()) {
            dayConfigs = (1..7).map { dow ->
                val existing = existingDays.find { it.dayOfWeek == dow }
                DayConfig(
                    dayOfWeek = dow,
                    enabled = existing != null,
                    title = existing?.dayTitle ?: getDefaultDayTitle(dow),
                    existingId = existing?.id
                )
            }
        }
    }

    // Exercises for adding to days
    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showExercisePicker by remember { mutableStateOf(false) }
    var exercisePickerDay by remember { mutableStateOf<DayConfig?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (planId == null) "Create Plan" else "Edit Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                savePlan(
                                    app = app,
                                    existingPlan = existingPlan,
                                    title = planTitle,
                                    dayConfigs = dayConfigs
                                )
                                navController.popBackStack()
                            }
                        },
                        enabled = planTitle.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Plan name
                item {
                    OutlinedTextField(
                        value = planTitle,
                        onValueChange = { planTitle = it },
                        label = { Text("Plan Name") },
                        placeholder = { Text("e.g., Climbing Season Prep") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Training Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toggle days on/off and set a focus for each day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(dayConfigs) { config ->
                    DayConfigCard(
                        config = config,
                        onToggle = { enabled ->
                            dayConfigs = dayConfigs.map {
                                if (it.dayOfWeek == config.dayOfWeek) it.copy(enabled = enabled)
                                else it
                            }
                        },
                        onTitleChange = { title ->
                            dayConfigs = dayConfigs.map {
                                if (it.dayOfWeek == config.dayOfWeek) it.copy(title = title)
                                else it
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Quick templates
                    Text(
                        text = "Quick Templates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                planTitle = "Climbing Performance"
                                dayConfigs = listOf(
                                    DayConfig(1, true, "Strength & Power"),
                                    DayConfig(2, true, "Endurance"),
                                    DayConfig(3, true, "Power Endurance"),
                                    DayConfig(4, false, "Rest"),
                                    DayConfig(5, true, "Strength"),
                                    DayConfig(6, true, "Mobility & Recovery"),
                                    DayConfig(7, false, "Rest")
                                )
                            },
                            label = { Text("Climbing") }
                        )
                        AssistChip(
                            onClick = {
                                planTitle = "General Fitness"
                                dayConfigs = listOf(
                                    DayConfig(1, true, "Upper Body"),
                                    DayConfig(2, true, "Cardio & Core"),
                                    DayConfig(3, true, "Lower Body"),
                                    DayConfig(4, false, "Rest"),
                                    DayConfig(5, true, "Full Body"),
                                    DayConfig(6, true, "Mobility"),
                                    DayConfig(7, false, "Rest")
                                )
                            },
                            label = { Text("General") }
                        )
                        AssistChip(
                            onClick = {
                                planTitle = "3-Day Minimum"
                                dayConfigs = listOf(
                                    DayConfig(1, true, "Strength"),
                                    DayConfig(2, false, "Rest"),
                                    DayConfig(3, true, "Power Endurance"),
                                    DayConfig(4, false, "Rest"),
                                    DayConfig(5, true, "Endurance"),
                                    DayConfig(6, false, "Rest"),
                                    DayConfig(7, false, "Rest")
                                )
                            },
                            label = { Text("3-Day") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayConfigCard(
    config: DayConfig,
    onToggle: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit
) {
    val dayName = DayOfWeek.of(config.dayOfWeek)
        .getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = if (config.enabled) CardDefaults.cardColors()
        else CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = config.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (config.enabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = config.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true,
                        placeholder = { Text("Session focus...") }
                    )
                } else {
                    Text(
                        text = "Rest Day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class DayConfig(
    val dayOfWeek: Int,
    val enabled: Boolean,
    val title: String,
    val existingId: String? = null
)

private fun getDefaultDayTitle(dayOfWeek: Int): String = when (dayOfWeek) {
    1 -> "Strength & Power"
    2 -> "Endurance"
    3 -> "Power Endurance"
    4 -> "Rest"
    5 -> "Strength"
    6 -> "Mobility & Recovery"
    7 -> "Rest"
    else -> ""
}

private suspend fun savePlan(
    app: Train4SendApp,
    existingPlan: TrainingPlanEntity?,
    title: String,
    dayConfigs: List<DayConfig>
) {
    // If this is the first plan ever, make it active
    val isFirstPlan = existingPlan == null
    val plan = existingPlan?.copy(title = title)
        ?: TrainingPlanEntity(title = title, isActive = isFirstPlan)

    if (existingPlan != null) {
        app.trainingPlanRepository.updatePlan(plan)
    } else {
        app.trainingPlanRepository.insertPlan(plan)
    }

    // Sync days: delete removed, update existing, insert new
    val enabledDays = dayConfigs.filter { it.enabled }

    // Delete days that were disabled
    dayConfigs.filter { !it.enabled && it.existingId != null }.forEach { config ->
        app.trainingPlanRepository.deletePlanDay(
            PlanDayEntity(
                id = config.existingId!!,
                planId = plan.id,
                dayOfWeek = config.dayOfWeek,
                dayTitle = config.title
            )
        )
    }

    // Upsert enabled days
    enabledDays.forEach { config ->
        val day = PlanDayEntity(
            id = config.existingId ?: java.util.UUID.randomUUID().toString(),
            planId = plan.id,
            dayOfWeek = config.dayOfWeek,
            dayTitle = config.title
        )
        app.trainingPlanRepository.insertPlanDay(day)
    }
}
