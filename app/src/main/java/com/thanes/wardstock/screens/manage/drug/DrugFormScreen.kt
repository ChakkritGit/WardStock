package com.thanes.wardstock.screens.manage.drug

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.DrugViewModel

data class DrugFormState(
  val id: String = "",
  val drugCode: String = "",
  val drugName: String = "",
  val unit: String = "",
  val drugLot: String = "",
  val drugExpire: String = "",
  val drugPriority: String = "",
  val weight: String = "",
  val status: String = "",
  val picture: String = "",
  val comment: String = ""
)

@Composable
fun DrugFormScreen(
  context: Context,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  navController: NavHostController?,
  drugSharedViewModel: DrugViewModel?,
  initialData: DrugFormState? = null,
  onSubmit: suspend (DrugFormState, Uri?) -> Boolean
) {

}