package com.example.moneypath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.navigation.MoneyPathAppNavigation
import com.example.moneypath.ui.theme.MoneyPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MoneyPathViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsState()

            MoneyPathTheme(darkTheme = userProfile.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoneyPathAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
