package com.thanes.wardstock.navigation

sealed  class Routes(val route: String) {
  object Login: Routes(route = "login_route")
  object Home: Routes(route = "home_route")
  object Setting: Routes(route = "setting_route")
}