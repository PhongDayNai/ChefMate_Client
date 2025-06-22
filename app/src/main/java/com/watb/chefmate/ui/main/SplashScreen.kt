package com.watb.chefmate.ui.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiConstant
import com.watb.chefmate.helper.DataStoreHelper

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    var isShowDialog by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoggedIn = DataStoreHelper.isLoggedIn(context = context)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            onClick = {
                if (ApiConstant.MAIN_URL == "") {
                    isShowDialog = true
                } else {
                    if (isLoggedIn) {
                        navController.navigate("mainAct") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate("signIn") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize(0.9f)
        ) {
            Text(
                text = "Vào",
                color = colorResource(id = R.color.white),
                fontSize = 32.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
        }

        if (isShowDialog) {
            EnterMainLink(
                onDismiss = { isShowDialog = false },
            )
        }
    }
}

@Composable
fun EnterMainLink(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .background(color = colorResource(id = R.color.white), shape = RoundedCornerShape(4.dp))
            ) {
                val (textRef, buttonRef) = createRefs()
                val horizontalGuideline = createGuidelineFromTop(0.5f)
                var link by remember { mutableStateOf("") }

                TextField(
                    value = link,
                    onValueChange = { newString ->
                        link = newString
                    },
                    modifier = Modifier.constrainAs(textRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(horizontalGuideline)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                Button(
                    onClick = {
                        ApiConstant.setMainUrl(link)
                        Log.d("URL", ApiConstant.MAIN_URL)
                    },
                    modifier = Modifier.constrainAs(buttonRef) {
                        top.linkTo(horizontalGuideline)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                ) {
                    Text(
                        text = "Vào"
                    )
                }
            }
        }
    }
}
