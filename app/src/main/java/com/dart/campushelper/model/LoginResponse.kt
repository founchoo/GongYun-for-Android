package com.dart.campushelper.model

class LoginResponse(
    val isSuccess: Boolean,
    val message: String?,
) {

    companion object {
        fun error(): LoginResponse {
            return LoginResponse(
                isSuccess = false,
                message = null,
            )
        }
    }
}