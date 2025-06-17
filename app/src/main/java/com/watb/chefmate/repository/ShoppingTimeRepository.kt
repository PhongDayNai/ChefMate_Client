package com.watb.chefmate.repository

import com.watb.chefmate.database.dao.ShoppingTimeDao
import com.watb.chefmate.database.entities.ShoppingTimeEntity
import kotlinx.coroutines.flow.Flow

class ShoppingTimeRepository(private val shoppingTimeDao: ShoppingTimeDao) {

    suspend fun insertShoppingTime(shoppingTime: ShoppingTimeEntity): Long {
        return shoppingTimeDao.insertShoppingTime(shoppingTime)
    }

    fun getAllShoppingTimes(): Flow<List<ShoppingTimeEntity>> {
        return shoppingTimeDao.getAllShoppingTimes()
    }

    suspend fun getShoppingTimeById(id: Int): ShoppingTimeEntity {
        return shoppingTimeDao.getShoppingTimeById(id)
    }
}
