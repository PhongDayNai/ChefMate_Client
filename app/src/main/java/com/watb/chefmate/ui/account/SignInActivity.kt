package com.watb.chefmate.ui.account

import androidx.compose.foundation.background
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.constraintlayout.compose.ConstraintLayout
import com.watb.chefmate.R
import com.watb.chefmate.ui.theme.CustomTextField

@Composable
fun SignInActivity() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val (heaadRef, contentRef, agreeRef, btnRef) = createRefs()
        var isConfirm by remember { mutableStateOf(false) }
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .constrainAs(heaadRef) {
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
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp, top = 25.dp)
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(heaadRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            val fullName = remember { mutableStateOf("") }
            val phoneNumber = remember { mutableStateOf("") }
            val email = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }
            val confirmPassword = remember { mutableStateOf("") }

            var isShowPassword by remember { mutableStateOf(false) }
            var isShowConfirmPassword by remember { mutableStateOf(false) }

            InputField(
                label = "Họ và tên",
                onValueChange = { fullName.value = it },
                valueTextField = fullName.value,
                placehodlerText = "Vui lòng nhập họ và tên",
            )
            InputField(
                label = "Số điện thoại",
                onValueChange = { phoneNumber.value = it },
                valueTextField = phoneNumber.value,
                placehodlerText = "Vui lòng nhập số điện thoại",
            )
            InputField(
                label = "Email",
                onValueChange = { email.value = it },
                valueTextField = email.value,
                placehodlerText = "Vui lòng nhập email",
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
                            if (isShowConfirmPassword) painterResource(R.drawable.ic_open_eye) else painterResource(R.drawable.ic_close_eye),
                            tint = Color(0xFF777779),
                            contentDescription = ""
                        )
                    }
                },
                visualTransformation = if(isShowConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
            InputField(
                label = "Xác nhận mật khẩu",
                onValueChange = { confirmPassword.value = it },
                valueTextField = confirmPassword.value,
                placehodlerText = "Vui lòng xác nhận mật khẩu",
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
                visualTransformation = if(isShowPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
        }
        val annotatedString = buildAnnotatedString {
            append("Đồng ý với ")
            withStyle(style = SpanStyle(color = Color(0xFFB4B4B4), fontWeight = FontWeight.Bold)) {
                append("Điều khoản dịch vụ\n")
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
                .constrainAs(agreeRef) {
                    top.linkTo(contentRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
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

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF97316)
            ),
            modifier = Modifier
                .padding(top = 15.dp)
                .width(242.dp)
                .constrainAs(btnRef) {
                    top.linkTo(agreeRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = "Đăng ký"
            )
        }
    }
}

@Composable
fun InputField(label: String, valueTextField: String, placehodlerText: String, onValueChange: (String) -> Unit, trailingIcon: @Composable (() -> Unit)? = null, visualTransformation: VisualTransformation = VisualTransformation.None) {
    Column {
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
            placeholder = placehodlerText,
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
fun SignInScreenPreview() {
    SignInActivity()
}