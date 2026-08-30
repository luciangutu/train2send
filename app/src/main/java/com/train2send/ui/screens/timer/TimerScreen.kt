package com.train2send.ui.screens.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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
import androidx.compose.ui.platform.LocalContext
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
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun generate880HzWav(context: Context): File {
    val sampleRate = 44100
    val duration = 0.15
    val numSamples = (sampleRate * duration).toInt()
    val volume = 0.6
    val frequency = 880.0
    
    // Use a new version name to force re-generation
    val file = File(context.cacheDir, "beep_880_v3.wav")
    if (file.exists()) file.delete()

    val outputStream = FileOutputStream(file)
    
    // WAV Header
    val totalAudioLen = numSamples * 2L
    val totalDataLen = totalAudioLen + 36
    val byteRate = sampleRate * 2L
    
    val header = ByteArray(44)
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    header[4] = (totalDataLen and 0xff).toByte()
    header[5] = ((totalDataLen shr 8) and 0xff).toByte()
    header[6] = ((totalDataLen shr 16) and 0xff).toByte()
    header[7] = ((totalDataLen shr 24) and 0xff).toByte()
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    header[16] = 16 // Subchunk1Size
    header[17] = 0
    header[18] = 0
    header[19] = 0
    header[20] = 1 // PCM
    header[21] = 0
    header[22] = 1 // Mono
    header[23] = 0
    header[24] = (sampleRate and 0xff).toByte()
    header[25] = ((sampleRate shr 8) and 0xff).toByte()
    header[26] = ((sampleRate shr 16) and 0xff).toByte()
    header[27] = ((sampleRate shr 24) and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte()
    header[29] = ((byteRate shr 8) and 0xff).toByte()
    header[30] = ((byteRate shr 16) and 0xff).toByte()
    header[31] = ((byteRate shr 24) and 0xff).toByte()
    header[32] = 2 // BlockAlign
    header[33] = 0
    header[34] = 16 // BitsPerSample
    header[35] = 0
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    header[40] = (totalAudioLen and 0xff).toByte()
    header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
    header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
    header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
    
    outputStream.write(header)
    
    // Apply a small fade-in and fade-out to prevent "clicking" sounds at the end
    val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
    val fadeSamples = (sampleRate * 0.01).toInt() // 10ms fade
    
    for (i in 0 until numSamples) {
        val angle = 2.0 * Math.PI * i * frequency / sampleRate
        var currentVolume = volume
        
        // Fade in
        if (i < fadeSamples) {
            currentVolume *= i.toDouble() / fadeSamples
        } 
        // Fade out
        else if (i > numSamples - fadeSamples) {
            currentVolume *= (numSamples - i).toDouble() / fadeSamples
        }
        
        val sample = (Math.sin(angle) * Short.MAX_VALUE * currentVolume).toInt().toShort()
        buffer.putShort(sample)
    }
    outputStream.write(buffer.array())
    outputStream.close()
    
    return file
}

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

    val context = LocalContext.current

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
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .build()
    }
    
    var defaultSoundId by remember { mutableIntStateOf(-1) }
    var isSoundLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(soundPool) {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isSoundLoaded = true
        }
        val file = generate880HzWav(context)
        defaultSoundId = soundPool.load(file.absolutePath, 1)
    }

    DisposableEffect(soundPool) {
        onDispose {
            soundPool.release()
        }
    }

    LaunchedEffect(timerEngine, isSoundLoaded, defaultSoundId) {
        timerEngine.soundEvents.collect { event ->
            // Play synthesized 880Hz sound exclusively
            if (isSoundLoaded && defaultSoundId != -1) {
                when (event) {
                    SoundEvent.BEEP -> {
                        soundPool.play(defaultSoundId, 1f, 1f, 1, 0, 1f)
                    }
                    SoundEvent.DOUBLE_BEEP -> {
                        soundPool.play(defaultSoundId, 1f, 1f, 1, 0, 1f)
                        delay(500)
                        soundPool.play(defaultSoundId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }
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
