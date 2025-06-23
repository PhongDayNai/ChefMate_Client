package com.watb.chefmate.ui.account

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.ui.theme.CircularLoading
import com.watb.chefmate.ui.theme.PrimaryTextButtonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    navController: NavController,
) {
    val context = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var screen by remember { mutableStateOf(0) }
    var displayNameCurrent by remember { mutableStateOf("") }
    var phoneNumberCurrent by remember { mutableStateOf("") }
    var emailCurrent by remember { mutableStateOf("") }

    var displayNameNew by remember { mutableStateOf("") }
    var phoneNumberNew by remember { mutableStateOf("") }
    var emailNew by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        displayNameCurrent = DataStoreHelper.getUsername(context)
        displayNameNew = displayNameCurrent
        phoneNumberCurrent = DataStoreHelper.getPhoneNumber(context)
        phoneNumberNew = phoneNumberCurrent
        emailCurrent = DataStoreHelper.getEmail(context)
        emailNew = emailCurrent
    }

    val hasChanges by remember {
        derivedStateOf {
            displayNameCurrent != displayNameNew || phoneNumberCurrent != phoneNumberNew || emailCurrent != emailNew
        }
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFF8F8FC))
                .safeDrawingPadding()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 32.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .border(width = 1.dp, color = Color(0xFFE1E1E3), shape = CircleShape)
                        .align(Alignment.CenterStart)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = null,
                            tint = Color(0xFF5A5A60),
                            modifier = Modifier
                                .size(12.dp)
                        )
                    }
                }
                Text(
                    text = "Chỉnh sửa thông tin",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1B1B1D),
                    fontSize = 20.sp,
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                shape = CircleShape,
                border = BorderStroke(width = 4.dp, color = Color(0xFFFFFFFF)),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
            ) {
                Image(
                    painter = painterResource(R.drawable.img_common_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                )
            }
            Card(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 8.dp
                ),
                modifier = Modifier
                    .padding(top = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0xFFFFFFFF))
                        .verticalScroll(state = rememberScrollState())
                ) {
                    AnimatedContent(
                        targetState = screen,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { target ->
                        when (target) {
                            0 -> {
                                EditPersonalInformation(
                                    context = context,
                                    hasChanges = hasChanges,
                                    displayNameNew = displayNameNew,
                                    onChangeDisplayName = { newValue ->
                                        displayNameNew = newValue
                                    },
                                    emailNew = emailNew,
                                    onChangeEmail = { newValue ->
                                        emailNew = newValue
                                    },
                                    phoneNumberNew = phoneNumberNew,
                                    onChangePhoneNumber = { newValue ->
                                        phoneNumberNew = newValue
                                    },
                                    onChangePassword = { screen = 1 },
                                    onChangeLoading = { isLoading = it }
                                )
                            }
                            1 -> {
                                EditPassword(
                                    context = context,
                                    onChangeLoading = { isLoading = it },
                                    onChangePersonalInformation = { screen = 0 }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (isLoading) {
            CircularLoading()
        }
    }
}

@Composable
fun EditPersonalInformation(
    context: Context,
    hasChanges: Boolean,
    displayNameNew: String,
    onChangeDisplayName: (String) -> Unit,
    emailNew: String,
    onChangeEmail: (String) -> Unit,
    phoneNumberNew: String,
    onChangePhoneNumber: (String) -> Unit,
    onChangePassword: () -> Unit,
    onChangeLoading: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Column {
        Text(
            text = "Họ và tên",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = displayNameNew,
            onValueChange = { newValue ->
                onChangeDisplayName(newValue)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {

                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f),
        )
        Text(
            text = "Địa chỉ email",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = emailNew,
            onValueChange = { newValue ->
                onChangeEmail(newValue)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {

                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        Text(
            text = "Số điện thoại",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = phoneNumberNew,
            onValueChange = { newValue ->
                onChangePhoneNumber(newValue)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {

                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        Text(
            text = "Đổi mật khẩu",
            textDecoration = TextDecoration.Underline,
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(500),
            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
            modifier = Modifier
                .padding(top = 12.dp, end = 20.dp)
                .align(Alignment.End)
                .clickable(
                    onClick = onChangePassword
                )
        )
        PrimaryTextButtonTheme(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) { onChangeLoading(true) }
                    try {
                        if (hasChanges) {

                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Không có thay đổi nào, vui lòng kiểm tra lại",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("EditProfile", "Something went wrong when change profile: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Có lỗi xảy ra, vui lòng thử lại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } finally {
                        withContext(Dispatchers.Main) { onChangeLoading(false) }
                    }
                }
            },
            text = "Lưu thay đổi",
            enabled = hasChanges,
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun EditPassword(
    context: Context,
    onChangeLoading: (Boolean) -> Unit,
    onChangePersonalInformation: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    Column {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }
        var isShowPassword by remember { mutableStateOf(false) }
        var isShowNewPassword by remember { mutableStateOf(false) }
        var isShowConfirmNewPassword by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.9f)
        ) {
            IconButton(
                onClick = onChangePersonalInformation,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    tint = Color(0xFF5A5A60),
                    modifier = Modifier
                        .size(16.dp)
                )
            }
        }
        Text(
            text = "Mật khẩu hiện tại",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { newValue ->
                currentPassword = newValue
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { isShowPassword = !isShowPassword }
                ) {
                    Icon(
                        painter = painterResource(id = if (isShowPassword) R.drawable.ic_open_eye else R.drawable.ic_close_eye),
                        contentDescription = null,
                        tint = Color(0xFF5A5A60),
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            },
            visualTransformation = if (isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {

                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        Text(
            text = "Mật khẩu mới",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newValue ->
                newPassword = newValue
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { isShowNewPassword = !isShowNewPassword }
                ) {
                    Icon(
                        painter = painterResource(id = if (isShowNewPassword) R.drawable.ic_open_eye else R.drawable.ic_close_eye),
                        contentDescription = null,
                        tint = Color(0xFF5A5A60),
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            },
            visualTransformation = if (isShowNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        Text(
            text = "Xác nhận mật khẩu mới",
            color = Color(0xFF5A5A60),
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .fillMaxWidth(0.9f)
        )
        OutlinedTextField(
            value = confirmNewPassword,
            onValueChange = { newValue ->
                confirmNewPassword = newValue
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1B1B1D),
                unfocusedTextColor = Color(0xFF1B1B1D),
                focusedContainerColor = Color(0xFFF8F8FC),
                unfocusedContainerColor = Color(0xFFF8F8FC),
                focusedIndicatorColor = Color(0xFFDDE7E7),
                unfocusedIndicatorColor = Color(0xFFDDE7E7),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { isShowConfirmNewPassword = !isShowConfirmNewPassword }
                ) {
                    Icon(
                        painter = painterResource(id = if (isShowConfirmNewPassword) R.drawable.ic_open_eye else R.drawable.ic_close_eye),
                        contentDescription = null,
                        tint = Color(0xFF5A5A60),
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            },
            visualTransformation = if (isShowConfirmNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        )
        PrimaryTextButtonTheme(
            onClick = {
                if (newPassword != confirmNewPassword) {
                    Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch {
                        onChangeLoading(true)
                        onChangeLoading(false)
                    }
                }
            },
            text = "Đổi mật khẩu",
            enabled = currentPassword != "" && newPassword != "" && confirmNewPassword != "",
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(0.9f)
        )
    }
}

@Preview
@Composable
fun EditProfileScreenPreview() {
    val navController = rememberNavController()

    EditProfileScreen(navController = navController)
}