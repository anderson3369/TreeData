package com.orchardlog.treedata.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Light palette
private val LightColors = lightColors(
    primary = Color(0xFF2E7D32),        // Forest Green
    primaryVariant = Color(0xFF1B5E20), // Dark Forest Green
    secondary = Color(0xFF8D6E63),      // Warm Brown
    secondaryVariant = Color(0xFF5D4037),
    background = Color(0xFFFAFAF5),     // Warm White
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFC62828),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onError = Color.White
)

// Dark palette
private val DarkColors = darkColors(
    primary = Color(0xFF66BB6A),        // Lighter Green for dark mode
    primaryVariant = Color(0xFF1B5E20),
    secondary = Color(0xFFA1887F),      // Lighter Brown for dark mode
    secondaryVariant = Color(0xFF5D4037),
    background = Color(0xFF1A1C18),     // Dark Green-Black
    surface = Color(0xFF2D312A),        // Dark Olive
    error = Color(0xFFEF5350),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE3E3DC),
    onSurface = Color(0xFFE3E3DC),
    onError = Color.Black
)

// Rounded shapes matching iOS cornerRadius(10)
private val TreeDataShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(10.dp)
)

@Composable
fun TreeDataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colors = colors,
        shapes = TreeDataShapes,
        content = content
    )
}
