package com.watb.chefmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import com.watb.chefmate.repository.ShoppingTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ShoppingTimeViewModel(private val repository: ShoppingTimeRepository) : ViewModel() {
    private val _shoppingTimes = MutableStateFlow<List<ShoppingTimeEntity>>(emptyList())
    val shoppingTimes: MutableStateFlow<List<ShoppingTimeEntity>> = _shoppingTimes

    private val _shoppingTime = MutableStateFlow(ShoppingTimeEntity(0,"", "", "", "", "", ""))

    private val _shoppingRecipeNames = MutableStateFlow<List<String>>(emptyList())
    val shoppingRecipeNames: MutableStateFlow<List<String>> = _shoppingRecipeNames

    private val _shoppingIngredientNames = MutableStateFlow<List<String>>(emptyList())
    val shoppingIngredientNames: MutableStateFlow<List<String>> = _shoppingIngredientNames

    private val _shoppingIngredientWeights = MutableStateFlow<List<String>>(emptyList())
    val shoppingIngredientWeights: MutableStateFlow<List<String>> = _shoppingIngredientWeights

    private val _shoppingIngredientUnits = MutableStateFlow<List<String>>(emptyList())
    val shoppingIngredientUnits: MutableStateFlow<List<String>> = _shoppingIngredientUnits

    private val _shoppingIngredientStatuses = MutableStateFlow<List<String>>(emptyList())
    val shoppingIngredientStatuses: MutableStateFlow<List<String>> = _shoppingIngredientStatuses

    fun getAllShoppingTimes() {
        viewModelScope.launch {
            repository.getAllShoppingTimes().collect {
                _shoppingTimes.value = it
            }
        }
    }

    fun insertShoppingTime(shoppingTime: ShoppingTimeEntity, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertShoppingTime(shoppingTime)
            onDone(id)
        }
    }

    suspend fun getShoppingTimeById(id: Int) {
        _shoppingTime.value = repository.getShoppingTimeById(id)
        _shoppingRecipeNames.value = _shoppingTime.value.recipeNames.split(";;;")
        _shoppingIngredientNames.value = _shoppingTime.value.ingredientNames.split(";;;")
        _shoppingIngredientWeights.value = _shoppingTime.value.ingredientWeights.split(";;;")
        _shoppingIngredientUnits.value = _shoppingTime.value.ingredientUnits.split(";;;")
        _shoppingIngredientStatuses.value = _shoppingTime.value.buyingStatuses.split(";;;")
    }

    fun updateShoppingTimeById(id: Int, ingredientNames: String, ingredientWeights: String, ingredientUnits: String, ingredientStatuses: String) {
        viewModelScope.launch {
            repository.updateShoppingTimeById(id, ingredientNames, ingredientWeights, ingredientUnits, ingredientStatuses)
            getShoppingTimeById(id)
        }
    }
}
