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
    val CREATE_RECIPE_URL: String
        get() = "$MAIN_URL/api/recipes/create"
    val INCREASE_VIEW_COUNT_URL: String
        get() = "$MAIN_URL/api/recipes/increase-view-count"

    // interaction
    val LIKE_URL: String
        get() = "$MAIN_URL/api/interactions/like"
    val COMMENT_URL: String
        get() = "$MAIN_URL/api/interactions/comment"

    fun setMainUrl(url: String) {
        MAIN_URL = "https://${url.trim()}.loca.lt"
    }
}