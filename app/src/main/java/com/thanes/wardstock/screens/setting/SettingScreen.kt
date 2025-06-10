package com.thanes.wardstock.screens.setting

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.screens.setting.dispense.DispenseTestToolList
import com.thanes.wardstock.screens.setting.language.LanguageSwitcher

@Composable
fun SettingScreen(navController: NavHostController, context: Context) {
  var userData by remember { mutableStateOf<UserData?>(null) }

  LaunchedEffect(Unit) {
    userData = DataManager.getUserData(context)
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.settings),
        onBack = { navController.popBackStack() }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column {
        LanguageSwitcher(context)
        Spacer(modifier = Modifier.height(6.dp))
        if (userData != null && userData?.role == "SUPER") {
          DispenseTestToolList(navController)
        }
      }
    }
  }
}
