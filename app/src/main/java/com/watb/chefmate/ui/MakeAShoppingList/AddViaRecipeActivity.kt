package com.watb.chefmate.ui.MakeAShoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun AddViaRecipeScreen() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val (titleRef, searchRef, btnRef, listRef) = createRefs()
        Row {
            
        }
    }
}

@Preview
@Composable
fun AddViaRecipeSCreenPreview() {
    AddViaRecipeScreen()
}