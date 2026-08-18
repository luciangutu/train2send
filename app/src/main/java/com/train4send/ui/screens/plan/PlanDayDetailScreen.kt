package com.train4send.ui.screens.plan

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train4send.Train4SendApp
import com.train4send.data.model.ExerciseEntity
import com.train4send.data.model.ExerciseSection
import com.train4send.data.model.PlanDayEntity
import com.train4send.data.model.PlannedExerciseEntity
import com.train4send.ui.navigation.Screen
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDayDetailScreen(
    planDayId: String,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as Train4SendApp

    // Load the plan day info
    val activePlan by app.trainingPlanRepository.getActivePlan()
        .collectAsStateWithLifecycle(initialValue = null)

    val planDays by activePlan?.let {
        app.trainingPlanRepository.getDaysForPlan(it.id)
    }?.collectAsStateWithLifecycle(initialValue = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val planDay = planDays.find { it.id == planDayId }

    val plannedExercises by app.trainingPlanRepository
        .getExercisesForDay(planDayId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val exerciseMap = allExercises.associateBy { it.id }

    val dayName = planDay?.let {
        DayOfWeek.of(it.dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())
    } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(planDay?.dayTitle ?: "Day Detail")
                        if (dayName.isNotEmpty()) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Timer.route) }) {
                        Icon(Icons.Default.Timer, contentDescription = "Start Timer")
                    }
                }
            )
        }
    ) { padding ->
        if (plannedExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No exercises planned",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Edit the plan to add exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group by section
                val mainExercises = plannedExercises.filter { it.section == ExerciseSection.MAIN }
                val secondaryExercises = plannedExercises.filter { it.section == ExerciseSection.SECONDARY }
                val complementaryExercises = plannedExercises.filter { it.section == ExerciseSection.COMPLEMENTARY }

                if (mainExercises.isNotEmpty()) {
                    item {
                        SectionHeader("Main Exercises")
                    }
                    items(mainExercises) { planned ->
                        PlannedExerciseCard(planned, exerciseMap[planned.exerciseId])
                    }
                }

                if (secondaryExercises.isNotEmpty()) {
                    item {
                        SectionHeader("Secondary")
                    }
                    items(secondaryExercises) { planned ->
                        PlannedExerciseCard(planned, exerciseMap[planned.exerciseId])
                    }
                }

                if (complementaryExercises.isNotEmpty()) {
                    item {
                        SectionHeader("Complementary")
                    }
                    items(complementaryExercises) { planned ->
                        PlannedExerciseCard(planned, exerciseMap[planned.exerciseId])
                    }
                }

                // Duration summary
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalMin = estimateDuration(plannedExercises)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Estimated duration: ~$totalMin min",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun PlannedExerciseCard(
    planned: PlannedExerciseEntity,
    exercise: ExerciseEntity?
) {
    val categoryColor = exercise?.let {
        Color(android.graphics.Color.parseColor(it.category.colorHex))
    } ?: MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp, 40.dp)
                    .background(categoryColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise?.name ?: "Unknown Exercise",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                exercise?.let {
                    Text(
                        text = it.category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val sets = planned.customSets ?: exercise?.defaultSets
                val reps = planned.customReps ?: exercise?.defaultReps
                val duration = planned.customDurationSec ?: exercise?.defaultDurationSec
                val rest = planned.customRestSec ?: exercise?.defaultRestSec

                sets?.let {
                    Text("$it sets", style = MaterialTheme.typography.labelSmall)
                }
                reps?.let {
                    Text("$it reps", style = MaterialTheme.typography.labelSmall)
                }
                duration?.let {
                    Text("${it}s work", style = MaterialTheme.typography.labelSmall)
                }
                rest?.let {
                    Text("${it}s rest", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun estimateDuration(exercises: List<PlannedExerciseEntity>): Int {
    if (exercises.isEmpty()) return 0
    return exercises.sumOf { ex ->
        val work = ex.customDurationSec ?: 60
        val rest = ex.customRestSec ?: 90
        val sets = ex.customSets ?: 3
        (work + rest) * sets
    } / 60
}
