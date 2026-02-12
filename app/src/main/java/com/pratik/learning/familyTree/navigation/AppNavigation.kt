package com.pratik.learning.familyTree.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pratik.learning.familyTree.presentation.screen.MemberSearchScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.pratik.learning.familyTree.presentation.screen.AboutAppScreen
import com.pratik.learning.familyTree.presentation.screen.AddMemberScreen
import com.pratik.learning.familyTree.presentation.screen.AddRelationScreen
import com.pratik.learning.familyTree.presentation.screen.AdminScreen
import com.pratik.learning.familyTree.presentation.screen.AncestryScreen
import com.pratik.learning.familyTree.presentation.screen.MemberDetailsScreen
import com.pratik.learning.familyTree.presentation.screen.EditMemberScreen
import com.pratik.learning.familyTree.presentation.screen.MemberCompareScreen
import com.pratik.learning.familyTree.presentation.screen.MemberTimelineScreen
import com.pratik.learning.familyTree.presentation.screen.MySpaceScreen
import com.pratik.learning.familyTree.presentation.screen.SplashScreen
import com.pratik.learning.familyTree.presentation.viewmodel.AppViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.SplashViewModel
import com.pratik.learning.familyTree.utils.sharedViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController(), appVM: AppViewModel) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            val vm = hiltViewModel<SplashViewModel>()
            SplashScreen(vm, navController)
        }
        composable<Home> { backStackEntry ->
            val membersViewModel = hiltViewModel<MembersViewModel>()
            MemberSearchScreen(
//                onMemberSelected = { member ->
//                    navController.navigate(route = MemberDetailsRoute(member.first))
////                    if (relation.isEmpty())
////                        navController.navigate(route = MemberDetailsRoute(member.first))
////                    else {
////                        navController.previousBackStackEntry
////                            ?.savedStateHandle
////                            ?.set("selectedMemberId", member)
////                        navController.previousBackStackEntry
////                            ?.savedStateHandle
////                            ?.set("relation", relation)
////                        navController.popBackStack()
////                    }
//                },
                navController,
                viewModel = membersViewModel,
                appVM = appVM
            )
        }
        navigation<MemberDetailsGraph>(startDestination = MemberDetailsRoute(memberId = 0)) {
            composable<MemberDetailsRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<MemberDetailsRoute>()
                val membersViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersViewModel.memberId = memberId.memberId
                MemberDetailsScreen(navController, membersViewModel, appVM = appVM)
            }

            composable<AncestryRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<AncestryRoute>()
                val membersViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersViewModel.memberId = memberId.memberId
                AncestryScreen(navController, membersViewModel)
            }

            composable<EditMemberRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<EditMemberRoute>()
                val membersViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersViewModel.memberId = memberId.memberId
                EditMemberScreen(membersViewModel, navController)
            }

            composable<AddRelationRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<AddRelationRoute>()
                var selectedPerson by rememberSaveable { mutableStateOf<Pair<Int, String>?>(null) }
                var relation by rememberSaveable { mutableStateOf("") }

                val membersViewModel = hiltViewModel<MembersViewModel>()
                val detailsViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                detailsViewModel.memberId = memberId.memberId
                AddRelationScreen(membersViewModel, detailsViewModel, navController)

                // Listen for result from SearchPersonScreen
                val savedStateHandle = navController.currentBackStackEntry
                    ?.savedStateHandle
                savedStateHandle
                    ?.getLiveData<Pair<Int, String>>("selectedMemberId")
                    ?.observe(LocalLifecycleOwner.current) { member ->
                        selectedPerson = member
                    }
                savedStateHandle
                    ?.getLiveData<String>("relation")
                    ?.observe(LocalLifecycleOwner.current) { relationType ->
                        relation = relationType
                    }
            }

            composable<AddMember> {
                val membersViewModel = hiltViewModel<MembersViewModel>()
                AddMemberScreen(membersViewModel, navController)
            }

            composable<MembersCompareRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<MemberDetailsRoute>()
                val membersDetailsViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersDetailsViewModel.memberId = memberId.memberId
                val membersViewModel = hiltViewModel<MembersViewModel>()
                MemberCompareScreen(membersDetailsViewModel, membersViewModel)
            }

            composable<MemberTimelineRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<MemberDetailsRoute>()
                val membersDetailsViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersDetailsViewModel.memberId = memberId.memberId
                MemberTimelineScreen(membersDetailsViewModel)
            }
            composable<Admin> { backStackEntry ->
                AdminScreen(navController = navController, appVM = appVM)
            }
            composable<MySpace> { backStackEntry ->
                MySpaceScreen(appVM = appVM, navController = navController)
            }
            composable<About> { backStackEntry ->
                AboutAppScreen(appVM = appVM)
            }
        }
    }
}
