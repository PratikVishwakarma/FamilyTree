package com.pratik.learning.familyTree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pratik.learning.familyTree.navigation.About
import com.pratik.learning.familyTree.navigation.Admin
import com.pratik.learning.familyTree.navigation.AppNavigation
import com.pratik.learning.familyTree.navigation.Home
import com.pratik.learning.familyTree.navigation.MySpace
import com.pratik.learning.familyTree.navigation.SplashRoute
import com.pratik.learning.familyTree.presentation.FancyBottomItem
import com.pratik.learning.familyTree.presentation.component.FancyBottomBar
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.ui.theme.FamilyTreeTheme
import com.pratik.learning.familyTree.utils.logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue


val rootRouteNames = setOf(
    Home::class.qualifiedName,
    MySpace::class.qualifiedName,
    Admin::class.qualifiedName,
    About::class.qualifiedName
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appVM: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVM.closeAppEvent.collect {
                    logger("closeApp event received")
                    finish()
                }
            }
        }
        FamilyTreeApp.isAdmin = appVM.getCurrentUser() != null
        setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val isSplash = currentRoute == SplashRoute::class.qualifiedName

            val isRootDestination = currentRoute in rootRouteNames

            val items = listOf(
                FancyBottomItem(MySpace, "My Space", Icons.Default.Person),
                FancyBottomItem(Home, "Search", Icons.Default.Search),
                FancyBottomItem(Admin, "Admin", Icons.Default.AdminPanelSettings),
                FancyBottomItem(About, "About", Icons.Default.Info)
            )

            val selectedIndex = items.indexOfFirst {
                backStackEntry?.destination?.route == it.route::class.qualifiedName
            }.takeIf { it >= 0 } ?: 1/*(items.size / 2)*/ // Search index

            FamilyTreeTheme {
                BackHandler(isRootDestination) {
                    finish()
                }
                Scaffold(
                    bottomBar = {
                        if (!isSplash)
                            FancyBottomBar(
                                items = items,
                                selectedIndex = selectedIndex,
                                onItemSelected = { index ->
                                    if (currentRoute != items[index].route::class.qualifiedName) {
                                        navController.navigate(items[index].route) {
                                            popUpTo(Home::class.qualifiedName!!) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 30.dp)
                    ) {
                        AppNavigation(navController, appVM)
                    }
                }
            }
        }
    }
}
