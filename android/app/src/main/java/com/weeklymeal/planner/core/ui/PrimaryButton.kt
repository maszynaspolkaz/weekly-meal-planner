package com.weeklymeal.planner.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weeklymeal.planner.core.theme.*

@Composable
fun PrimaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, busy: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled && LocalDecorativeMotion.current) 0.98f else 1f, spring(0.85f, 600f), label = "press")
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick, enabled = enabled && !busy, interactionSource = interaction,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale)
            .shadow(if (enabled) 6.dp else 0.dp, shape, ambientColor = Accent.copy(alpha = 0.16f), spotColor = Accent.copy(alpha = 0.16f))
            .background(Brush.horizontalGradient(if (enabled) listOf(Action, ActionDeep) else listOf(BorderSage, BorderSage)), shape)
            .border(1.dp, if (enabled) Accent.copy(alpha = 0.7f) else BorderSage, shape),
        shape = shape, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Surface, disabledContainerColor = Color.Transparent, disabledContentColor = if (busy && enabled) Surface else TextSecondary),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (busy) { CircularProgressIndicator(Modifier.size(20.dp), color = Surface, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = enabled,
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderSage),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface.copy(alpha = 0.92f), contentColor = Action, disabledContainerColor = BorderSage, disabledContentColor = TextSecondary),
        contentPadding = PaddingValues(14.dp)) { Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun ActionText(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = Action, disabledContentColor = TextSecondary)) {
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}
