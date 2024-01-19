package com.unsika.waterfilterapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unsika.waterfilterapp.navigation.Screen.Home
import com.unsika.waterfilterapp.ui.screen.home.HomeScreen

@Composable
fun WaterFilterApp() {
    val navHostController = rememberNavController()

    NavHost(
        navController = navHostController,
        startDestination = Home.route
    ) {
        composable(Home.route) {
            HomeScreen(navHostController)
        }
    }
}