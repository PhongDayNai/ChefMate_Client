package com.watb.chefmate.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.watb.chefmate.R

@Composable
fun SignUpScreen() {
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
            val phoneNumber = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            var isShowConfirmPassword by remember { mutableStateOf(false) }

            InputField(
                label = "Số điện thoại",
                onValueChange = { phoneNumber.value = it },
                valueTextField = phoneNumber.value,
                placehodlerText = "Vui lòng nhập số điện thoại",
            )

            InputField(
                label = "Mật khẩu",
                onValueChange = { password.value = it },
                valueTextField = password.value,
                placehodlerText = "Vui lòng nhập mật khẩu",
                trailingIcon = {
                    IconButton(
                        onClick = {isShowConfirmPassword = !isShowConfirmPassword}
                    ) {
                        Icon(
                            if (isShowConfirmPassword) painterResource(R.drawable.ic_open_eye) else painterResource(
                                R.drawable.ic_close_eye),
                            tint = Color(0xFF777779),
                            contentDescription = ""
                        )
                    }
                },
                visualTransformation = if(isShowConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
        }

        Button(
            onClick = {

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF97316)
            ),
            modifier = Modifier
                .padding(top = 15.dp)
                .width(242.dp)
                .constrainAs(btnRef) {
                    top.linkTo(contentRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = "Đăng nhập"
            )
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}