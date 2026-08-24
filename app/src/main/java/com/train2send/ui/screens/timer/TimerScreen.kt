package com.train2send.ui.screens.timer

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.train2send.domain.timer.FlexibleTimerEngine
import com.train2send.domain.timer.SoundEvent
import com.train2send.domain.timer.TimerState
import com.train2send.utils.formatDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    work: Int? = null,
    restRep: Int? = null,
    reps: Int? = null,
    sets: Int? = null,
    restSet: Int? = null
) {
    val timerEngine = remember { FlexibleTimerEngine() }
    val timerState by timerEngine.state.collectAsStateWithLifecycle()
    val timerScope = remember { CoroutineScope(Dispatchers.Default) }

    var workSecInput by remember { mutableStateOf(work?.toString() ?: "10") }
    var restRepSecInput by remember { mutableStateOf(restRep?.toString() ?: "0") }
    var repsInput by remember { mutableStateOf(reps?.toString() ?: "1") }
    var setsInput by remember { mutableStateOf(sets?.toString() ?: "6") }
    var restSetSecInput by remember { mutableStateOf(restSet?.toString() ?: "60") }

    // Keep screen on during timer
    val view = LocalView.current
    val isTimerActive = timerState is TimerState.Running || timerState is TimerState.Preparing
    LaunchedEffect(isTimerActive) {
        view.keepScreenOn = isTimerActive
    }
    DisposableEffect(Unit) {
        onDispose {
            view.keepScreenOn = false
        }
    }

    // Sound handling
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_ALARM, 100) }
    LaunchedEffect(timerEngine) {
        timerEngine.soundEvents.collect { event ->
            when (event) {
                SoundEvent.BEEP -> {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                }
                SoundEvent.DOUBLE_BEEP -> {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    delay(300)
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    // Auto-start if parameters are provided
    LaunchedEffect(Unit) {
        if (work != null || sets != null) {
            timerEngine.startExerciseProtocol(
                scope = timerScope,
                workSec = work ?: 10,
                restRepSec = restRep ?: 0,
                reps = reps ?: 1,
                sets = sets ?: 6,
                restSetSec = restSet ?: 60
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Timer display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (timerState) {
                        is TimerState.Preparing -> MaterialTheme.colorScheme.secondaryContainer
                        is TimerState.Running -> {
                            if ((timerState as TimerState.Running).isWorkPhase)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        }
                        is TimerState.Finished -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = timerState) {
                        is TimerState.Idle -> {
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Light
                            )
                        }
                        is TimerState.Preparing -> {
                            Text(
                                text = "PREPARE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.remainingSeconds.toString(),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        is TimerState.Running -> {
                            Text(
                                text = if (state.isWorkPhase) "WORK" else "REST",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatDuration(state.remainingSeconds),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Set ${state.currentSet} / ${state.totalSets}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (state.currentRep != null && state.totalReps != null && state.totalReps > 1) {
                                    Text(
                                        text = "  •  Rep ${state.currentRep} / ${state.totalReps}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        is TimerState.Finished -> {
                            Text(
                                text = "Done!",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is TimerState.Paused -> {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Controls (only show config when idle)
            if (timerState is TimerState.Idle || timerState is TimerState.Finished) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = setsInput,
                            onValueChange = { setsInput = it },
                            label = { Text("Sets") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = restSetSecInput,
                            onValueChange = { restSetSecInput = it },
                            label = { Text("Rest/Set (s)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = restRepSecInput,
                            onValueChange = { restRepSecInput = it },
                            label = { Text("Rest/Rep (s)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = workSecInput,
                        onValueChange = { workSecInput = it },
                        label = { Text("Work Duration (s)") },
                        supportingText = {
                            workSecInput.toIntOrNull()?.let { Text(formatDuration(it)) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        timerEngine.startExerciseProtocol(
                            scope = timerScope,
                            workSec = workSecInput.toIntOrNull() ?: 10,
                            restRepSec = restRepSecInput.toIntOrNull() ?: 0,
                            reps = repsInput.toIntOrNull() ?: 1,
                            sets = setsInput.toIntOrNull() ?: 6,
                            restSetSec = restSetSecInput.toIntOrNull() ?: 60
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start", style = MaterialTheme.typography.titleMedium)
                }
            } else if (timerState is TimerState.Running || timerState is TimerState.Preparing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { timerEngine.stop() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Button(
                        onClick = { timerEngine.next() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
