package com.unsika.waterfilterapp.navigation

sealed class Screen(var route: String) {
    object Home : Screen("home_screen")
}