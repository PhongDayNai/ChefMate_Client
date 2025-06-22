package com.watb.chefmate.api

object ApiConstant {
    var MAIN_URL = ""

    // account
    val LOGIN_URL: String
        get() = "$MAIN_URL/api/users/login"
    val REGISTER_URL: String
        get() = "$MAIN_URL/api/users/register"

    // recipe
    val TOP_TRENDING_URL: String
        get() = "$MAIN_URL/api/recipes/top-trending"
    val SEARCH_URL: String
        get() = "$MAIN_URL/api/recipes/search"
    val SEARCH_BY_TAG_URL: String
        get() = "$MAIN_URL/api/recipes/search-by-tag"
    val CREATE_RECIPE_URL: String
        get() = "$MAIN_URL/api/recipes/create"
    val GET_ALL_INGREDIENTS_URL: String
        get() = "$MAIN_URL/api/recipes/ingredients"
    val GET_ALL_TAGS_URL: String
        get() = "$MAIN_URL/api/recipes/tags"

    // interaction
    val LIKE_URL: String
        get() = "$MAIN_URL/api/interactions/like"
    val COMMENT_URL: String
        get() = "$MAIN_URL/api/interactions/comment"
    val INCREASE_VIEW_COUNT_URL: String
        get() = "$MAIN_URL/api/interactions/increase-view-count"

    fun setMainUrl(url: String) {
        MAIN_URL = "https://${url.trim()}.loca.lt"
    }
}