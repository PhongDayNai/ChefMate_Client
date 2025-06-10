package com.watb.chefmate.data

data class Recipe(
    val image: String,
    val name: String,
    val author: String,
    val likesQuantity: Int,
    val userViews: Int,
    val ingredients: List<String>,
    val cookingSteps: List<String>,
    val cookingTime: String,
    val comments: List<CommentItem>
)

data class CommentItem(
    val author: String,
    val time: String,
    val content: String,
)