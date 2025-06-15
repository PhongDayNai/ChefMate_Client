package com.watb.chefmate.api

object ApiConstant {
    var MAIN_URL = ""

    val LOGIN_URL: String
        get() = "$MAIN_URL/users/login"
    val REGISTER_URL: String
        get() = "$MAIN_URL/users/register"

    fun setMainUrl(url: String) {
        MAIN_URL = "https://${url.trim()}.loca.lt/"
    }
}