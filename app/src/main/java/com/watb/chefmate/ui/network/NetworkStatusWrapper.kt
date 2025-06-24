package com.watb.chefmate.ui.network


import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watb.chefmate.R
import com.watb.chefmate.global.GlobalApplication
import com.watb.chefmate.ui.theme.SecondaryTextButtonTheme
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkStatusWrapper(
    activity: Activity,
    content: @Composable () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val networkMonitor = GlobalApplication.networkMonitor
    val isConnected = networkMonitor?.isConnected?.collectAsState()?.value ?: true

    Log.d("NetworkStatusWrapper", "isConnected: $isConnected, networkMonitor null: ${networkMonitor == null}")

    LaunchedEffect(isConnected) {
        Log.d("NetworkStatusWrapper", "LaunchedEffect triggered with isConnected: $isConnected")
        if (!isConnected) {
            Log.d("NetworkStatusWrapper", "Showing NoInternetConnectionBottomSheet")
            showBottomSheet = true
        } else if (showBottomSheet) {
            Log.d("NetworkStatusWrapper", "Hiding NoInternetConnectionBottomSheet")
            sheetState.hide()
            showBottomSheet = false
        }
    }

    content()

    if (showBottomSheet) {
        NoInternetConnectionBottomSheet(
            activity = activity,
            sheetState = sheetState,
            onDismiss = {
                Log.d("NetworkStatusWrapper", "BottomSheet dismissed")
                showBottomSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoInternetConnectionBottomSheet(
    activity: Activity,
    sheetState: SheetState,
    onDismiss: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_circle_x),
                        contentDescription = null,
                        tint = Color(0xFF5A5A60),
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFFD7EDED),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFB2D8D8),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_no_internet_connection),
                    contentDescription = "No internet connection",
                    modifier = Modifier
                        .size(70.dp)
                )
            }
            Text(
                text = "Không có kết nối internet",
                color = Color(0xFF1B1B1D),
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            Text(
                text = "Vui lòng kiểm tra kết nối internet và thử lại",
                color = Color(0xFF5A5A60),
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.8f)
            )
            SecondaryTextButtonTheme(
                onClick = {
                    onDismiss()
                    activity.finishAffinity()
                    exitProcess(0)
                },
                text = "Đóng ứng dụng",
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
