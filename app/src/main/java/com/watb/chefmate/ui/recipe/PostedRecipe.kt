package com.watb.chefmate.ui.recipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.RecipeViewModel

@Composable
fun PostedRecipeList(
    navController: NavController,
    onRecipeClick: (Recipe) -> Unit,
    recipeViewModel: RecipeViewModel
) {
    Column {
        Header(
            text = "Công thức đã đăng",
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
    }
}