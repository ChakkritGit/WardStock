package com.thanes.wardstock.screens.refill

import android.app.Application
import android.content.Context
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.RefillSharedViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefillScreen(
  navController: NavHostController,
  context: Context,
  sharedViewModel: RefillSharedViewModel
) {
  val viewModel: RefillViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as Application)
  )
  var canClick by remember { mutableStateOf(true) }
  var pullState by remember { mutableStateOf(false) }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = viewModel.isLoading,
    onRefresh = {
      viewModel.fetchRefill()
      pullState = true
    }
  )

  LaunchedEffect(viewModel.refillState) {
    if (viewModel.refillState.isEmpty()) {
      viewModel.fetchRefill()
    }
  }

  LaunchedEffect(viewModel.errorMessage) {
    if (viewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_SHORT).show()
      viewModel.errorMessage = ""
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.refill_medicine),
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
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      when {
        viewModel.isLoading && !pullState -> {
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

        viewModel.refillState.isEmpty() -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Image(
              painter = painterResource(R.drawable.empty),
              contentDescription = "ScanBanner",
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

          val filteredList = viewModel.refillState.filter {
            it.drugName?.contains(searchText, ignoreCase = true) == true ||
                    it.inventoryPosition.toString().contains(searchText)
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
                label = { Text(stringResource(R.string.search_drug)) },
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
                  AnimatedCardItem(index, item, navController, sharedViewModel)
                }
              }
            }
          }

          PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Colors.BlueGrey120,
            contentColor = Colors.BluePrimary
          )
        }
      }
    }
  }
}
