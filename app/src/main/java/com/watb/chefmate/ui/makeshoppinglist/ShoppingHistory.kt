package com.watb.chefmate.ui.makeshoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.ShoppingTimeViewModel

@Composable
fun ShoppingHistoryScreen(
    navController: NavController,
    shoppingViewModel: ShoppingTimeViewModel
) {
    val shoppingTimes by shoppingViewModel.shoppingTimes.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
            .padding(bottom = 42.dp)
    ) {
        Header(
            text = "Lịch sử mua sắm",
            leadingIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Notification",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            shoppingTimes.forEach { shoppingTime ->
                ShoppingHistoryItem(
                    shoppingTime = shoppingTime,
                    onClick = {},
                    modifier = Modifier
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingHistoryItem(
    shoppingTime: ShoppingTimeEntity,
    onClick: (ShoppingTimeEntity) -> Unit = {},
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
            .fillMaxWidth(0.8f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = "History",
                tint = Color(0xFF000000),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Ngày mua",
                    color = Color(0xFF555555),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    modifier = Modifier
                )
                Text(
                    text = shoppingTime.createdDate,
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_medium)),
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onClick(shoppingTime) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Xem chi tiết",
                    color = Color(0xFFF97316),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_medium)),
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview
@Composable
fun ShoppingHistoryScreenPreview() {
//    ShoppingHistoryScreen(navController = NavController(LocalContext.current))
}
