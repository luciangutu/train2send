package com.train2send.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.model.ClimbingType
import com.train2send.data.model.ExerciseCategory
import com.train2send.data.model.ExerciseEntity
import com.train2send.utils.calculateExerciseDuration
import com.train2send.utils.color
import com.train2send.utils.formatDuration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditScreen(navController: NavController, exerciseId: String? = null) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.STRENGTH) }
    var climbingType by remember { mutableStateOf(ClimbingType.ANY) }
    var defaultSets by remember { mutableStateOf("") }
    var defaultReps by remember { mutableStateOf("") }
    var defaultDurationSec by remember { mutableStateOf("") }
    var defaultRestSec by remember { mutableStateOf("") }
    var defaultRestBetweenSetsSec by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(exerciseId != null) }

    LaunchedEffect(exerciseId) {
        if (exerciseId != null) {
            val exercise = app.exerciseRepository.getExerciseById(exerciseId).first()
            if (exercise != null) {
                name = exercise.name
                description = exercise.description ?: ""
                selectedCategory = exercise.category
                climbingType = exercise.climbingType
                defaultSets = exercise.defaultSets?.toString() ?: ""
                defaultReps = exercise.defaultReps?.toString() ?: ""
                defaultDurationSec = exercise.defaultDurationSec?.toString() ?: ""
                defaultRestSec = exercise.defaultRestSec?.toString() ?: ""
                defaultRestBetweenSetsSec = exercise.defaultRestBetweenSetsSec?.toString() ?: ""
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (exerciseId == null) "Create Exercise" else "Edit Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    scope.launch {
                                        val exercise = ExerciseEntity(
                                            id = exerciseId ?: UUID.randomUUID().toString(),
                                            name = name.trim(),
                                            category = selectedCategory,
                                            climbingType = climbingType,
                                            description = description.takeIf { it.isNotBlank() },
                                            defaultSets = defaultSets.toIntOrNull(),
                                            defaultReps = defaultReps.toIntOrNull(),
                                            defaultDurationSec = defaultDurationSec.toIntOrNull(),
                                            defaultRestSec = defaultRestSec.toIntOrNull(),
                                            defaultRestBetweenSetsSec = defaultRestBetweenSetsSec.toIntOrNull()
                                        )
                                        if (exerciseId == null) {
                                            app.exerciseRepository.insertExercise(exercise)
                                        } else {
                                            app.exerciseRepository.updateExercise(exercise)
                                        }
                                        navController.popBackStack()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Category selector
                Text("Category", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExerciseCategory.entries.forEach { category ->
                        val color = category.color
                        val isSelected = category == selectedCategory

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) color else color.copy(alpha = 0.3f))
                                .clickable { selectedCategory = category },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = category.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = selectedCategory.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = selectedCategory.color
                )

                // Climbing Type selector
                Text("Climbing Type", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClimbingType.entries.forEach { type ->
                        FilterChip(
                            selected = climbingType == type,
                            onClick = { climbingType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                HorizontalDivider()

                // Defaults
                Text("Defaults (optional)", style = MaterialTheme.typography.labelLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = defaultSets,
                        onValueChange = { defaultSets = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultReps,
                        onValueChange = { defaultReps = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = defaultDurationSec,
                        onValueChange = { defaultDurationSec = it },
                        label = { Text("Work Rep (sec)") },
                        supportingText = {
                            defaultDurationSec.toIntOrNull()?.let {
                                Text(formatDuration(it))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultRestSec,
                        onValueChange = { defaultRestSec = it },
                        label = { Text("Rest rep (sec)") },
                        supportingText = {
                            defaultRestSec.toIntOrNull()?.let {
                                Text(formatDuration(it))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = defaultRestBetweenSetsSec,
                    onValueChange = { defaultRestBetweenSetsSec = it },
                    label = { Text("Rest set (sec)") },
                    supportingText = {
                        defaultRestBetweenSetsSec.toIntOrNull()?.let {
                            Text(formatDuration(it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Total Duration display
                val totalDuration = calculateExerciseDuration(
                    sets = defaultSets.toIntOrNull(),
                    reps = defaultReps.toIntOrNull(),
                    workRepSec = defaultDurationSec.toIntOrNull(),
                    restRepSec = defaultRestSec.toIntOrNull(),
                    restSetSec = defaultRestBetweenSetsSec.toIntOrNull()
                )

                if (totalDuration > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Estimated Duration: ${formatDuration(totalDuration)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
