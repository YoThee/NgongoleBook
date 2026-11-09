package com.zuct.ngongolebook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zuct.ngongolebook.NgongoleApp
import com.zuct.ngongolebook.ui.add.AddDebtScreen
import com.zuct.ngongolebook.ui.add.AddDebtViewModel
import com.zuct.ngongolebook.ui.detail.DebtDetailScreen
import com.zuct.ngongolebook.ui.detail.DebtDetailViewModel
import com.zuct.ngongolebook.ui.list.DebtListScreen
import com.zuct.ngongolebook.ui.list.DebtListViewModel
import com.zuct.ngongolebook.ui.settings.SettingsScreen
import com.zuct.ngongolebook.ui.settings.SettingsViewModel

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val DEBT_ID = "debtId"
    const val DETAIL = "detail/{$DEBT_ID}"
    fun detail(id: Long) = "detail/$id"
}

/**
 * All navigation lives here. Each destination gets its own ViewModel, scoped to that
 * back-stack entry, so pressing Back returns to the previous screen exactly as it was.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as NgongoleApp

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            val vm: DebtListViewModel = viewModel(factory = DebtListViewModel.factory(app.repository))
            DebtListScreen(
                viewModel = vm,
                onAddDebt = { navController.navigate(Routes.ADD) },
                onOpenDebt = { id -> navController.navigate(Routes.detail(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADD) {
            val vm: AddDebtViewModel = viewModel(factory = AddDebtViewModel.factory(app.repository))
            AddDebtScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.DEBT_ID) { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong(Routes.DEBT_ID) ?: 0L
            val vm: DebtDetailViewModel =
                viewModel(factory = DebtDetailViewModel.factory(app.repository, id))
            DebtDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.reminders))
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
