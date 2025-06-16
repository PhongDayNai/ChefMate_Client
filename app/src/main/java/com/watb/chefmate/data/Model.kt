package com.watb.chefmate.data

data class Recipe(
    val recipeId: Int,
    val image: String,
    val name: String,
    val author: String,
    val likesQuantity: Int,
    val viewCount: Int,
    val ingredients: List<IngredientItem>,
    val cookingSteps: List<CookingStep>,
    val cookingTime: String,
    val ration: Int,
    val isLiked: Boolean = false,
    val comments: List<CommentItem>,
    val createdAt: String
)

data class IngredientItem(
    val ingredientId: Int,
    val ingredientName: String,
    val weight: Int,
    val unit: String,
)

data class CookingStep(
    val indexStep: Int,
    val stepContent: String,
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

data class IngredientInput(
    var name: String,
    var weight: String,
    var unit: String
)

data class StepInput(
    val index: Int,
    var content: String
)