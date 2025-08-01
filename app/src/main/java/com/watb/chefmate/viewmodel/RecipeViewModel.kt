package com.watb.chefmate.viewmodel

import android.annotation.SuppressLint
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
import com.watb.chefmate.database.entities.TagEntity
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

    private val _personalRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val personalRecipes: StateFlow<List<Recipe>> = _personalRecipes

    val allRecipes: Flow<List<Recipe>> =
        repository.getAllRecipes().map { list ->
            list.map { it.toRecipe() }
        }

    suspend fun getTopTrending(userId: Int? = null) {
        val response = ApiClient.getTopTrending(userId)
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
            val response = ApiClient.searchRecipe(recipeName.trim(), userId)
            response?.data?.let {
                _searchResult.value = it
            }
            _isLoading.value = false
        }
    }

    fun searchRecipeByTag(tag: String, userId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResult.value = emptyList()
            val response = ApiClient.searchRecipeByTag(tag.trim(), userId)
            response?.data?.let {
                _searchResult.value = it
            }
            _isLoading.value = false
        }
    }

    fun getPersonalRecipes(userId: Int) {
        viewModelScope.launch {
            val response = ApiClient.getRecipesByUserId(userId)
            response?.data?.let {
                _personalRecipes.value = it
            }
        }
    }

    fun getIATDataFromServer() {
        viewModelScope.launch {
            launch {
                val ingredientsResponse = ApiClient.getAllIngredients()
                ingredientsResponse?.data?.let {
                    val localIngredients: Flow<List<IngredientEntity>?> = getAllIngredients()
                    if (localIngredients.first() != null) {
                        if (ingredientsResponse.data.size != localIngredients.first()!!.size) {
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
            launch {
                val tagsResponse = ApiClient.getAllTags()
                tagsResponse?.data?.let {
                    val localTags: Flow<List<TagEntity>?> = getAllTags()
                    if (localTags.first() != null) {
                        if (tagsResponse.data.size != localTags.first()!!.size) {
                            deleteAllTags()
                            it.forEach { tag ->
                                insertTag(tag)
                            }
                        }
                    } else {
                        it.forEach { tag ->
                            insertTag(tag)
                        }
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
        createdAt: String,
        ingredients: List<Pair<String, Pair<Int, String>>>,
        steps: List<Pair<Int, String>>,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val ingredientNames = ingredients.joinToString(";;;") { it.first }
            val ingredientWeights = ingredients.joinToString(";;;") { it.second.first.toString() }
            val ingredientUnits = ingredients.joinToString(";;;") { it.second.second }
            val cookingSteps = steps.sortedBy { it.first }.joinToString(";;;") { it.second }
            val tagsString = tags.joinToString(";;;")

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
                tags = tagsString
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
        steps: List<Pair<Int, String>>,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val ingredientNames = ingredients.joinToString(";;;") { it.first }
            val ingredientWeights = ingredients.joinToString(";;;") { it.second.first.toString() }
            val ingredientUnits = ingredients.joinToString(";;;") { it.second.second }
            val cookingSteps = steps.sortedBy { it.first }.joinToString(";;;") { it.second }
            val tagsString = tags.joinToString(";;;")

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
                tags = tagsString
            )
            repository.updateRecipe(updatedRecipe)
        }
    }

    fun deleteRecipeById(recipeId: Int) {
        viewModelScope.launch {
            repository.deleteRecipeById(recipeId)
        }
    }

    fun getRecipeById(recipeId: Int): Flow<Recipe?> {
        return repository.getRecipeById(recipeId).map { it?.toRecipe() }
    }

    fun getRecipeByName(name: String): Flow<Recipe?> {
        return repository.getRecipeByName(name).map { it?.toRecipe() }
    }

    private fun insertIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
        }
    }

    fun getAllIngredients(): Flow<List<IngredientEntity>?> {
        return repository.getAllIngredients()
    }

    private fun deleteAllIngredients() {
        viewModelScope.launch {
            repository.deleteAllIngredients()
        }
    }

    private fun insertTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.insertTag(tag)
        }
    }

    fun getAllTags(): Flow<List<TagEntity>?> {
        return repository.getAllTags()
    }

    private fun deleteAllTags() {
        viewModelScope.launch {
            repository.deleteAllTags()
        }
    }

    companion object {
        private const val TAG = "RecipeViewModel"
    }
}