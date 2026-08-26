package com.train2send.ui.screens.plan

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.model.*
import com.train2send.ui.navigation.Screen
import com.train2send.utils.calculateExerciseDuration
import com.train2send.utils.formatDuration
import com.train2send.utils.formatDurationRounded
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDayDetailScreen(
    planDayId: String,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    val scope = rememberCoroutineScope()

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

    var showAddExerciseSheet by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExerciseSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
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
                        text = "Tap + to add exercises to this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val exercisesToShow = plannedExercises.filter { planned ->
                if (planned.alternativeGroupId == null) true
                else planned.isSelected
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group by section
                val mainExercises = exercisesToShow.filter { it.section == ExerciseSection.MAIN }
                val secondaryExercises = exercisesToShow.filter { it.section == ExerciseSection.SECONDARY }
                val complementaryExercises = exercisesToShow.filter { it.section == ExerciseSection.COMPLEMENTARY }

                if (mainExercises.isNotEmpty()) {
                    item { SectionHeader("Main Exercises") }
                    items(mainExercises) { planned ->
                        val group = planned.alternativeGroupId?.let { gid ->
                            plannedExercises.filter { it.alternativeGroupId == gid }
                        } ?: emptyList()

                        PlannedExerciseCard(
                            planned = planned,
                            exercise = exerciseMap[planned.exerciseId],
                            onRemove = {
                                scope.launch {
                                    app.trainingPlanRepository.deletePlannedExercise(planned)
                                }
                            },
                            onToggleVariant = if (group.size > 1) {
                                {
                                    scope.launch {
                                        val currentIndex = group.indexOf(planned)
                                        val nextIndex = (currentIndex + 1) % group.size
                                        
                                        // Update all in group: only nextIndex is selected
                                        group.forEachIndexed { index, pe ->
                                            app.trainingPlanRepository.updatePlannedExercise(
                                                pe.copy(isSelected = index == nextIndex)
                                            )
                                        }
                                    }
                                }
                            } else null,
                            onClick = {
                                planned.exerciseId.let { id ->
                                    navController.navigate(Screen.ExerciseDetail.createRoute(id))
                                }
                            }
                        )
                    }
                }

                if (secondaryExercises.isNotEmpty()) {
                    item { SectionHeader("Secondary") }
                    items(secondaryExercises) { planned ->
                        val group = planned.alternativeGroupId?.let { gid ->
                            plannedExercises.filter { it.alternativeGroupId == gid }
                        } ?: emptyList()

                        PlannedExerciseCard(
                            planned = planned,
                            exercise = exerciseMap[planned.exerciseId],
                            onRemove = {
                                scope.launch {
                                    app.trainingPlanRepository.deletePlannedExercise(planned)
                                }
                            },
                            onToggleVariant = if (group.size > 1) {
                                {
                                    scope.launch {
                                        val currentIndex = group.indexOf(planned)
                                        val nextIndex = (currentIndex + 1) % group.size
                                        group.forEachIndexed { index, pe ->
                                            app.trainingPlanRepository.updatePlannedExercise(
                                                pe.copy(isSelected = index == nextIndex)
                                            )
                                        }
                                    }
                                }
                            } else null,
                            onClick = {
                                planned.exerciseId.let { id ->
                                    navController.navigate(Screen.ExerciseDetail.createRoute(id))
                                }
                            }
                        )
                    }
                }

                if (complementaryExercises.isNotEmpty()) {
                    item { SectionHeader("Complementary") }
                    items(complementaryExercises) { planned ->
                        val group = planned.alternativeGroupId?.let { gid ->
                            plannedExercises.filter { it.alternativeGroupId == gid }
                        } ?: emptyList()

                        PlannedExerciseCard(
                            planned = planned,
                            exercise = exerciseMap[planned.exerciseId],
                            onRemove = {
                                scope.launch {
                                    app.trainingPlanRepository.deletePlannedExercise(planned)
                                }
                            },
                            onToggleVariant = if (group.size > 1) {
                                {
                                    scope.launch {
                                        val currentIndex = group.indexOf(planned)
                                        val nextIndex = (currentIndex + 1) % group.size
                                        group.forEachIndexed { index, pe ->
                                            app.trainingPlanRepository.updatePlannedExercise(
                                                pe.copy(isSelected = index == nextIndex)
                                            )
                                        }
                                    }
                                }
                            } else null,
                            onClick = {
                                planned.exerciseId.let { id ->
                                    navController.navigate(Screen.ExerciseDetail.createRoute(id))
                                }
                            }
                        )
                    }
                }

                // Duration summary
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalSec = estimateDuration(exercisesToShow, exerciseMap)
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
                                text = "Estimated duration: ~${formatDurationRounded(totalSec)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Exercise Bottom Sheet
    if (showAddExerciseSheet) {
        AddExerciseSheet(
            allExercises = allExercises,
            alreadyAdded = plannedExercises.map { it.exerciseId }.toSet(),
            onDismiss = { showAddExerciseSheet = false },
            onAdd = { exerciseId, section ->
                scope.launch {
                    app.trainingPlanRepository.insertPlannedExercise(
                        PlannedExerciseEntity(
                            planDayId = planDayId,
                            exerciseId = exerciseId,
                            section = section,
                            orderIndex = plannedExercises.size
                        )
                    )
                }
                showAddExerciseSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseSheet(
    allExercises: List<ExerciseEntity>,
    alreadyAdded: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (exerciseId: String, section: ExerciseSection) -> Unit
) {
    var selectedSection by remember { mutableStateOf(ExerciseSection.MAIN) }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }

    val filteredExercises = allExercises
        .filter { it.id !in alreadyAdded }
        .filter { selectedCategory == null || it.category == selectedCategory }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add Exercise",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Section picker
            Text(
                text = "Section",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExerciseSection.entries.forEach { section ->
                    FilterChip(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        label = {
                            Text(
                                when (section) {
                                    ExerciseSection.MAIN -> "Main"
                                    ExerciseSection.SECONDARY -> "Secondary"
                                    ExerciseSection.COMPLEMENTARY -> "Complementary"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter
            Text(
                text = "Filter by category",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
                // Show first few categories that fit
                ExerciseCategory.entries.take(4).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label.take(8)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (allExercises.isEmpty()) "No exercises created yet.\nCreate exercises first."
                        else "All exercises already added.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        ExercisePickerItem(
                            exercise = exercise,
                            onClick = { onAdd(exercise.id, selectedSection) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerItem(
    exercise: ExerciseEntity,
    onClick: () -> Unit
) {
    val categoryColor = Color(android.graphics.Color.parseColor(exercise.category.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp, 32.dp)
                    .background(categoryColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = exercise.category.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor
                )
            }
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
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
    exercise: ExerciseEntity?,
    onRemove: () -> Unit,
    onToggleVariant: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val categoryColor = exercise?.let {
        Color(android.graphics.Color.parseColor(it.category.colorHex))
    } ?: MaterialTheme.colorScheme.outline

    var showRemoveConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                planned.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val sets = planned.customSets ?: exercise?.defaultSets
                val reps = planned.customReps ?: exercise?.defaultReps
                val duration = planned.customDurationSec ?: exercise?.defaultDurationSec
                val rest = planned.customRestSec ?: exercise?.defaultRestSec
                val restSet = planned.customRestBetweenSetsSec ?: exercise?.defaultRestBetweenSetsSec

                sets?.let { Text("$it sets", style = MaterialTheme.typography.labelSmall) }
                reps?.let { Text("$it reps", style = MaterialTheme.typography.labelSmall) }
                duration?.let { Text("${formatDuration(it)} work", style = MaterialTheme.typography.labelSmall) }
                rest?.let { Text("${formatDuration(it)} rest rep", style = MaterialTheme.typography.labelSmall) }
                restSet?.let { Text("${formatDuration(it)} rest set", style = MaterialTheme.typography.labelSmall) }
                
                // Add total duration for this exercise
                val totalSec = calculateExerciseDuration(
                    sets = sets,
                    reps = reps,
                    workRepSec = duration,
                    restRepSec = rest,
                    restSetSec = restSet
                )
                if (totalSec > 0) {
                    Text(
                        text = "Total: ${formatDuration(totalSec)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (onToggleVariant != null) {
                val icon = when (exercise?.climbingType) {
                    ClimbingType.BOULDERING -> Icons.Default.Terrain
                    ClimbingType.ROPE -> Icons.Default.Height
                    else -> Icons.Default.SwapHoriz
                }
                IconButton(
                    onClick = onToggleVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = "Switch Variant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
                onClick = { showRemoveConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Remove Exercise") },
            text = { Text("Remove \"${exercise?.name ?: "this exercise"}\" from this day?") },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveConfirm = false
                    onRemove()
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun estimateDuration(
    exercises: List<PlannedExerciseEntity>,
    exerciseMap: Map<String, ExerciseEntity>
): Int {
    if (exercises.isEmpty()) return 0
    return exercises.sumOf { planned ->
        val exercise = exerciseMap[planned.exerciseId]
        calculateExerciseDuration(
            sets = planned.customSets ?: exercise?.defaultSets,
            reps = planned.customReps ?: exercise?.defaultReps,
            workRepSec = planned.customDurationSec ?: exercise?.defaultDurationSec,
            restRepSec = planned.customRestSec ?: exercise?.defaultRestSec,
            restSetSec = planned.customRestBetweenSetsSec ?: exercise?.defaultRestBetweenSetsSec
        )
    }
}
