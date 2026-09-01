package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ProfileRepository
import com.example.ui.FaceVerifyViewModel
import com.example.ui.ViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object RegisterRoute
@Serializable object ResultRoute
@Serializable object SettingsRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "faceverify-db"
        ).build()
        val repository = ProfileRepository(db.profileDao())
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: FaceVerifyViewModel = viewModel(
                        factory = ViewModelFactory(repository)
                    )

                    NavHost(navController = navController, startDestination = HomeRoute) {
                        composable<HomeRoute> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                                onNavigateToResult = { navController.navigate(ResultRoute) },
                                onNavigateToSettings = { navController.navigate(SettingsRoute) }
                            )
                        }
                        composable<RegisterRoute> {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<ResultRoute> {
                            ResultScreen(
                                viewModel = viewModel,
                                onNavigateHome = {
                                    navController.popBackStack(HomeRoute, false)
                                }
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
