package com.watb.chefmate.ui.account

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.ui.theme.CircularLoading
import com.watb.chefmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val (headRef, contentRef, btnRef) = createRefs()
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .constrainAs(headRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
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
                    text = "Đăng nhập",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    color = Color(0xFFFFFFFF),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(bottom = 15.dp)
                )
                Text(
                    text = "Chào mừng trở lại ChefMate",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(headRef.bottom, margin = 50.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            var isShowPassword by remember { mutableStateOf(false) }

            InputField(
                label = "Số điện thoại hoặc email",
                onValueChange = { identifier = it },
                valueTextField = identifier,
                placeholderText = "Nhập số điện thoại hoặc email"
            )

            InputField(
                label = "Mật khẩu",
                onValueChange = { password = it },
                valueTextField = password,
                placeholderText = "Nhập mật khẩu",
                trailingIcon = {
                    IconButton(
                        onClick = {isShowPassword = !isShowPassword}
                    ) {
                        Icon(
                            if (isShowPassword) painterResource(R.drawable.ic_open_eye) else painterResource(
                                R.drawable.ic_close_eye),
                            tint = Color(0xFF777779),
                            contentDescription = ""
                        )
                    }
                },
                visualTransformation = if(isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .padding(top = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .constrainAs(btnRef) {
                    top.linkTo(contentRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Button(
                onClick = {
                    if (identifier != "" && password != "") {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val response = ApiClient.login(identifier = identifier.trim(), password = password.trim())
                                Log.d("Login", "Response: $response")
                                if (response != null) {
                                    if (response.success) {
                                        if (response.data != null) {
                                            userViewModel.saveLoginState(context, response.data)
                                            navController.navigate("mainAct") {
                                                popUpTo("signIn") {
                                                    inclusive = true
                                                }
                                            }
                                            isLoading = false
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Đăng nhập thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        isLoading = false
                                        if (response.message == "Unauthorized access") {
                                            Toast.makeText(context, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Đăng nhập thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                if (e.message == "Unauthorized access") {
                                    Toast.makeText(context, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Đăng nhập thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97316)
                ),
                modifier = Modifier
                    .width(242.dp)
            ) {
                Text(
                    text = "Đăng nhập",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Bạn chưa có tài khoản?",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    color = Color(0xFF777779),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Đăng ký ngay",
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    color = Color(0xFFF97316),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .clickable(
                            onClick = {
                                navController.navigate("signUp") {
                                    popUpTo("signIn") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                )
            }
        }
        if (isLoading) {
            CircularLoading()
        }
    }
}

@Preview
@Composable
fun SignInScreenPreview() {
    val userViewModel: UserViewModel = viewModel()
    SignInScreen(rememberNavController(), userViewModel)
}