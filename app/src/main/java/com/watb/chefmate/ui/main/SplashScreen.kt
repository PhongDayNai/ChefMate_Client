package com.watb.chefmate.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.AppConstant

@Composable
fun SplashScreen(navController: NavController) {
    val progress = remember { Animatable(0f) }
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )

        if (!hasNavigated) {
            hasNavigated = true
            navController.navigate("mainAct") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = AppConstant.backgroundGradient)
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(150.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.splash_logo_content_description),
                        modifier = Modifier
                            .size(96.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.splash_app_title),
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp)
            )

            Text(
                text = stringResource(R.string.splash_loading_message),
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier
                    .padding(top = 6.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = progress.value,
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(8.dp)
                    .background(Color.Transparent, RoundedCornerShape(12.dp))
            )
            Text(
                text = "${(progress.value * 100).toInt()}%",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
