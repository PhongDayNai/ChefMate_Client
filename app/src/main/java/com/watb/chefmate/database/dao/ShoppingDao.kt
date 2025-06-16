package com.watb.chefmate.database.dao

import androidx.room.*
import com.watb.chefmate.database.entities.*
import com.watb.chefmate.database.relations.ShoppingIngredientsWithDetails
import kotlinx.coroutines.flow.Flow

//@Dao
//interface ShoppingDao {
//    // --- ShoppingTimes ---
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertShoppingTime(shoppingTimes: ShoppingTimeEntity): Long
//
//    @Delete
//    suspend fun deleteShoppingTime(shoppingTimes: ShoppingTimeEntity)
//
//    @Query("SELECT * FROM ShoppingTimes ORDER BY creationDate DESC")
//    fun getAllShoppingTimes(): Flow<List<ShoppingTimeEntity>>
//
//    @Query("SELECT * FROM ShoppingTimes WHERE stId = :stId")
//    suspend fun getShoppingTimeById(stId: Int): ShoppingTimeEntity?
//
//    // --- ShoppingIngredients ---
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)
//
//    @Update
//    suspend fun updateShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)
//
//    @Delete
//    suspend fun deleteShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)
//
//    @Query("DELETE FROM ShoppingIngredients WHERE stId = :stId AND ingredientId = :ingredientId")
//    suspend fun deleteShoppingIngredientByStIdAndIngredientId(stId: Int, ingredientId: Int)
//
//    // Lấy danh sách nguyên liệu mua sắm cho một ShoppingTime cụ thể, kèm theo thông tin ingredientName
//    @Transaction
//    @Query("""
//        SELECT
//            si.*,
//            i.ingredientName as ingredientName
//        FROM ShoppingIngredients si
//        INNER JOIN Ingredients i ON si.ingredientId = i.ingredientId
//        WHERE si.stId = :stId
//        ORDER BY i.ingredientName ASC
//    """)
//    fun getShoppingListDetails(stId: Int): Flow<List<ShoppingIngredientsWithDetails>>
//
//    @Query("UPDATE ShoppingIngredients SET isBought = :isBought WHERE siId = :siId")
//    suspend fun updateShoppingIngredientBoughtStatus(siId: Int, isBought: Boolean)
//
//    @Query("DELETE FROM ShoppingIngredients WHERE stId = :stId")
//    suspend fun deleteAllShoppingIngredientsInTime(stId: Int)
//
//    // --- ShoppingRecipes ---
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertShoppingRecipe(shoppingRecipes: ShoppingRecipeEntity)
//
//    @Delete
//    suspend fun deleteShoppingRecipe(shoppingRecipes: ShoppingRecipeEntity)
//
//    @Query("DELETE FROM ShoppingRecipes WHERE stId = :stId")
//    suspend fun deleteAllShoppingRecipesInTime(stId: Int)
//
//    // Lấy các RecipesIngredients cho các công thức trong một ShoppingTime cụ thể
//    // Lưu ý: Query này cần được điều chỉnh để lấy RecipesIngredients dựa trên danh sách recipeIds,
//    // không phải dựa vào stId của ShoppingRecipes, vì ShoppingRecipes chỉ là liên kết.
//    @Query("""
//        SELECT ri.*
//        FROM RecipesIngredients ri
//        INNER JOIN ShoppingRecipes sr ON ri.recipeId = sr.recipeId
//        WHERE sr.stId = :stId
//    """)
//    suspend fun getRecipesIngredientsForShoppingTime(stId: Int): List<RecipeIngredientEntity>
//
//    // Thêm một query để lấy RecipesIngredients theo danh sách recipeIds
//    @Query("""
//        SELECT *
//        FROM RecipesIngredients
//        WHERE recipeId IN (:recipeIds)
//    """)
//    suspend fun getRecipesIngredientsByRecipeIds(recipeIds: List<Int>): List<RecipeIngredientEntity>
//}
@Dao
interface ShoppingDao {
    // --- ShoppingTimes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingTime(shoppingTimes: ShoppingTimeEntity): Long

    @Delete
    suspend fun deleteShoppingTime(shoppingTimes: ShoppingTimeEntity)

    @Query("SELECT * FROM ShoppingTimes ORDER BY creationDate DESC")
    fun getAllShoppingTimes(): Flow<List<ShoppingTimeEntity>>

    @Query("SELECT * FROM ShoppingTimes WHERE stId = :stId")
    suspend fun getShoppingTimeById(stId: Int): ShoppingTimeEntity?

    // --- ShoppingIngredients ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)

    @Update
    suspend fun updateShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)

    @Delete
    suspend fun deleteShoppingIngredient(shoppingIngredients: ShoppingIngredientEntity)

    @Query("DELETE FROM ShoppingIngredients WHERE stId = :stId AND ingredientName = :ingredientName")
    suspend fun deleteShoppingIngredientByStIdAndIngredientName(stId: Int, ingredientName: String)

    // Lấy danh sách nguyên liệu mua sắm cho một ShoppingTime cụ thể, kèm theo thông tin ingredientName
    // Since ingredientName is now directly in ShoppingIngredientEntity, ShoppingIngredientsWithDetails
    // might be simplified or removed if it only served to join with IngredientEntity.
    // For now, let's assume ShoppingIngredientsWithDetails is still used to represent a richer object
    // if other fields were to be added later, or simplify the return type if not.
    // Assuming ShoppingIngredientsWithDetails now just wraps ShoppingIngredientEntity directly
    // or is adapted to only include what's in ShoppingIngredientEntity + a recipe name if needed.
    @Query("""
        SELECT
            si.*
        FROM ShoppingIngredients si
        WHERE si.stId = :stId
    """)
    fun getShoppingIngredientsForShoppingTime(stId: Int): Flow<List<ShoppingIngredientEntity>>

    @Query("UPDATE ShoppingIngredients SET isBought = :isBought WHERE siId = :siId")
    suspend fun updateShoppingIngredientBoughtStatus(siId: Int, isBought: Boolean)

    @Query("DELETE FROM ShoppingIngredients WHERE stId = :stId")
    suspend fun deleteAllShoppingIngredientsInTime(stId: Int)

    // --- ShoppingRecipes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingRecipe(shoppingRecipes: ShoppingRecipeEntity)

    @Delete
    suspend fun deleteShoppingRecipe(shoppingRecipes: ShoppingRecipeEntity)

    @Query("DELETE FROM ShoppingRecipes WHERE stId = :stId")
    suspend fun deleteAllShoppingRecipesInTime(stId: Int)

    // This query needs to be re-evaluated. RecipesIngredients is also removed.
    // If the goal was to get all ingredients from recipes linked to a shopping time,
    // you would now parse the 'ingredients' String from RecipeEntity for each linked recipe.
    // This cannot be done directly in Room with a single query across complex string parsing.
    // This function will likely need to be handled in the repository or service layer.
    // For now, I'm commenting out the old and adding a placeholder if you still need recipe details.
    @Query("""
        SELECT r.*
        FROM Recipes r
        INNER JOIN ShoppingRecipes sr ON r.recipeId = sr.recipeId
        WHERE sr.stId = :stId
    """)
    suspend fun getRecipesForShoppingTime(stId: Int): List<RecipeEntity>
}