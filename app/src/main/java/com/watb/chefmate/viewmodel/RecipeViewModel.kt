package com.watb.chefmate.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.database.entities.RecipeEntity
import com.watb.chefmate.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.toRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _topTrending = MutableStateFlow<List<Recipe>>(emptyList())
    val topTrending: StateFlow<List<Recipe>> = _topTrending

    private val _searchResult = MutableStateFlow<List<Recipe>>(emptyList())
    val searchResult: StateFlow<List<Recipe>> = _searchResult

    val allRecipes: Flow<List<Recipe>> =
        repository.getAllRecipes().map { list ->
            list.map { it.toRecipe() }
        }

    suspend fun getTopTrending() {
        val response = ApiClient.getTopTrending()
        response?.let {
            response.data?.let {
                _topTrending.value = it
            }
        }
    }

    fun searchRecipe(recipeName: String, userId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResult.value = emptyList()
            val response = ApiClient.searchRecipe(recipeName, userId)
            response?.data?.let {
                _searchResult.value = it
            }
            _isLoading.value = false
        }
    }

    fun getAllIngredientsFromServer() {
        viewModelScope.launch {
            val response = ApiClient.getAllIngredients()
            response?.data?.let {
                val localIngredients: Flow<List<IngredientEntity>?> = getAllIngredients()
                if (localIngredients.first() != null) {
                    if (response.data.size != localIngredients.first()!!.size) {
                        deleteAllIngredients()
                        it.forEach { ingredient ->
                            insertIngredient(ingredient)
                        }
                    }
                } else {
                    it.forEach { ingredient ->
                        insertIngredient(ingredient)
                    }
                }
            }
        }
    }

    @SuppressLint("MemberExtensionConflict")
    fun addRecipe(
        recipeName: String,
        imageUri: String,
        userName: String,
        isPublic: Boolean,
        likeQuantity: Int,
        cookingTime: String,
        ration: Int,
        viewCount: Int,
        createdAt: String, // Changed to String
        ingredients: List<Pair<String, Pair<Int, String>>>, // (name, (weight, unit))
        steps: List<Pair<Int, String>> // (index, content)
    ) {
        viewModelScope.launch {
            val ingredientNames = ingredients.joinToString(";;;") { it.first }
            val ingredientWeights = ingredients.joinToString(";;;") { it.second.first.toString() }
            val ingredientUnits = ingredients.joinToString(";;;") { it.second.second }
            val cookingSteps = steps.sortedBy { it.first }.joinToString(";;;") { it.second } // Đảm bảo đúng thứ tự

            val newRecipe = RecipeEntity(
                recipeName = recipeName,
                image = imageUri,
                userName = userName,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookingTime = cookingTime,
                ration = ration,
                viewCount = viewCount,
                ingredientNames = ingredientNames,
                ingredientWeights = ingredientWeights,
                ingredientUnits = ingredientUnits,
                cookingSteps = cookingSteps,
                createdAt = createdAt,
            )
            repository.insertRecipe(newRecipe)
        }
    }

    @SuppressLint("MemberExtensionConflict")
    fun updateRecipe(
        recipeId: Int,
        recipeName: String,
        imageUri: String,
        userName: String,
        isPublic: Boolean,
        likeQuantity: Int,
        cookingTime: String,
        ration: Int,
        viewCount: Int,
        createdAt: String,
        ingredients: List<Pair<String, Pair<Int, String>>>,
        steps: List<Pair<Int, String>>
    ) {
        viewModelScope.launch {
            val ingredientNames = ingredients.joinToString(";;;") { it.first }
            val ingredientWeights = ingredients.joinToString(";;;") { it.second.first.toString() }
            val ingredientUnits = ingredients.joinToString(";;;") { it.second.second }
            val cookingSteps = steps.sortedBy { it.first }.joinToString(";;;") { it.second }

            val updatedRecipe = RecipeEntity(
                recipeId = recipeId,
                recipeName = recipeName,
                image = imageUri,
                userName = userName,
                isPublic = isPublic,
                likeQuantity = likeQuantity,
                cookingTime = cookingTime,
                ration = ration,
                viewCount = viewCount,
                ingredientNames = ingredientNames,
                ingredientWeights = ingredientWeights,
                ingredientUnits = ingredientUnits,
                cookingSteps = cookingSteps,
                createdAt = createdAt,
            )
            repository.updateRecipe(updatedRecipe)
        }
    }

    fun getRecipesByIds(recipeIds: List<Int>): Flow<List<Recipe>> {
        return repository.getAllRecipes().map { recipes ->
            recipes.filter { it.recipeId in recipeIds }.map { it.toRecipe() }
        }
    }


    fun getRecipeById(recipeId: Int): Flow<RecipeEntity?> {
        return repository.getRecipeById(recipeId)
    }

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

    fun insertIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
        }
    }

    fun getAllIngredients(): Flow<List<IngredientEntity>?> {
        return repository.getAllIngredients()
    }

    fun getIngredientByName(name: String): Flow<IngredientEntity?> {
        return repository.getIngredientByName(name)
    }

    fun getIngredientById(ingredientId: Int): Flow<IngredientEntity?> {
        return repository.getIngredientById(ingredientId)
    }

    fun deleteAllIngredients() {
        viewModelScope.launch {
            repository.deleteAllIngredients()
        }
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "RecipeViewModel"
    }
}