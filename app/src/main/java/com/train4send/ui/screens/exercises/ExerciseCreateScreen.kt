package com.train4send.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.train4send.Train4SendApp
import com.train4send.data.model.ExerciseCategory
import com.train4send.data.model.ExerciseEntity
import com.train4send.utils.formatDuration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCreateScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as Train4SendApp
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.STRENGTH) }
    var defaultSets by remember { mutableStateOf("") }
    var defaultReps by remember { mutableStateOf("") }
    var defaultDurationSec by remember { mutableStateOf("") }
    var defaultRestSec by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                scope.launch {
                                    app.exerciseRepository.insertExercise(
                                        ExerciseEntity(
                                            name = name.trim(),
                                            category = selectedCategory,
                                            description = description.takeIf { it.isNotBlank() },
                                            defaultSets = defaultSets.toIntOrNull(),
                                            defaultReps = defaultReps.toIntOrNull(),
                                            defaultDurationSec = defaultDurationSec.toIntOrNull(),
                                            defaultRestSec = defaultRestSec.toIntOrNull()
                                        )
                                    )
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
                    val color = Color(android.graphics.Color.parseColor(category.colorHex))
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
                color = Color(android.graphics.Color.parseColor(selectedCategory.colorHex))
            )

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
                    label = { Text("Work (sec)") },
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
                    label = { Text("Rest (sec)") },
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
        }
    }
}
