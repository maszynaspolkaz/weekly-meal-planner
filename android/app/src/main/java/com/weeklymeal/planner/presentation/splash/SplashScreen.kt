package com.weeklymeal.planner.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklymeal.planner.R
import com.weeklymeal.planner.core.di.ServiceLocator
import com.weeklymeal.planner.core.theme.*
import com.weeklymeal.planner.core.ui.AppBackdrop

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.state.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val infinite = rememberInfiniteTransition(label = "kitchen")
    val float by infinite.animateFloat(-5f, 5f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val rotation by infinite.animateFloat(-4f, 4f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "leaf")
    val motion = LocalDecorativeMotion.current
    AppBackdrop(0) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.eyebrow), style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Surface(shape = RoundedCornerShape(38.dp), color = Surface.copy(alpha = 0.68f), shadowElevation = 8.dp,
                modifier = Modifier.graphicsLayer { translationY = if (motion) float else 0f; rotationZ = if (motion) rotation else 0f }) {
                Image(painterResource(R.drawable.logo), null, Modifier.padding(18.dp).size(148.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(Modifier.size(32.dp), color = Action, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(if (state.finished) R.string.kitchen_ready else R.string.splash_loading), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
    }
}
