package com.watb.chefmate.viewmodel

//import android.graphics.Bitmap
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.watb.chefmate.data.Recipe
//import com.watb.chefmate.database.entities.*
//import com.watb.chefmate.database.relations.RecipeWithIngredientsAndSteps
//import com.watb.chefmate.repository.RecipeRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.flow.firstOrNull
//import kotlinx.coroutines.flow.map
//
//class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
//
//    val allRecipes: Flow<List<Recipe>> =
//        repository.getAllRecipes().map { list ->
//            list.map { it.toRecipe() }
//        }
//
//
//    fun addRecipe(
//        recipeName: String,
//        imageUri: String,
//        userName: String,
//        isPublic: Boolean,
//        likeQuantity: Int,
//        cookTime: String,
//        ration: Int,
//        viewCount: Int,
//        createdAt: String,
//        ingredients: List<Pair<String, Pair<Int, String>>>, // (name, (weight, unit))
//        steps: List<Pair<Int, String>> // (index, content)
//    ) {
//        viewModelScope.launch {
//            val newRecipe = RecipeEntity(
//                recipeName = recipeName,
//                image = imageUri,
//                userName = userName,
//                isPublic = isPublic,
//                likeQuantity = likeQuantity,
//                cookTime = cookTime,
//                ration = ration,
//                viewCount = viewCount,
//                createdAt = createdAt
//            )
//            val recipeId = repository.insertRecipe(newRecipe)
//
//            ingredients.forEach { (name, details) ->
//                val (weight, unit) = details
//                var ingredient = repository.getIngredientByName(name).firstOrNull()
//                if (ingredient == null) {
//                    ingredient = IngredientEntity(ingredientName = name)
//                    val ingredientId = repository.insertIngredient(ingredient)
//                    ingredient = ingredient.copy(ingredientId = ingredientId.toInt())
//                }
//
//                val recipeIngredient = RecipeIngredientEntity(
//                    recipeId = recipeId.toInt(),
//                    ingredientId = ingredient.ingredientId,
//                    weight = weight,
//                    unit = unit
//                )
//                repository.insertRecipeIngredient(recipeIngredient)
//            }
//
//            steps.forEach { (index, content) ->
//                val step = StepEntity(
//                    recipeId = recipeId.toInt(),
//                    index = index,
//                    content = content
//                )
//                repository.insertStep(step)
//            }
//        }
//    }
//
//    fun updateRecipe(
//        recipeId: Int,
//        recipeName: String,
//        imageUri: String,
//        userName: String,
//        isPublic: Boolean,
//        likeQuantity: Int,
//        cookTime: String,
//        ration: Int,
//        viewCount: Int,
//        createdAt: String,
//        ingredients: List<Pair<String, Pair<Int, String>>>,
//        steps: List<Pair<Int, String>>
//    ) {
//        viewModelScope.launch {
//            val updatedRecipe = RecipeEntity(
//                recipeId = recipeId,
//                recipeName = recipeName,
//                image = imageUri,
//                userName = userName,
//                isPublic = isPublic,
//                likeQuantity = likeQuantity,
//                cookTime = cookTime,
//                ration = ration,
//                viewCount = viewCount,
//                createdAt = createdAt
//            )
//            repository.updateRecipe(updatedRecipe)
//
//            // Xóa tất cả nguyên liệu và bước cũ của công thức, sau đó thêm lại
//            repository.deleteRecipeIngredientsForRecipe(recipeId)
//            repository.deleteStepsForRecipe(recipeId)
//
//            ingredients.forEach { (name, details) ->
//                val (weight, unit) = details
//                var ingredient = repository.getIngredientByName(name).firstOrNull()
//                if (ingredient == null) {
//                    ingredient = IngredientEntity(ingredientName = name)
//                    val ingredientId = repository.insertIngredient(ingredient)
//                    ingredient = ingredient.copy(ingredientId = ingredientId.toInt())
//                }
//
//                val recipeIngredient = RecipeIngredientEntity(
//                    recipeId = recipeId,
//                    ingredientId = ingredient.ingredientId,
//                    weight = weight,
//                    unit = unit
//                )
//                repository.insertRecipeIngredient(recipeIngredient)
//            }
//
//            steps.forEach { (index, content) ->
//                val step = StepEntity(
//                    recipeId = recipeId,
//                    index = index,
//                    content = content
//                )
//                repository.insertStep(step)
//            }
//        }
//    }
//
//    fun getRecipeWithIngredientsAndSteps(recipeId: Int): Flow<RecipeWithIngredientsAndSteps?> {
//        return repository.getRecipeWithIngredientsAndSteps(recipeId)
//    }
//
//    // Factory để tạo ViewModel với một dependency (Repository)
//    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
//                @Suppress("UNCHECKED_CAST")
//                return RecipeViewModel(repository) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
//}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.database.entities.RecipeEntity
import com.watb.chefmate.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.google.gson.Gson // For JSON serialization
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.entities.toRecipe
import kotlinx.coroutines.flow.map

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

//    val allRecipes: Flow<List<RecipeEntity>> = repository.getAllRecipes()

    val allRecipes: Flow<List<Recipe>> =
        repository.getAllRecipes().map { list ->
            list.map { it.toRecipe() }
        }


    fun addRecipe(
        recipeName: String,
        imageUri: String,
        userName: String, // Changed to userName
        isPublic: Boolean,
        likeQuantity: Int,
        cookTime: String,
        ration: Int,
        viewCount: Int,
        createdAt: String, // Changed to String
        ingredients: List<Pair<String, Pair<Int, String>>>, // (name, (weight, unit))
        steps: List<Pair<Int, String>> // (index, content)
    ) {
        viewModelScope.launch {
            // Convert ingredients and steps to JSON strings
            val gson = Gson()
            val ingredientsJson = gson.toJson(ingredients)
            val stepsJson = gson.toJson(steps)

            val newRecipe = RecipeEntity(
                recipeName = recipeName,
                image = imageUri,
                userName = userName,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookTime = cookTime,
                ration = ration,
                viewCount = viewCount,
                ingredients = ingredientsJson,
                cookingSteps = stepsJson,
                createdAt = createdAt
            )
            repository.insertRecipe(newRecipe)
        }
    }

    fun updateRecipe(
        recipeId: Int,
        recipeName: String,
        imageUri: String,
        userName: String,
        isPublic: Boolean,
        likeQuantity: Int,
        cookTime: String,
        ration: Int,
        viewCount: Int,
        createdAt: String,
        ingredients: List<Pair<String, Pair<Int, String>>>,
        steps: List<Pair<Int, String>>
    ) {
        viewModelScope.launch {
            val gson = Gson()
            val ingredientsJson = gson.toJson(ingredients)
            val stepsJson = gson.toJson(steps)

            val updatedRecipe = RecipeEntity(
                recipeId = recipeId,
                recipeName = recipeName,
                image = imageUri,
                userName = userName,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookTime = cookTime,
                ration = ration,
                viewCount = viewCount,
                ingredients = ingredientsJson,
                cookingSteps = stepsJson,
                createdAt = createdAt
            )
            repository.updateRecipe(updatedRecipe)
        }
    }

    fun getRecipeById(recipeId: Int): Flow<RecipeEntity?> {
        return repository.getRecipeById(recipeId)
    }

    // You will need a way to parse the JSON strings back to lists in your UI or a helper.
    fun parseIngredientsJson(jsonString: String): List<Pair<String, Pair<Int, String>>> {
        val gson = Gson()
        val type = object : TypeToken<List<Pair<String, Pair<Int, String>>>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    fun parseStepsJson(jsonString: String): List<Pair<Int, String>> {
        val gson = Gson()
        val type = object : TypeToken<List<Pair<Int, String>>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    // Factory to create ViewModel with a dependency (Repository)
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