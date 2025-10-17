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
import com.pratik.learning.familyTree.presentation.screen.AddMemberScreen
import com.pratik.learning.familyTree.presentation.screen.AddRelationScreen
import com.pratik.learning.familyTree.presentation.screen.AncestryScreen
import com.pratik.learning.familyTree.presentation.screen.MemberDetailsScreen
import com.pratik.learning.familyTree.presentation.screen.EditMemberScreen
import com.pratik.learning.familyTree.presentation.screen.ProductScreen
import com.pratik.learning.familyTree.presentation.viewmodel.MemberDetailsViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.MembersViewModel
import com.pratik.learning.familyTree.presentation.viewmodel.ProductViewModel
import com.pratik.learning.familyTree.utils.sharedViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Home()
    ) {
        composable<Home> { backStackEntry ->
            val hiltViewModel = hiltViewModel<MembersViewModel>()
            val relation = backStackEntry.toRoute<Home>().relation
            hiltViewModel.relationType = relation
            MemberSearchScreen(
                onMemberSelected = { member ->
                    if (relation.isEmpty())
                        navController.navigate(route = DetailsRoute(member.first))
                    else {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedMemberId", member)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("relation", relation)
                        navController.popBackStack()
                    }
                },
                onAddMemberClick = {
                    navController.navigate(route = AddMember)
                },
                viewModel = hiltViewModel
            )
        }

        navigation<MemberDetailsGraph>(startDestination = DetailsRoute(memberId = 0)) {
            composable<DetailsRoute> { backStackEntry ->
                val memberId = backStackEntry.toRoute<DetailsRoute>()
                val membersViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersViewModel.memberId = memberId.memberId
                MemberDetailsScreen(navController, membersViewModel)
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

                val membersViewModel =
                    backStackEntry.sharedViewModel<MemberDetailsViewModel>(navController)
                membersViewModel.memberId = memberId.memberId
                AddRelationScreen(membersViewModel, navController, selectedPerson, relation)

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


            composable<ProductRoute> {
                val hiltViewModel = hiltViewModel<ProductViewModel>()
                ProductScreen(
                    onProductClick = { productId, _ ->
                        navController.navigate(route = AncestryRoute(productId))
                    },
                    viewModel = hiltViewModel
                )
            }
        }
    }
}
