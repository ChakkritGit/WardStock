package com.thanes.wardstock.screens.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.navigation.Routes
import kotlinx.coroutines.launch
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun HomeScreen(navController: NavHostController, context: Context) {
  val scope = rememberCoroutineScope()

  Scaffold(
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxHeight()
    ) {
      Column(modifier = Modifier
        .fillMaxHeight()
        .padding(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(stringResource(R.string.app_name))
          Button(onClick = {
            scope.launch {
              DataManager.clearAll(context)

              navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
              }
            }
          }) {
            Image(
              painter = painterResource(id = R.drawable.baseline_logout_24),
              contentDescription = stringResource(R.string.app_name),
              modifier = Modifier
                .height(30.dp)
                .width(30.dp),
              contentScale = ContentScale.Fit,
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
          navController.navigate(Routes.Setting.route) {
            popUpTo(Routes.Home.route) { inclusive = false }
          }
        }) {
          Text(stringResource(R.string.settings))
        }
      }
    }
  }
}
