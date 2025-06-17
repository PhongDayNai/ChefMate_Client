package com.watb.chefmate.ui.makeshoppinglist

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout

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
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val (titleRef, contentRef, btnRef) = createRefs()

        Column(
            modifier = Modifier
                .padding(20.dp)
                .constrainAs(contentRef) {
                    top.linkTo(titleRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                    end.linkTo(parent.end, margin = 25.dp)
                }
        ) {
            Text(
                text = "Nhập tên nguyên liệu",
                fontSize = 18.sp,
                fontWeight = FontWeight(600)
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
                    ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
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
                fontWeight = FontWeight(600)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row {
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
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = KeyboardActions.Default.onDone
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Gray
                        )
                        .weight(1f)
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
                        onDone = KeyboardActions.Default.onDone
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Gray
                        )
                        .weight(1f)
                )
            }
        }
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF97518)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .width(100.dp)
                .constrainAs(btnRef) {
                    top.linkTo(contentRef.bottom)
                    end.linkTo(parent.end, margin = 20.dp)
                }
        ) {
            Text(
                text = "Thêm",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}


@Preview
@Composable
fun AddManuallyScreenPreview() {
//    AddManuallyScreen()
}