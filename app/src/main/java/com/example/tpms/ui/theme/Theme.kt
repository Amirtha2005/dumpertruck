

package com.example.tpms.ui.theme

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

// Define your blue color palette
//private val DarkColorScheme = darkColorScheme(
//    primary = Color(0xFF1976D2), // Blue
//    onPrimary = Color(0xFFF2F2F2),
//    primaryContainer = Color(0xFFBBDEFB), // Light blue
//    onPrimaryContainer = Color(0xFF0D47A1), // Darker blue
//    secondary = Color(0xFF0288D1), // Light blue
//    onSecondary = Color.White,
//    secondaryContainer = Color(0xFFB3E5FC), // Lighter blue
//    onSecondaryContainer = Color(0xFF01579B), // Darker blue
//    background = Color(0xFFAFAFAF), // Dark background
//    onBackground = Color.White,
//    surface = Color(0xFF121212), // Dark surface
//    onSurface = Color.White
//)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2), // Blue
    onPrimary = Color(0xFFF2F2F2),
    primaryContainer = Color(0xFFBBDEFB), // Light blue
    onPrimaryContainer = Color(0xFF0D47A1), // Darker blue
    secondary = Color(0xFF03A9F4), // Light blue
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC), // Lighter blue
    onSecondaryContainer = Color(0xFF01579B), // Darker blue
    background = Color(0xFFF5F5F5), // Light background
    onBackground = Color(0xFF0D47A1), // Darker blue text
    surface = Color.White,
    onSurface = Color.Black
)

//@Composable
//fun TpmsTheme(
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
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}

@Composable
fun TpmsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Always use light color scheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
