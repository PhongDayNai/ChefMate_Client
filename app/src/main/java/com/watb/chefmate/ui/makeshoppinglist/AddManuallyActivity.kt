package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.watb.chefmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManuallyScreen() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val (titleRef, contentRef, btnRef) = createRefs()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top=50.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDBA74)),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        ) {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color(0xFFFFFFFF),
                    modifier = Modifier
                        .size(38.dp, 38.dp)
                )
            }
            Text(
                text = "Thêm nguyên liệu thủ công",
                color = Color(0xFFFFFFFF),
                fontSize = 18.sp
            )
        }
        Column(
            modifier = Modifier
                .padding(20.dp)
                .constrainAs(contentRef) {
                    top.linkTo(titleRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                    end.linkTo(parent.end, margin = 25.dp)
                }
        ) {
            var ingredientName = remember { mutableStateOf("") }
            var weight = remember { mutableStateOf("") }
            var unit = remember { mutableStateOf("") }
            Text(
                text = "Nhập tên nguyên liệu",
                fontSize = 18.sp,
                fontWeight = FontWeight(600)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = ingredientName.value,
                onValueChange = { ingredientName.value = it },
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
                var options = listOf("kg","gam","thìa cà phê")
                var expanded by remember { mutableStateOf(false) }
                TextField(
                    value = weight.value,
                    onValueChange = { weight.value = it },
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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color.Gray
                        )
                        .weight(2f)
                )
                Spacer(modifier = Modifier.width(20.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {expanded = !expanded},
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White)
                ) {
                    TextField(
                        value = unit.value,
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(2f)
                            .menuAnchor()
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(10.dp),
                                ambientColor = Color.Gray
                            ),
                        placeholder = {
                            Text(
                                "Đơn vị",
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon( expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    unit.value = selectionOption
                                    expanded = false
                                },
                                modifier = Modifier
                                    .background(Color.White),
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = {},
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
    AddManuallyScreen()
}