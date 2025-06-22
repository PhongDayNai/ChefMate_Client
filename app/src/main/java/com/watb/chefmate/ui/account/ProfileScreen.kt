package com.watb.chefmate.ui.account

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.data.AppConstant
import com.watb.chefmate.helper.CommonHelper
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.ui.theme.SecondaryTextButtonTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isShownLogoutBottomSheet by remember { mutableStateOf(false) }
    var isRating by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(brush = AppConstant.backgroundProfileGradient)
            .safeDrawingPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(0.9f)
        ) {
            Text(
                text = "Trang cá nhân",
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                color = Color(0xFFFFFFFF),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(color = Color(0x3FFFFFFF), shape = CircleShape)
                    .border(width = 1.dp, color = Color(0xFF85D0B6), shape = CircleShape)
            ) {
                IconButton(
                    onClick = { isShownLogoutBottomSheet = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = null,
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }
        }
        ProfileInformationCard(
            userName = "Dương Hùng Phong",
            phoneNumber = "0855576569",
            email = "dhphong266@gmail.com",
            followCount = 1520,
            recipeCount = 10,
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F8FC)
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            ),
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
            ) {
                Text(
                    text = "Cài đặt",
                    color = Color(0xFF1B1B1D),
                    fontSize = 18.sp,
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(0.9f)
                )
                Settings(
                    navController = navController,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .fillMaxWidth(0.9f),
                ) {
                    isRating = true
                }
            }
        }
        if (isShownLogoutBottomSheet) {
            LogoutBottomSheet(
                navController = navController,
                onLogout = {
                    coroutineScope.launch {
                        isShownLogoutBottomSheet = false
                        DataStoreHelper.clearLoginState(context = context)
                        navController.navigate("signIn") {
                            popUpTo("signIn") {
                                inclusive = false
                            }
                        }
                    }
                },
                onDismiss = { isShownLogoutBottomSheet = false }
            )
        }
        if (isRating) {
            RatingDialog(onDismiss = { isRating = false })
        }
    }
}

@Composable
fun Settings(
    navController: NavController,
    modifier: Modifier = Modifier,
    onRating: () -> Unit
) {
    val icons = listOf(
        R.drawable.ic_shopping_history,
        R.drawable.ic_privacy_policy,
        R.drawable.ic_terms_of_use,
        R.drawable.ic_help_and_report,
        R.drawable.ic_rate,
    )
    val labels = listOf(
        "Lịch sử mua sắm",
        "Chính sách bảo mật",
        "Điều khoản sử dụng",
        "Hỗ trợ và báo cáo",
        "Đánh giá"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE1E1E3).copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        labels.forEachIndexed { index, label ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFFE1E1E3).copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(0.9f)
                    .clickable {
                        when (index) {
                            4 -> onRating()
//                            else -> navController.navigate(labels[index])
                        }
                    }
            ) {
                Icon(
                    painter = painterResource(id = icons[index]),
                    contentDescription = null,
                    tint = Color(0xFF5A5A60),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(14.dp)
                )
                Text(
                    text = label,
                    color = Color(0xFF1B1B1D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Color(0xFF5A5A60),
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun ProfileInformationCard(
    userName: String,
    phoneNumber: String,
    email: String,
    followCount: Int,
    recipeCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
//                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_chef_avatar),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, Color(0xFFE0E0E0), shape = CircleShape)
                            .clip(CircleShape)
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = userName,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = "${CommonHelper.parseNumber(followCount)} người theo dõi",
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                            color = Color(0xFF5A5A60)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_phone),
                                contentDescription = "Phone",
                                tint = Color(0xFF5A5A60),
                            )
                            Text(
                                text = "+84 ${phoneNumber.drop(1)}",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                                color = Color(0xFF5A5A60),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mail),
                                contentDescription = "Phone",
                                tint = Color(0xFF5A5A60),
                            )
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                                color = Color(0xFF5A5A60),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { /*TODO*/ }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        tint = Color(0xFF2E8D8C),
                    )
                    Text(
                        text = "Sửa",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        color = Color(0xFF2E8D8C),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .background(color = Color(0xFFE9EBEE), RoundedCornerShape(10.dp))
                    .border(width = 1.dp, color = Color(0xFFE1E1E3), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_recipe),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Color(0xFFD7EDED), shape = CircleShape)
                        .border(1.dp, color = Color(0xFFFFFFFF), shape = CircleShape)
                        .padding(8.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                ) {
                    val annotatedString = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1B1B1D),
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
                            )
                        ) {
                            append("Bạn đã đăng ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF1B1B1D),
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                            )
                        ) {
                            append("$recipeCount công thức")
                        }
                    }
                    Text(text = annotatedString)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { /*TODO*/ }
                    ) {
                        Text(
                            text = "Xem tất cả công thức",
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            color = Color(0xFF2E8D8C),
                            textDecoration = TextDecoration.Underline,
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right),
                            contentDescription = null,
                            tint = Color(0xFF2E8D8C),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDialog(onDismiss: () -> Unit) {
    var numberOfStar by remember { mutableStateOf(0) }
    val isSubmitEnabled by remember { derivedStateOf { numberOfStar > 0 } }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ConstraintLayout {
            val (starRef, contentRef) = createRefs()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(color = Color(0xFFF8F8FC), shape = RoundedCornerShape(8.dp))
                    .padding(24.dp)
                    .constrainAs(contentRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(
                    text = "Đánh giá",
                    color = Color(0xFF1B1B1D),
                    fontSize = 24.sp,
                    fontWeight = FontWeight(700),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .padding(vertical = 18.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { numberOfStar = i },
                            modifier = Modifier
                                .size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = null,
                                tint = if (i <= numberOfStar) Color(0xFFFACC15) else Color(
                                    0xFFE1E1E3
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }
                }
                Row {
                    val submitInteractionSource = remember { MutableInteractionSource() }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                            .clickable {
                                val packageName = "com.plantidentification.chainz"
                                val playStoreUri = "market://details?id=$packageName".toUri()
                                val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
                                    setPackage("com.android.vending")
                                }
                                try {
                                    context.startActivity(intent)
                                    onDismiss()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    val webUri =
                                        "https://play.google.com/store/apps/details?id=$packageName".toUri()
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    onDismiss()
                                }
                            }
                    ) {
                        Text(
                            text = "Đánh giá",
                            color = if (isSubmitEnabled) Color(0xFF5A5A60) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                            .clickable(onClick = onDismiss)
                    ) {
                        Text(
                            text = "Có lẽ để sau",
                            color = Color(0xFF5A5A60),
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = null,
                tint = Color(0xFFFACC15),
                modifier = Modifier
                    .size(56.dp)
                    .constrainAs(starRef) {
                        bottom.linkTo(contentRef.top, margin = (-20).dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutBottomSheet(
    navController: NavController,
    onLogout: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
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
                    .background(color = Color(0xFFD7EDED), shape = CircleShape)
                    .border(width = 1.dp, color = Color(0xFFB2D8D8), shape = CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_account_logout),
                    contentDescription = "Account Logout",
                    modifier = Modifier
                        .size(70.dp)
                )
            }
            Text(
                text = "Đăng xuất tài khoản",
                color = Color(0xFF1B1B1D),
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            Text(
                text = "Bạn có chắc muốn đăng xuất không?",
                color = Color(0xFF5A5A60),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.75f)
            )
            SecondaryTextButtonTheme(
                onClick = {
                    onLogout()
                    onDismiss()
                },
                text = "Đăng xuất",
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController)
}
