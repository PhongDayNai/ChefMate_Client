package com.watb.chefmate.api

import com.watb.chefmate.BuildConfig

object ApiConstant {
    val MAIN_URL: String
        get() = BuildConfig.API_BASE_URL.trim().trimEnd('/')

    private const val V2 = "/v2"

    val LOGIN_URL: String
        get() = "$MAIN_URL$V2/users/login"
    val REGISTER_URL: String
        get() = "$MAIN_URL$V2/users/register"
    val REFRESH_TOKEN_URL: String
        get() = "$MAIN_URL$V2/users/refresh-token"
    val UPDATE_USER_INFORMATION_URL: String
        get() = "$MAIN_URL$V2/users/me"
    val CHANGE_PASSWORD_URL: String
        get() = "$MAIN_URL$V2/users/change-password"

    val TOP_TRENDING_URL: String
        get() = "$MAIN_URL$V2/recipes/trending-v2"
    val SEARCH_URL: String
        get() = "$MAIN_URL$V2/recipes/search"
    val SEARCH_BY_TAG_URL: String
        get() = "$MAIN_URL$V2/recipes/search-by-tag"
    val GET_RECIPES_BY_USER_ID_URL: String
        get() = "$MAIN_URL$V2/recipes/me"
    val GET_ALL_RECIPES_URL: String
        get() = "$MAIN_URL$V2/recipes/all"
    val CREATE_RECIPE_URL: String
        get() = "$MAIN_URL$V2/recipes/create"
    val GET_ALL_INGREDIENTS_URL: String
        get() = "$MAIN_URL$V2/recipes/ingredients"
    val GET_ALL_TAGS_URL: String
        get() = "$MAIN_URL$V2/recipes/tags"

    val LIKE_URL: String
        get() = "$MAIN_URL$V2/interactions/like"
    val COMMENT_URL: String
        get() = "$MAIN_URL$V2/interactions/comment"
    val INCREASE_VIEW_COUNT_URL: String
        get() = "$MAIN_URL$V2/interactions/increase-view-count"
}
