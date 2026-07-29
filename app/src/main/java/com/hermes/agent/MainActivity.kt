package com.hermes.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermes.agent.ui.navigation.HermesNavGraph
import com.hermes.agent.ui.theme.HermesAgentTheme
import com.hermes.agent.viewmodel.HermesViewModel

/**
 * Main entry point for the Hermes Agent Android app.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: HermesViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            HermesAgentTheme(
                darkMode = settings.darkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HermesNavGraph(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
