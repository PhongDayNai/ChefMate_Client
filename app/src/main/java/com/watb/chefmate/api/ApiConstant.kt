package com.watb.chefmate.api

object ApiConstant {
    var MAIN_URL = ""

    val LOGIN_URL: String
        get() = "$MAIN_URL/api/users/login"
    val REGISTER_URL: String
        get() = "$MAIN_URL/api/users/register"

    val TOP_TRENDING_URL: String
        get() = "$MAIN_URL/api/recipes/top-trending"

    fun setMainUrl(url: String) {
        MAIN_URL = "https://${url.trim()}.loca.lt"
    }
}