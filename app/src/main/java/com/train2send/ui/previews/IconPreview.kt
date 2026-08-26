package com.train2send.ui.previews

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun IconRow(name: String, icons: List<Pair<String, ImageVector>>) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = name, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icons.forEach { (label, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(32.dp))
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IconPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            IconRow(
                name = "Bouldering Options",
                icons = listOf(
                    "Terrain" to Icons.Default.Terrain,
                    "FilterHdr" to Icons.Default.FilterHdr,
                    "GridView" to Icons.Default.GridView,
                    "Category" to Icons.Default.Category,
                    "Texture" to Icons.Default.Texture
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            IconRow(
                name = "Rope Options",
                icons = listOf(
                    "Height" to Icons.Default.Height,
                    "Link" to Icons.Default.Link,
                    "Hiking" to Icons.Default.Hiking,
                    "Security" to Icons.Default.Security,
                    "ArrowUpward" to Icons.Default.ArrowUpward
                )
            )
        }
    }
}
