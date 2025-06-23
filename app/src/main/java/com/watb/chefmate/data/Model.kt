package com.watb.chefmate.data

import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.TagEntity

data class Recipe(
    val recipeId: Int? = null,
    val recipeName: String,
    val image: String,
    val cookingTime: String,
    val ration: Int,
    val viewCount: Int,
    val likeQuantity: Int,
    val userId: Int? = null,
    val createdAt: String,
    val userName: String,
    val cookingSteps: List<CookingStep>,
    val ingredients: List<IngredientItem>,
    val comments: List<CommentItem>,
    val tags: List<TagEntity>,
    val isLiked: Boolean = false,
)

data class CreateRecipeData(
    val recipeName: String,
    val cookingTime: String,
    val ration: Int,
    val ingredients: List<IngredientItem>,
    val cookingSteps: List<CookingStepAddRecipeData>,
    val userId: Int,
    val image: String,
    val tags: List<TagData>
)

data class TagData(
    val tagName: String
)

data class IngredientItem(
    val ingredientId: Int? = null,
    val ingredientName: String,
    val weight: Int,
    val unit: String,
)

data class CookingStep(
    val indexStep: Int? = null,
    val stepContent: String,
)

data class CookingStepAddRecipeData(
    val content: String,
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
    val email: String,
    val password: String
)

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class UpdateUserInformationRequest(
    val userId: Int,
    val fullName: String,
    val phone: String,
    val email: String
)

data class ChangePasswordRequest(
    val phone: String,
    val currentPassword: String,
    val newPassword: String
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
    val email: String,
    val passwordHash: String? = null,
    val followCount: Int,
    val recipeCount: Int,
    val createdAt: String
)

data class CreateRecipeResponse(
    val success: Boolean,
    val data: Int? = null,
    val message: String? = null
)

data class SearchRecipeRequest(
    val recipeName: String,
    val userId: Int? = null
)

data class SearchRecipeByTagRequest(
    val tagName: String,
    val userId: Int? = null
)

data class UserIDRequest(
    val userId: Int? = null
)

data class RecipeListResponse(
    val success: Boolean,
    val data: List<Recipe>? = null,
    val message: String? = null
)

data class AllIngredientsResponse(
    val success: Boolean,
    val data: List<IngredientEntity>? = null,
    val message: String? = null
)

data class AllTagsResponse(
    val success: Boolean,
    val data: List<TagEntity>? = null,
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
    val data: InteractionData? = null,
    val message: String? = null
)

data class InteractionData(
    val count: Int,
    val comments: List<CommentItem>? = null
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

enum class SearchType(val value: String) {
    NAME("name"),
    TAG("tag")
}

enum class StatusShopping (val value: String){
    WAITING("waiting"),
    BOUGHT("bought"),
    COULD_NOT_BUY("couldNotBuy")
}
