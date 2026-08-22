package com.train2send.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.model.ExerciseEntity
import com.train2send.data.model.ExerciseSection
import com.train2send.data.model.PlannedExerciseEntity
import com.train2send.data.repository.ThemePreference
import com.train2send.ui.navigation.Screen
import com.train2send.utils.calculateExerciseDuration
import com.train2send.utils.formatDuration
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as Train2SendApp
    val activePlan by app.trainingPlanRepository.getActivePlan()
        .collectAsStateWithLifecycle(initialValue = null)

    val scope = rememberCoroutineScope()
    var showThemeMenu by remember { mutableStateOf(false) }
    val themePreference by app.userPreferencesRepository.themePreferenceFlow
        .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

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
                        Text("Train2Send", fontWeight = FontWeight.Bold)
                        Text(
                            text = "v$versionName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = when (themePreference) {
                                    ThemePreference.LIGHT -> Icons.Default.LightMode
                                    ThemePreference.DARK -> Icons.Default.DarkMode
                                    ThemePreference.SYSTEM -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "Theme"
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Light") },
                                onClick = {
                                    scope.launch { app.userPreferencesRepository.updateThemePreference(ThemePreference.LIGHT) }
                                    showThemeMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LightMode, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Dark") },
                                onClick = {
                                    scope.launch { app.userPreferencesRepository.updateThemePreference(ThemePreference.DARK) }
                                    showThemeMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.DarkMode, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("System") },
                                onClick = {
                                    scope.launch { app.userPreferencesRepository.updateThemePreference(ThemePreference.SYSTEM) }
                                    showThemeMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.BrightnessAuto, null) }
                            )
                        }
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

            // Swipeable Session Pager
            activePlan?.let { plan ->
                SessionPager(
                    planId = plan.id,
                    planName = plan.title,
                    app = app,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionPager(
    planId: String,
    planName: String,
    app: Train2SendApp,
    navController: NavController
) {
    val planDays by app.trainingPlanRepository.getDaysForPlan(planId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val todayDow = LocalDate.now().dayOfWeek.value // 1-7
    val pagerState = rememberPagerState(
        initialPage = todayDow - 1,
        pageCount = { 7 }
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { index ->
                    val isToday = (index + 1) == todayDow
                    val color = if (pagerState.currentPage == index) {
                        MaterialTheme.colorScheme.primary
                    } else if (isToday) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    }
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val dayOfWeekValue = page + 1
            val planDay = planDays.find { it.dayOfWeek == dayOfWeekValue }
            val isToday = dayOfWeekValue == todayDow

            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            TodaySessionCard(
                planDay = planDay,
                planName = planName,
                dayOfWeek = dayOfWeekValue,
                isToday = isToday,
                app = app,
                navController = navController,
                modifier = Modifier
                    .graphicsLayer {
                        // Grayed out and scaled down effect for non-active pages
                        val scale = lerp(0.9f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    }
            )
        }
    }
}

@Composable
private fun TodaySessionCard(
    planDay: com.train2send.data.model.PlanDayEntity?,
    planName: String,
    dayOfWeek: Int,
    isToday: Boolean,
    app: Train2SendApp,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val allExercises by app.exerciseRepository.getAllExercises()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val exerciseMap = allExercises.associateBy { it.id }

    val dayName = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                planDay?.let {
                    navController.navigate(Screen.WeeklyPlan.route)
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isToday) Icons.Default.Today else Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isToday) "TODAY'S SESSION" else "${dayName.uppercase()}'S SESSION",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (planDay != null) {
                val exercises by app.trainingPlanRepository
                    .getExercisesForDay(planDay.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                Text(
                    text = planDay.dayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val estimatedSec = if (exercises.isEmpty()) 0 else {
                    exercises.sumOf { planned ->
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
                Text(
                    text = "${exercises.size} exercises · ~${formatDuration(estimatedSec)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                if (exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Grouping exercises by section
                    val groupedExercises = exercises.groupBy { it.section }
                    val sectionOrder = listOf(ExerciseSection.MAIN, ExerciseSection.SECONDARY, ExerciseSection.COMPLEMENTARY)

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sectionOrder.forEach { section ->
                            val sectionExercises = groupedExercises[section] ?: emptyList()
                            if (sectionExercises.isNotEmpty()) {
                                sectionExercises.forEach { planned ->
                                    val exercise = exerciseMap[planned.exerciseId]
                                    ExerciseTile(
                                        planned = planned,
                                        exercise = exercise,
                                        isToday = isToday,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            exercise?.id?.let { id ->
                                                navController.navigate(Screen.ExerciseDetail.createRoute(id, planned.id))
                                            }
                                        }
                                    )
                                }
                                
                                // Check if there are subsequent sections to add a spacer
                                val hasMoreSections = sectionOrder
                                    .drop(sectionOrder.indexOf(section) + 1)
                                    .any { groupedExercises[it]?.isNotEmpty() == true }
                                
                                if (hasMoreSections) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No exercises added to this day yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Text(
                    text = "Rest Day 🧘",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No training scheduled. Enjoy your recovery!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ExerciseTile(
    planned: PlannedExerciseEntity,
    exercise: ExerciseEntity?,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
    }
    
    val contentColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val categoryColor = exercise?.let {
        try {
            Color(android.graphics.Color.parseColor(it.category.colorHex))
        } catch (e: Exception) {
            contentColor
        }
    } ?: contentColor

    val indicatorColor = when (planned.section) {
        ExerciseSection.MAIN -> categoryColor
        ExerciseSection.SECONDARY -> categoryColor.copy(alpha = 0.7f)
        ExerciseSection.COMPLEMENTARY -> categoryColor.copy(alpha = 0.4f)
    }

    val indicatorWidth = when (planned.section) {
        ExerciseSection.MAIN -> 6.dp
        ExerciseSection.SECONDARY -> 4.dp
        ExerciseSection.COMPLEMENTARY -> 2.dp
    }

    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Section Indicator Bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(indicatorWidth)
                    .background(indicatorColor)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = exercise?.name ?: "Unknown",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor
                )
                
                val sets = planned.customSets ?: exercise?.defaultSets
                val reps = planned.customReps ?: exercise?.defaultReps
                val duration = planned.customDurationSec ?: exercise?.defaultDurationSec

                val detailText = buildString {
                    if (sets != null) append("${sets}x")
                    if (reps != null) {
                        if (isNotEmpty()) append("")
                        append("$reps")
                    }
                    if (duration != null) {
                        if (isNotEmpty()) append(" ")
                        append("(${formatDuration(duration)})")
                    }
                }

                Text(
                    text = detailText,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF757575))
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
