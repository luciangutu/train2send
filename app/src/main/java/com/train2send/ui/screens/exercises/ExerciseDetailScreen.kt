package com.train2send.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.model.ExerciseEntity
import com.train2send.data.model.PlannedExerciseEntity
import com.train2send.ui.navigation.Screen
import com.train2send.utils.calculateExerciseDuration
import com.train2send.utils.formatDuration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    plannedExerciseId: String? = null,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    val scope = rememberCoroutineScope()
    
    val exerciseState = produceState<ExerciseEntity?>(initialValue = null) {
        value = app.exerciseRepository.getExerciseById(exerciseId)
    }
    val exercise = exerciseState.value

    val plannedState = produceState<PlannedExerciseEntity?>(initialValue = null) {
        if (plannedExerciseId != null) {
            value = app.trainingPlanRepository.getPlannedExerciseById(plannedExerciseId)
        }
    }
    val planned = plannedState.value

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val work = planned?.customDurationSec ?: exercise?.defaultDurationSec
                        val restRep = planned?.customRestSec ?: exercise?.defaultRestSec
                        val reps = planned?.customReps ?: exercise?.defaultReps
                        val sets = planned?.customSets ?: exercise?.defaultSets
                        val restSet = planned?.customRestBetweenSetsSec ?: exercise?.defaultRestBetweenSetsSec
                        
                        navController.navigate(Screen.Timer.createRoute(
                            work = work,
                            restRep = restRep,
                            reps = reps,
                            sets = sets,
                            restSet = restSet
                        ))
                    }) {
                        Icon(Icons.Default.Timer, contentDescription = "Start Timer")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.ExerciseEdit.createRoute(exerciseId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (exercise == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ExerciseDetailContent(
                exercise = exercise,
                planned = planned,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showDeleteConfirm && exercise != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Exercise") },
            text = { Text("Delete \"${exercise.name}\"? This will also remove it from all training plans.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            app.exerciseRepository.deleteExercise(exercise)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExerciseDetailContent(
    exercise: ExerciseEntity,
    planned: PlannedExerciseEntity?,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(android.graphics.Color.parseColor(exercise.category.colorHex))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = exercise.category.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (planned != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Planned for Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlannedValuesSection(planned, exercise)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Sections
        InfoSection(
            title = "Description",
            content = exercise.description ?: "No description provided.",
            icon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Default Values",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailBox(
                label = "Sets",
                value = exercise.defaultSets?.toString() ?: "--",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f)
            )
            DetailBox(
                label = "Reps",
                value = exercise.defaultReps?.toString() ?: "--",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailBox(
                label = "Work Rep",
                value = exercise.defaultDurationSec?.let { formatDuration(it) } ?: "--",
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
            DetailBox(
                label = "Rest Rep",
                value = exercise.defaultRestSec?.let { formatDuration(it) } ?: "--",
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailBox(
                label = "Rest Set",
                value = exercise.defaultRestBetweenSetsSec?.let { formatDuration(it) } ?: "--",
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
            val totalSec = calculateExerciseDuration(
                sets = exercise.defaultSets,
                reps = exercise.defaultReps,
                workRepSec = exercise.defaultDurationSec,
                restRepSec = exercise.defaultRestSec,
                restSetSec = exercise.defaultRestBetweenSetsSec
            )
            DetailBox(
                label = "Total Duration",
                value = formatDuration(totalSec),
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlannedValuesSection(planned: PlannedExerciseEntity, exercise: ExerciseEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailBox(
                    label = "Sets",
                    value = (planned.customSets ?: exercise.defaultSets)?.toString() ?: "--",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                DetailBox(
                    label = "Reps",
                    value = (planned.customReps ?: exercise.defaultReps)?.toString() ?: "--",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailBox(
                    label = "Work",
                    value = (planned.customDurationSec ?: exercise.defaultDurationSec)?.let { formatDuration(it) } ?: "--",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                DetailBox(
                    label = "Rest (Set)",
                    value = (planned.customRestBetweenSetsSec ?: exercise.defaultRestBetweenSetsSec)?.let { formatDuration(it) } ?: "--",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }
            if (planned.customRestSec != null || exercise.defaultRestSec != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailBox(
                    label = "Rest (Rep)",
                    value = (planned.customRestSec ?: exercise.defaultRestSec)?.let { formatDuration(it) } ?: "--",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, content: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(if (compact) 12.dp else 16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 16.dp else 20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = if (compact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
