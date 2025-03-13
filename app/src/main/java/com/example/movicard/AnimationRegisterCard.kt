package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class AnimationRegisterCard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernSuccessAnimation(
                onAnimationEnd = {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, BlockCard::class.java))
                        finish()
                    }, 500)
                }
            )
        }
    }
}

@Composable
fun ModernSuccessAnimation(onAnimationEnd: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (animationStarted) 360f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutBounce),
        label = "Rotation",
        finishedListener = { onAnimationEnd() }
    )

    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1.5f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "Scale"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    // 🔥 Fondo de la pantalla con un color personalizado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FA)), // Cambia el color aquí
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        AnimatedVisibility(
            visible = animationStarted,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.5f, animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size((100 * scale).dp)
                        .rotate(rotation)
                ) {
                    val gradient = Brush.radialGradient(
                        colors = listOf(Color(0xFF34C759), Color(0xFF2ECC71)),
                        center = center,
                        radius = size.minDimension / 2
                    )

                    drawCircle(
                        brush = gradient,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 20f, cap = StrokeCap.Round)
                    )

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = size.minDimension / 2 + 10f,
                        style = Stroke(width = 24f)
                    )

                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.55f)
                        quadraticBezierTo(
                            size.width * 0.35f, size.height * 0.75f,
                            size.width * 0.45f, size.height * 0.7f
                        )
                        quadraticBezierTo(
                            size.width * 0.75f, size.height * 0.2f,
                            size.width * 0.85f, size.height * 0.3f
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.3f),
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = path,
                        color = Color(0xFF34C759),
                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

