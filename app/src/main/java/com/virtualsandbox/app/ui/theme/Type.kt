package com.virtualsandbox.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

val SandboxTypography = Typography(
    bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.Default),
    titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.Default),
    labelLarge = Typography().labelLarge.copy(fontFamily = FontFamily.Default)
)
