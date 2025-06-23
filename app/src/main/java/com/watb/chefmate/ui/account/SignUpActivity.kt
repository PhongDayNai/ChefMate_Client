package com.watb.chefmate.ui.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.ui.theme.CircularLoading
import com.watb.chefmate.ui.theme.CustomTextField
import com.watb.chefmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    val fullName = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            var isConfirm by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDBA74))
                            ),
                            shape = RoundedCornerShape(bottomEnd = 100.dp)
                        )
                        .padding(40.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Đăng ký",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        color = Color(0xFFFFFFFF),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                    )
                    Text(
                        text = "Chào mừng đến với ChefMate",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 40.dp, end = 40.dp, top = 25.dp)
                        .fillMaxWidth()
                ) {
                    var isShowPassword by remember { mutableStateOf(false) }
                    var isShowConfirmPassword by remember { mutableStateOf(false) }

                    InputField(
                        label = "Họ và tên",
                        onValueChange = { fullName.value = it },
                        valueTextField = fullName.value,
                        placeholderText = "Vui lòng nhập họ và tên",
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    InputField(
                        label = "Số điện thoại",
                        onValueChange = { phoneNumber.value = it },
                        valueTextField = phoneNumber.value,
                        placeholderText = "Vui lòng nhập số điện thoại",
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    InputField(
                        label = "Email",
                        onValueChange = { email.value = it },
                        valueTextField = email.value,
                        placeholderText = "Vui lòng nhập email",
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    InputField(
                        label = "Mật khẩu",
                        onValueChange = { password.value = it },
                        valueTextField = password.value,
                        placeholderText = "Vui lòng nhập mật khẩu",
                        trailingIcon = {
                            IconButton(
                                onClick = {isShowConfirmPassword = !isShowConfirmPassword}
                            ) {
                                Icon(
                                    painter = if (isShowConfirmPassword) painterResource(R.drawable.ic_open_eye) else painterResource(R.drawable.ic_close_eye),
                                    tint = Color(0xFF777779),
                                    contentDescription = ""
                                )
                            }
                        },
                        visualTransformation = if (isShowConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    InputField(
                        label = "Xác nhận mật khẩu",
                        onValueChange = { confirmPassword.value = it },
                        valueTextField = confirmPassword.value,
                        placeholderText = "Vui lòng xác nhận mật khẩu",
                        trailingIcon = {
                            IconButton(
                                onClick = {isShowPassword = !isShowPassword}
                            ) {
                                Icon(
                                    if (isShowPassword) painterResource(R.drawable.ic_open_eye) else painterResource(R.drawable.ic_close_eye),
                                    tint = Color(0xFF777779),
                                    contentDescription = ""
                                )
                            }
                        },
                        visualTransformation = if(isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 24.dp)
                    )
                }
                val annotatedString = buildAnnotatedString {
                    append("Đồng ý với ")
                    withStyle(style = SpanStyle(color = Color(0xFFB4B4B4), fontWeight = FontWeight.Bold)) {
                        append("Điều khoản dịch vụ")
                    }
                    append(" và ")
                    withStyle(style = SpanStyle(color = Color(0xFFB4B4B4), fontWeight = FontWeight.Bold)) {
                        append("Chính sách bảo mật")
                    }
                    append(" của ChefMate.")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                ) {
                    Checkbox(
                        checked = isConfirm,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFF97316),
                            uncheckedColor = Color(0xFFB4B4B4)
                        ),
                        onCheckedChange = { isConfirm = it }
                    )
                    Text(
                        text =  annotatedString,
                        fontSize = 14.sp,

                        )
                }
                Button(
                    onClick = {
                        if (fullName.value != "" &&
                            phoneNumber.value != "" &&
                            email.value != "" &&
                            password.value != "" &&
                            confirmPassword.value != "") {
                            if (password.value == confirmPassword.value) {
                                if (isConfirm) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        val response = ApiClient.register(
                                            fullName = fullName.value.trim(),
                                            phone = phoneNumber.value.trim(),
                                            email = email.value.trim(),
                                            password = password.value.trim()
                                        )
                                        if (response != null) {
                                            if (response.data != null) {
                                                userViewModel.saveLoginState(context, response.data)
                                                navController.navigate("mainAct") {
                                                    popUpTo("signUp") {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Vui lòng đồng ý với điều khoản dịch vụ", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316)
                    ),
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .width(242.dp)
                ) {
                    Text(
                        text = "Đăng ký"
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "Bạn đã có tài khoản?",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        color = Color(0xFF777779),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Đăng nhập ngay",
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        color = Color(0xFFF97316),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate("signIn") {
                                        popUpTo("signUp") {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                    )
                }
            }
        }
        if (isLoading) {
            CircularLoading()
        }
    }
}

@Composable
fun InputField(
    label: String,
    valueTextField: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 16.dp)
        )
        CustomTextField(
            value = valueTextField,
            onValueChange = onValueChange,
            placeholder = placeholderText,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    val userViewModel: UserViewModel = viewModel()
    SignUpScreen(rememberNavController(), userViewModel)
}