package com.watb.chefmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import com.watb.chefmate.repository.ShoppingTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ShoppingTimeViewModel(private val repository: ShoppingTimeRepository) : ViewModel() {

    fun getAllShoppingTimes(): Flow<List<ShoppingTimeEntity>> = repository.getAllShoppingTimes()

    fun insertShoppingTime(shoppingTime: ShoppingTimeEntity, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertShoppingTime(shoppingTime)
            onDone(id)
        }
    }

    suspend fun getShoppingTimeById(id: Int): ShoppingTimeEntity {
        return repository.getShoppingTimeById(id)
    }

    class Factory(private val repository: ShoppingTimeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShoppingTimeViewModel(repository) as T
        }
    }
}
