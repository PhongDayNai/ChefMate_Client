package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.watb.chefmate.R
import kotlinx.coroutines.launch

@Composable
fun AddManuallyScreen(
    name: String,
    onNameChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    onDone: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .background(Color(0xFFFFFFFF))
            .imePadding()
    ) {
        val (titleRef, contentRef) = createRefs()

        val scrollState = rememberScrollState()

        val coroutineScope = rememberCoroutineScope()

        val keyboardController = LocalSoftwareKeyboardController.current

        val focusManager = LocalFocusManager.current
        val weightRequester = remember { FocusRequester() }
        val unitRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState)
                .constrainAs(contentRef) {
                    top.linkTo(titleRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                    end.linkTo(parent.end, margin = 25.dp)
                }
        ) {
            Text(
                text = "Nhập tên nguyên liệu",
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = Color.Gray
                    )
                    .clickable {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        weightRequester.requestFocus()
                    }
                ),
                placeholder = {
                    Text(
                        text = "Tên nguyên liệu",
                        fontSize = 14.sp,
                        color = Color(0xFFADAEBC)
                    )
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Nhập định lượng",
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp)
            ) {
                TextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    placeholder = {
                        Text(
                        text = "Khối lượng",
                        fontSize = 14.sp,
                        color = Color(0xFFADAEBC)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            unitRequester.requestFocus()
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Gray
                        )
                        .weight(1f)
                        .focusRequester(weightRequester)
                )
                Spacer(modifier = Modifier.width(20.dp))
                TextField(
                    value = unit,
                    onValueChange = onUnitChange,
                    placeholder = {
                        Text(
                            text = "Đơn vị",
                            fontSize = 14.sp,
                            color = Color(0xFFADAEBC)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Gray
                        )
                        .weight(1f)
                        .focusRequester(unitRequester)
                )
            }
            Row{
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97518)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(100.dp)
                ) {
                    Text(
                        text = "Thêm",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun AddManuallyScreenPreview() {
//    AddManuallyScreen()
}