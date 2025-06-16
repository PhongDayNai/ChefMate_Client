package com.watb.chefmate.data

data class Recipe(
    val recipeId: Int,
    val image: String,
    val recipeName: String,
    val userName: String,
    val likeQuantity: Int,
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
    val ingredientId: Int? = null,
    val ingredientName: String,
    val weight: Int,
    val unit: String,
)

data class CookingStep(
    val indexStep: Int,
    val stepContent: String,
)

data class CommentItem(
    val commentId: Int,
    val userId: Int,
    val userName: String,
    val content: String,
    val createdAt: String,
)

data class RegisterRequest(
    val fullName: String,
    val phone: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val data: LoginData? = null,
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

data class TopTrendingResponse(
    val success: Boolean,
    val data: List<Recipe>? = null,
    val message: String? = null
)

data class LikeRequest(
    val userId: Int,
    val recipeId: Int
)

data class CommentRequest(
    val userId: Int,
    val recipeId: Int,
    val content: String
)

data class IncreaseRequest(
    val recipeId: Int
)

data class InteractionResponse(
    val success: Boolean,
    val data: Boolean? = null,
    val message: String? = null
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