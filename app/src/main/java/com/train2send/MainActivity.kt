package com.train2send

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.train2send.data.repository.ThemePreference
import com.train2send.ui.navigation.AppNavigation
import com.train2send.ui.theme.Train2SendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as Train2SendApp
            val themePreference by app.userPreferencesRepository.themePreferenceFlow
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

            Train2SendTheme(themePreference = themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
