package com.thanes.wardstock.screens.manage.group

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.components.keyboard.Keyboard
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GroupTab(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel
) {
  val context = LocalContext.current
  var pullState by remember { mutableStateOf(false) }
  val hideKeyboard = Keyboard.hideKeyboard()

  val pullRefreshState = rememberPullRefreshState(
    refreshing = groupSharedViewModel.isLoading,
    onRefresh = {
      groupSharedViewModel.fetchGroup()
      pullState = true
    }
  )

  LaunchedEffect(groupSharedViewModel.groupInventoryState) {
    if (groupSharedViewModel.groupInventoryState.isEmpty()) {
      groupSharedViewModel.fetchGroup()
    }
  }

  LaunchedEffect(groupSharedViewModel.errorMessage) {
    if (groupSharedViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, groupSharedViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      groupSharedViewModel.errorMessage = ""
    }
  }

  Scaffold(
    topBar = {},
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { navController.navigate(Routes.AddMachine.route) },
        containerColor = Colors.BluePrimary,
        icon = {
          Icon(
            painter = painterResource(R.drawable.add_24px),
            contentDescription = "Search Icon",
            tint = Colors.BlueGrey80,
            modifier = Modifier.size(36.dp)
          )
        },
        text = {
          Text(
            stringResource(R.string.add_machine),
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
          )
        },
        shape = RoundedCornerShape(RoundRadius.Medium),
        modifier = Modifier.height(72.dp)
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Text("Group")
    }
  }
}