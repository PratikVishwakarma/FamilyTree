package com.pratik.learning.familyTree.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pratik.learning.familyTree.R
import com.pratik.learning.familyTree.navigation.Home
import com.pratik.learning.familyTree.presentation.component.AnimatedAppLogo
import com.pratik.learning.familyTree.presentation.component.NoInternetScreen
import com.pratik.learning.familyTree.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    navController: NavController
) {
    val scale = remember { Animatable(0.5f) }
    val mAlpha = remember { Animatable(0f) }

    val isDataLoaded = viewModel.isDataLoaded.collectAsState().value
    val isInternetRequired = viewModel.isInternetRequired.collectAsState().value

    // Navigate to home when data loaded
    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            navController.navigate(Home) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // Animate logo
    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            mAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    when {
        isInternetRequired -> {
            NoInternetScreen(
                onRetry = { viewModel.retryDataSync() }
            )
        }

        else -> {
            // Splash UI with loader
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E88E5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_background), // Your background image resource
                    contentDescription = "Background Image",
                    contentScale = ContentScale.FillBounds, // Scales the image to fill the bounds of the Box
                    modifier = Modifier.matchParentSize() // Stretches the image to fill the Box size
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_app_logo),
//                        contentDescription = "App Logo",
//                        modifier = Modifier
//                            .size(120.dp)
//                            .graphicsLayer {
//                                scaleX = scale.value
//                                scaleY = scale.value
//                                alpha = mAlpha.value
//                            }
//                    )
                    AnimatedAppLogo()

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isDataLoaded) {
//                        CircularProgressIndicator(
//                            color = Color.White,
//                            strokeWidth = 3.dp
//                        )
                        Text(
                            text = "Data Loading....",
                            style = typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

