package com.thanes.wardstock.screens.manage.group

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InventoryTab(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel
) {
  val context = LocalContext.current
  var pullState by remember { mutableStateOf(false) }
  val hideKeyboard = Keyboard.hideKeyboard()

  val pullRefreshState = rememberPullRefreshState(
    refreshing = inventorySharedViewModel.isLoading,
    onRefresh = {
      inventorySharedViewModel.fetchInventory()
      pullState = true
    }
  )

  LaunchedEffect(inventorySharedViewModel.inventoryState) {
    if (inventorySharedViewModel.inventoryState.isEmpty()) {
      inventorySharedViewModel.fetchInventory()
    }
  }

  LaunchedEffect(inventorySharedViewModel.errorMessage) {
    if (inventorySharedViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, inventorySharedViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      inventorySharedViewModel.errorMessage = ""
    }
  }

  Scaffold(
    topBar = {},
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { navController.navigate(Routes.AddInventory.route) },
        containerColor = Colors.BluePrimary,
        icon = {
          Icon(
            painter = painterResource(R.drawable.box_24px),
            contentDescription = "box_24px",
            tint = Colors.BlueGrey80,
            modifier = Modifier.size(36.dp)
          )
        },
        text = {
          Text(
            stringResource(R.string.add_inventory),
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
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when {
        inventorySharedViewModel.isLoading && inventorySharedViewModel.inventoryState.isEmpty() && !pullState -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(
              color = Colors.BluePrimary,
              strokeWidth = 3.dp,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
              stringResource(R.string.is_Loading),
              fontSize = 20.sp,
              color = Colors.BluePrimary,
              fontWeight = FontWeight.Medium,
              fontFamily = ibmpiexsansthailooped
            )
          }
        }

        inventorySharedViewModel.inventoryState.isEmpty() -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Image(
              painter = painterResource(R.drawable.empty),
              contentDescription = "Empty",
              modifier = Modifier
                .width(320.dp)
                .height(320.dp),
              contentScale = ContentScale.Fit,
            )
            Text(
              stringResource(R.string.empty_data),
              fontSize = 24.sp,
              color = Colors.BluePrimary,
              fontWeight = FontWeight.Medium,
              fontFamily = ibmpiexsansthailooped
            )
          }
        }

        else -> {
          var searchText by remember { mutableStateOf("") }

          val filteredList = inventorySharedViewModel.inventoryState.filter {
            it.position.toString().contains(searchText, ignoreCase = true)
          }

          Column(modifier = Modifier.fillMaxSize()) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
            ) {
              OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text(stringResource(R.string.search_drug_two)) },
                modifier = Modifier
                  .padding(horizontal = 14.dp, vertical = 8.dp)
                  .fillMaxWidth()
                  .height(60.dp),
                shape = RoundedCornerShape(RoundRadius.Large),
                textStyle = TextStyle(fontSize = 20.sp),
                leadingIcon = {
                  Icon(
                    painter = painterResource(R.drawable.search_24px),
                    contentDescription = "Search Icon",
                    tint = Colors.BlueGrey40,
                    modifier = Modifier.size(32.dp)
                  )
                },
                trailingIcon = {
                  if (searchText.isNotEmpty()) {
                    IconButton(
                      onClick = { searchText = "" },
                      modifier = Modifier.padding(end = 8.dp)
                    ) {
                      Icon(
                        painter = painterResource(R.drawable.close_24px),
                        contentDescription = "Clear Search",
                        tint = Colors.BlueGrey40,
                        modifier = Modifier.size(32.dp)
                      )
                    }
                  }
                },
                singleLine = true,
                maxLines = 1,
                keyboardActions = KeyboardActions(
                  onDone = {
                    hideKeyboard()
                  }),
                colors = TextFieldDefaults.colors(
                  focusedTextColor = Colors.BlueSecondary,
                  focusedIndicatorColor = Colors.BlueSecondary,
                  unfocusedIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f),
                  focusedLabelColor = Colors.BlueSecondary,
                  unfocusedLabelColor = Colors.BlueGrey40,
                  cursorColor = Colors.BlueSecondary,
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  disabledContainerColor = Color.Transparent,
                  errorContainerColor = Color.Transparent
                )
              )

              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(top = 8.dp)
              ) {
                itemsIndexed(filteredList) { index, item ->
//                AnimatedMachineItem(index, item, filteredList, onClick = {
//                  inventorySharedViewModel.setInventory(item)
//                  navController.navigate(Routes.EditMachine.route)
//                })
                }
              }
            }
          }

          PullRefreshIndicator(
            refreshing = inventorySharedViewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Colors.BlueGrey120,
            contentColor = Colors.BluePrimary,
            scale = true,
          )
        }
      }
    }
  }
}