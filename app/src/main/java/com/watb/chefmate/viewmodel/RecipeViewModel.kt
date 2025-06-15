package com.watb.chefmate.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.database.entities.*
import com.watb.chefmate.database.relations.RecipeWithIngredientsAndSteps
import com.watb.chefmate.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.flow.firstOrNull

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val allRecipes: Flow<List<Recipes>> = repository.getAllRecipes()

    fun addRecipe(
        recipeName: String,
        imageUri: Bitmap,
        userId: Int,
        isPublic: Boolean,
        likeQuantity: Int,
        cookTime: String,
        ration: Int,
        viewCount: Int,
        createAt: Date,
        ingredients: List<Pair<String, Pair<Int, String>>>, // (name, (weight, unit))
        steps: List<Pair<Int, String>> // (index, content)
    ) {
        viewModelScope.launch {
            val newRecipe = Recipes(
                recipeName = recipeName,
                image = imageUri,
                userId = userId,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookTime = cookTime,
                ration = ration,
                viewCount = viewCount,
                createAt = createAt
            )
            val recipeId = repository.insertRecipe(newRecipe)

            ingredients.forEach { (name, details) ->
                val (weight, unit) = details
                var ingredient = repository.getIngredientByName(name).firstOrNull()
                if (ingredient == null) {
                    ingredient = Ingredients(ingredientName = name)
                    val ingredientId = repository.insertIngredient(ingredient)
                    ingredient = ingredient.copy(ingredientId = ingredientId.toInt())
                }

                val recipeIngredient = RecipesIngredients(
                    recipeId = recipeId.toInt(),
                    ingredientId = ingredient.ingredientId,
                    weight = weight,
                    unit = unit
                )
                repository.insertRecipeIngredient(recipeIngredient)
            }

            steps.forEach { (index, content) ->
                val step = Steps(
                    recipeId = recipeId.toInt(),
                    index = index,
                    content = content
                )
                repository.insertStep(step)
            }
        }
    }

    fun updateRecipe(
        recipeId: Int,
        recipeName: String,
        imageUri: Bitmap,
        userId: Int,
        isPublic: Boolean,
        likeQuantity: Int,
        cookTime: String,
        ration: Int,
        viewCount: Int,
        createAt: Date,
        ingredients: List<Pair<String, Pair<Int, String>>>,
        steps: List<Pair<Int, String>>
    ) {
        viewModelScope.launch {
            val updatedRecipe = Recipes(
                recipeId = recipeId,
                recipeName = recipeName,
                image = imageUri,
                userId = userId,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookTime = cookTime,
                ration = ration,
                viewCount = viewCount,
                createAt = createAt
            )
            repository.updateRecipe(updatedRecipe)

            // Xóa tất cả nguyên liệu và bước cũ của công thức, sau đó thêm lại
            repository.deleteRecipeIngredientsForRecipe(recipeId)
            repository.deleteStepsForRecipe(recipeId)

            ingredients.forEach { (name, details) ->
                val (weight, unit) = details
                var ingredient = repository.getIngredientByName(name).firstOrNull()
                if (ingredient == null) {
                    ingredient = Ingredients(ingredientName = name)
                    val ingredientId = repository.insertIngredient(ingredient)
                    ingredient = ingredient.copy(ingredientId = ingredientId.toInt())
                }

                val recipeIngredient = RecipesIngredients(
                    recipeId = recipeId,
                    ingredientId = ingredient.ingredientId,
                    weight = weight,
                    unit = unit
                )
                repository.insertRecipeIngredient(recipeIngredient)
            }

            steps.forEach { (index, content) ->
                val step = Steps(
                    recipeId = recipeId,
                    index = index,
                    content = content
                )
                repository.insertStep(step)
            }
        }
    }

    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?> {
        return repository.getRecipeWithIngredientsAndSteps(recipeId)
    }

    // Factory để tạo ViewModel với một dependency (Repository)
    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}