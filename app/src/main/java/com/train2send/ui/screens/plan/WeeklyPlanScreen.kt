package com.train2send.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.model.ExerciseCategory
import com.train2send.data.model.ExerciseEntity
import com.train2send.data.model.PlanDayEntity
import com.train2send.data.model.PlannedExerciseEntity
import com.train2send.data.model.TrainingPlanEntity
import com.train2send.ui.navigation.Screen
import com.train2send.ui.screens.plan.TrainingGuideScreen
import com.train2send.utils.calculateExerciseDuration
import com.train2send.utils.formatDuration
import com.train2send.utils.formatDurationRounded
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    val activePlan by app.trainingPlanRepository.getActivePlan()
        .collectAsStateWithLifecycle(initialValue = null)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Schedule", "Breakdown", "Guide")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Weekly Plan") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (activePlan != null) {
                            IconButton(onClick = {
                                navController.navigate(Screen.PlanSetup.createRoute(activePlan!!.id))
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Plan")
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate(Screen.PlanList.route)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "All Plans")
                        }
                    }
                )
                if (activePlan != null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (activePlan == null) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.PlanSetup.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Plan")
                }
            }
        }
    ) { padding ->
        if (activePlan == null) {
            NoPlanContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onCreatePlan = { navController.navigate(Screen.PlanSetup.route) }
            )
        } else {
            when (selectedTab) {
                0 -> WeeklyPlanContent(
                    plan = activePlan!!,
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
                1 -> PlanBreakdownScreen(
                    plan = activePlan!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
                2 -> TrainingGuideScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun NoPlanContent(modifier: Modifier, onCreatePlan: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No weekly plan yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a plan to see your\nweekly training schedule",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Plan")
            }
        }
    }
}

@Composable
private fun WeeklyPlanContent(
    plan: TrainingPlanEntity,
    navController: NavController,
    modifier: Modifier
) {
    val app = LocalContext.current.applicationContext as Train2SendApp
    val planDays by app.trainingPlanRepository.getDaysForPlan(plan.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    
    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val exerciseMap = allExercises.associateBy { it.id }

    val today = LocalDate.now()
    val todayDow = today.dayOfWeek.value // 1=Mon, 7=Sun
    val todayPlanDay = planDays.find { it.dayOfWeek == todayDow }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plan title
        Text(
            text = plan.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Today's Dashboard
        TodayDashboard(
            todayPlanDay = todayPlanDay,
            navController = navController,
            app = app,
            exerciseMap = exerciseMap
        )

        // 7-Day Calendar
        Text(
            text = "This Week",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        WeekCalendarRow(
            planDays = planDays,
            today = today,
            onDayClick = { planDay ->
                navController.navigate(Screen.PlanDayDetail.createRoute(planDay.id))
            }
        )

        // Day-by-day breakdown
        planDays.sortedBy { it.dayOfWeek }.forEach { day ->
            DaySummaryCard(
                planDay = day,
                isToday = day.dayOfWeek == todayDow,
                app = app,
                exerciseMap = exerciseMap,
                onClick = {
                    navController.navigate(Screen.PlanDayDetail.createRoute(day.id))
                }
            )
        }
    }
}

@Composable
private fun TodayDashboard(
    todayPlanDay: PlanDayEntity?,
    navController: NavController,
    app: Train2SendApp,
    exerciseMap: Map<String, ExerciseEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (todayPlanDay != null) {
                val allPlannedExercises by app.trainingPlanRepository
                    .getExercisesForDay(todayPlanDay.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                val exercises = allPlannedExercises.filter { 
                    it.isSelected || it.alternativeGroupId.isNullOrBlank() 
                }

                Text(
                    text = todayPlanDay.dayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))

                val estimatedSeconds = estimateDuration(exercises, exerciseMap)
                Text(
                    text = "${exercises.size} exercises · ~${formatDurationRounded(estimatedSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate(Screen.PlanDayDetail.createRoute(todayPlanDay.id))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Session")
                }
            } else {
                Text(
                    text = "Rest Day",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No training scheduled for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun WeekCalendarRow(
    planDays: List<PlanDayEntity>,
    today: LocalDate,
    onDayClick: (PlanDayEntity) -> Unit
) {
    val startOfWeek = today.with(DayOfWeek.MONDAY)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (0..6).forEach { offset ->
            val date = startOfWeek.plusDays(offset.toLong())
            val dayOfWeek = date.dayOfWeek.value
            val planDay = planDays.find { it.dayOfWeek == dayOfWeek }
            val isToday = date == today

            DayChip(
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                dayNumber = date.dayOfMonth.toString(),
                isToday = isToday,
                hasSession = planDay != null,
                onClick = { planDay?.let { onDayClick(it) } }
            )
        }
    }
}

@Composable
private fun DayChip(
    dayLabel: String,
    dayNumber: String,
    isToday: Boolean,
    hasSession: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        hasSession -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        hasSession -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        if (hasSession) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}

@Composable
private fun DaySummaryCard(
    planDay: PlanDayEntity,
    isToday: Boolean,
    app: Train2SendApp,
    exerciseMap: Map<String, ExerciseEntity>,
    onClick: () -> Unit
) {
    val allPlannedExercises by app.trainingPlanRepository
        .getExercisesForDay(planDay.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    
    val exercises = allPlannedExercises.filter { 
        it.isSelected || it.alternativeGroupId.isNullOrBlank() 
    }

    val dayName = DayOfWeek.of(planDay.dayOfWeek)
        .getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = if (isToday) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = planDay.dayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${exercises.size} exercises · ~${formatDurationRounded(estimateDuration(exercises, exerciseMap))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View day",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
