package com.train4send.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train4send.Train4SendApp
import com.train4send.data.model.ExerciseEntity
import com.train4send.data.model.PlannedExerciseEntity
import com.train4send.ui.navigation.Screen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as Train4SendApp
    val activePlan by app.trainingPlanRepository.getActivePlan()
        .collectAsStateWithLifecycle(initialValue = null)

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Train4Send", fontWeight = FontWeight.Bold)
                        Text(
                            text = "v$versionName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Plan",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.WeeklyPlan.route) }
                )
                QuickActionCard(
                    title = "Exercises",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Exercises.route) }
                )
                QuickActionCard(
                    title = "Timer",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Timer.route) }
                )
                QuickActionCard(
                    title = "Backup",
                    icon = Icons.Default.CloudUpload,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Backup.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Session Card (if plan exists)
            activePlan?.let { plan ->
                TodaySessionCard(
                    planId = plan.id,
                    app = app,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun TodaySessionCard(
    planId: String,
    app: Train4SendApp,
    navController: NavController
) {
    val planDays by app.trainingPlanRepository.getDaysForPlan(planId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val todayDow = LocalDate.now().dayOfWeek.value
    val todayPlanDay = planDays.find { it.dayOfWeek == todayDow }

    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val exerciseMap = allExercises.associateBy { it.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.WeeklyPlan.route) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TODAY'S SESSION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (todayPlanDay != null) {
                val exercises by app.trainingPlanRepository
                    .getExercisesForDay(todayPlanDay.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                Text(
                    text = todayPlanDay.dayTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                val estimatedMin = if (exercises.isEmpty()) 0 else {
                    exercises.sumOf { ex ->
                        val work = ex.customDurationSec ?: 60
                        val rest = ex.customRestSec ?: 90
                        val sets = ex.customSets ?: 3
                        (work + rest) * sets
                    } / 60
                }
                Text(
                    text = "${exercises.size} exercises · ~$estimatedMin min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                if (exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    exercises.forEach { planned ->
                        val exercise = exerciseMap[planned.exerciseId]
                        ExerciseItemMini(
                            planned = planned,
                            exercise = exercise,
                            onClick = {
                                exercise?.id?.let { id ->
                                    navController.navigate(Screen.ExerciseDetail.createRoute(id))
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else {
                Text(
                    text = "Rest Day 🧘",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No training scheduled for today. Take some time to recover!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ExerciseItemMini(
    planned: PlannedExerciseEntity,
    exercise: ExerciseEntity?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise?.name ?: "Unknown Exercise",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            exercise?.let {
                Text(
                    text = it.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }

        val sets = planned.customSets ?: exercise?.defaultSets
        val reps = planned.customReps ?: exercise?.defaultReps
        val duration = planned.customDurationSec ?: exercise?.defaultDurationSec

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (sets != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$sets",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = " × ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            val detail = when {
                reps != null -> "$reps reps"
                duration != null -> "${duration}s"
                else -> "--"
            }

            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}


