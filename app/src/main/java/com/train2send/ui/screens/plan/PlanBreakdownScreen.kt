package com.train2send.ui.screens.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.train2send.Train2SendApp
import com.train2send.data.model.*
import com.train2send.utils.calculateExerciseDuration
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max

@Composable
fun PlanBreakdownScreen(
    plan: TrainingPlanEntity,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    
    var selectedType by remember { mutableStateOf(BreakdownType.TOTAL) }
    
    // Time range for current week
    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val endOfWeek = today.with(DayOfWeek.SUNDAY)
    
    val startTimestamp = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endTimestamp = endOfWeek.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val plannedExercises by app.trainingPlanRepository.getExercisesForPlan(plan.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    
    val workoutLogs by app.workoutLogRepository.getLogsBetween(startTimestamp, endTimestamp)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val exerciseMap = allExercises.associateBy { it.id }

    val categoryData = remember(plannedExercises, workoutLogs, exerciseMap, selectedType) {
        calculateBreakdown(plannedExercises, workoutLogs, exerciseMap, selectedType)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (plannedExercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No planned exercises found for this week", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = selectedType == BreakdownType.TIME,
                        onClick = { selectedType = BreakdownType.TIME },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Time")
                    }
                    SegmentedButton(
                        selected = selectedType == BreakdownType.TOTAL,
                        onClick = { selectedType = BreakdownType.TOTAL },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Total")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SessionBreakdownChart(
                data = categoryData.filter { it.total > 0 || it.completed > 0 },
                unitLabel = if (selectedType == BreakdownType.TIME) "Hours" else "Sessions"
            )
        }
    }
}

enum class BreakdownType {
    TIME, TOTAL
}

data class CategoryBreakdown(
    val label: String,
    val color: Color,
    val completed: Float,
    val total: Float
)

@Composable
private fun SessionBreakdownChart(
    data: List<CategoryBreakdown>,
    unitLabel: String
) {
    val isSessions = unitLabel == "Sessions"
    val maxValue = data.maxOfOrNull { it.total } ?: 0f
    
    val axisMax = if (isSessions) {
        7f // Always 7 days for weekly sessions
    } else {
        // Adaptive for hours
        if (maxValue <= 0f) 6f 
        else if (maxValue <= 1f) 1f
        else if (maxValue <= 6f) kotlin.math.ceil(maxValue).toFloat()
        else (kotlin.math.ceil(maxValue / 2.0) * 2).toFloat()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    Canvas(modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)) {
                        val width = size.width
                        val barHeight = size.height
                        
                        // Total bar (light)
                        val totalWidth = (item.total / axisMax) * width
                        if (totalWidth > 0) {
                            drawRoundRect(
                                color = item.color.copy(alpha = 0.6f),
                                size = Size(totalWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                        
                        // Completed bar (solid)
                        val completedWidth = (item.completed / axisMax) * width
                        if (completedWidth > 0) {
                            drawRoundRect(
                                color = item.color,
                                size = Size(completedWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-Axis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 116.dp)
                .height(16.dp)
        ) {
            val stepCount = axisMax.toInt()
            for (i in 0..stepCount) {
                val bias = if (axisMax > 0f) (i.toFloat() / axisMax) * 2 - 1 else -1f
                Text(
                    text = (i + 1).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(androidx.compose.ui.BiasAlignment(bias, 0f))
                )
            }
        }
        
        Text(
            text = unitLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun calculateBreakdown(
    planned: List<PlannedExerciseEntity>,
    logs: List<WorkoutLogEntity>,
    exerciseMap: Map<String, ExerciseEntity>,
    type: BreakdownType
): List<CategoryBreakdown> {
    return ExerciseCategory.entries.map { cat ->
        val plannedForCat = planned.filter { pe ->
            val exercise = exerciseMap[pe.exerciseId]
            exercise?.category == cat && (pe.isSelected || pe.alternativeGroupId.isNullOrBlank())
        }
        
        val logsForCat = logs.filter { log ->
            val exercise = exerciseMap[log.exerciseId]
            exercise?.category == cat
        }

        val color = try {
            val baseColor = android.graphics.Color.parseColor(cat.colorHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(baseColor, hsv)
            // Force maximum brightness and saturation for visibility in dark mode
            hsv[1] = 1.0f // Saturation
            hsv[2] = 1.0f // Value/Brightness
            Color(android.graphics.Color.HSVToColor(hsv))
        } catch (e: Exception) {
            Color.Gray
        }

        if (type == BreakdownType.TIME) {
            val plannedSeconds = plannedForCat.sumOf { pe ->
                val exercise = exerciseMap[pe.exerciseId]
                calculateExerciseDuration(
                    sets = pe.customSets ?: exercise?.defaultSets,
                    reps = pe.customReps ?: exercise?.defaultReps,
                    workRepSec = pe.customDurationSec ?: exercise?.defaultDurationSec,
                    restRepSec = pe.customRestSec ?: exercise?.defaultRestSec,
                    restSetSec = pe.customRestBetweenSetsSec ?: exercise?.defaultRestBetweenSetsSec
                )
            }
            val completedSeconds = logsForCat.sumOf { it.durationSeconds ?: 0 }
            
            CategoryBreakdown(
                label = cat.label,
                color = color,
                completed = completedSeconds / 3600f,
                total = max(plannedSeconds / 3600f, completedSeconds / 3600f)
            )
        } else {
            // Sessions (Days)
            val plannedDays = plannedForCat.map { it.planDayId }.distinct().size.toFloat()
            
            // For logs, we group by date
            val completedDays = logsForCat.map { log ->
                Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }.distinct().size.toFloat()

            CategoryBreakdown(
                label = cat.label,
                color = color,
                completed = completedDays,
                total = max(plannedDays, completedDays)
            )
        }
    }
}
