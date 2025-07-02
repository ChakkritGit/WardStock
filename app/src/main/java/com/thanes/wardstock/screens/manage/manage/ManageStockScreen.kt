package com.thanes.wardstock.screens.manage.manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.screens.manage.group.GroupTab
import com.thanes.wardstock.screens.manage.manage.Tabs
import com.thanes.wardstock.screens.manage.inventory.InventoryTab
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStockScreen(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  val tabNavController = rememberNavController()
  var selectedTabIndex by rememberSaveable { mutableIntStateOf(Tabs.Inventory.ordinal) }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.stock_management),
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      Column {
        PrimaryTabRow(
          selectedTabIndex = selectedTabIndex,
          modifier = Modifier.padding(innerPadding),
          containerColor = Colors.BlueGrey100,
          divider = {
            HorizontalDivider(color = Colors.BlueGrey80)
          }
        ) {
          Tabs.Companion.entries.forEachIndexed { index, tab ->
            Tab(
              selected = selectedTabIndex == index,
              onClick = {
                selectedTabIndex = index
                tabNavController.navigate(tab.route) {
                  popUpTo(tabNavController.graph.startDestinationId) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = {
                if (index == 0) {
                  Icon(
                    painter = painterResource(R.drawable.box_24px),
                    contentDescription = "box_24px",
                    tint = if (selectedTabIndex == index) Colors.BluePrimary else Colors.BlueGrey40,
                    modifier = Modifier
                      .size(32.dp)
                      .padding(bottom = 6.dp)
                  )
                } else {
                  Icon(
                    painter = painterResource(R.drawable.stack_group_24px),
                    contentDescription = "stack_group_24px",
                    tint = if (selectedTabIndex == index) Colors.BluePrimary else Colors.BlueGrey40,
                    modifier = Modifier
                      .size(32.dp)
                      .padding(bottom = 6.dp)
                  )
                }
              },
              text = {
                Text(
                  text = stringResource(tab.labelRes),
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  fontSize = 20.sp,
                  fontWeight = if (selectedTabIndex == index) FontWeight.Medium else FontWeight.Normal,
                  color = if (selectedTabIndex == index) Colors.BluePrimary else Colors.BlueGrey40,
                  fontFamily = ibmpiexsansthailooped
                )
              }
            )
          }
        }

        NavHost(
          navController = tabNavController,
          startDestination = Tabs.Inventory.route,
          modifier = Modifier.weight(1f)
        ) {
          composable(Tabs.Inventory.route) {
            InventoryTab(
              navController,
              inventorySharedViewModel
            )
          }
          composable(Tabs.Group.route) {
            GroupTab(
              navController,
              inventorySharedViewModel,
              groupSharedViewModel
            )
          }
        }
      }
    }
  }
}
