package com.train2send.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TrainingGuideScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What to train?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Flowchart steps
        FlowStep(
            question = "CAN YOU WALK TO THE CLIFF?",
            noAction = "1\nTRAIN YOUR CARDIO SYSTEM",
            showDownArrow = true
        )

        FlowStep(
            question = "CAN YOU HOLD ALL HOLDS ON YOUR PROJECT?",
            noAction = "2\nGET ON THE HANGBOARD",
            showDownArrow = true
        )

        FlowStep(
            question = "CAN YOU MOVE BETWEEN THEM?",
            noAction = "3\nWORK ON LIMIT BOULDERING",
            showDownArrow = true
        )

        FlowStep(
            question = "CAN YOU CLIMB THE SECTIONS BETWEEN RESTS?",
            noAction = "4\nWORK ON ROUTE-\nSPECIFIC INTERVALS",
            showDownArrow = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionBox(text = "ARE YOU AFRAID?", modifier = Modifier.weight(1f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("YES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            
            ActionBox(text = "5\nDANGER ASSESSMENT &\nFALL EXERCISES", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "NO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(20.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionBox(text = "CAN YOU GIVE IT SEVERAL TRIES PER DAY?", modifier = Modifier.weight(1f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("NO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            
            ActionBox(text = "6\nTRAIN YOUR CAPACITY", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "YES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(20.dp))

        Spacer(modifier = Modifier.height(8.dp))

        ActionBox(text = "7\nSTART REDPOINTING", modifier = Modifier.width(200.dp))
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun FlowStep(
    question: String,
    noAction: String,
    showDownArrow: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuestionBox(text = question, modifier = Modifier.weight(1.2f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("NO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            
            ActionBox(text = noAction, modifier = Modifier.weight(1f))
        }
        
        if (showDownArrow) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "YES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuestionBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Black)
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ActionBox(text: String, modifier: Modifier = Modifier) {
    val purple = Color(0xFF7B1FA2)
    Box(
        modifier = modifier
            .background(purple)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingGuideScreenPreview() {
    MaterialTheme {
        TrainingGuideScreen()
    }
}
