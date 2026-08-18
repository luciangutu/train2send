package com.train4send.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.train4send.data.model.ExerciseCategory
import com.train4send.ui.navigation.Screen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as Train4SendApp
    val activePlan by app.trainingPlanRepository.getActivePlan()
        .collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Train4Send", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Today's Session Card (if plan exists)
            activePlan?.let { plan ->
                TodaySessionCard(
                    planId = plan.id,
                    app = app,
                    navController = navController
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categories
            Text(
                text = "Training Categories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ExerciseCategory.entries.toList()) { category ->
                    CategoryCard(category = category)
                }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.WeeklyPlan.route) },
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
                    text = "Today's Session",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (todayPlanDay != null) {
                val exercises by app.trainingPlanRepository
                    .getExercisesForDay(todayPlanDay.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                Text(
                    text = todayPlanDay.dayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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
            } else {
                Text(
                    text = "Rest Day 🧘",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "No training scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CategoryCard(category: ExerciseCategory) {
    val color = Color(android.graphics.Color.parseColor(category.colorHex))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
