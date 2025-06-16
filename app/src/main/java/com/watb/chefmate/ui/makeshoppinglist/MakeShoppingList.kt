package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.watb.chefmate.R
import com.watb.chefmate.ui.theme.Header

@Composable
fun MakeShoppingListScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .safeDrawingPadding()
    ) {
        Header(
            "Lập danh sách mua sắm",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color(0xFFFFFFFF)
                )
            }
        )
    }
}

@Preview
@Composable
fun MakeShoppingListScreenPreview() {
    MakeShoppingListScreen()
}