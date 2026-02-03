package com.pratik.learning.familyTree.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val DarkVibrantColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = OnDarkPrimary,
    primaryContainer = TealPrimaryContainer,

    secondary = AmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AmberSecondaryContainer,

    tertiary = PurpleTertiary,
    tertiaryContainer = PurpleTertiaryContainer,

    background = DarkBackground,
    onBackground = OnDarkSurface,

    surface = DarkSurface,
    onSurface = OnDarkSurface,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,

    outline = Color(0xFF4A4F55)
)

private val LightVibrantColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),

    secondary = Color(0xFFF9A825),
    onSecondary = Color.Black,

    tertiary = Color(0xFF7B1FA2),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFE7E7E7),
    onSurfaceVariant = Color(0xFF4A4A4A),

    outline = Color(0xFF8A8A8A)
)


//@Composable
//fun FamilyTreeTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme.colorScheme.copy(
//        primary = Color(0xFF80CBC4),
//        surfaceVariant = Color(0xFF2C2C2C),
//        onSurfaceVariant = Color(0xFFB0B0B0)
//    )
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}

@Composable
fun FamilyTreeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // recommend OFF for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkVibrantColorScheme
        else -> LightVibrantColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}