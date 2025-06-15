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

data class RegisterRequest(
    val fullName: String,
    val phone: String,
    val password: String
)

data class Response(
    val success: Boolean,
    val data: Any? = null,
    val message: String? = null
)

data class LoginData(
    val userId: Int,
    val fullName: String,
    val phone: String,
    val passwordHash: String,
    val followCount: Int,
    val createdAt: String
)