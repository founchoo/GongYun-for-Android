package com.dart.campushelper.utils

import android.util.Log
import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ResponseErrorHandler<T> (
    private val response: retrofit2.Response<T>,
    private val scope: CoroutineScope,
    private val responseSuccessCode: Int = 200,
    val actionWhenResponseSuccess: (body: T?) -> Unit,
    val actionWhenResponseFail: (response: retrofit2.Response<T>) -> Unit = {},
    private val ignoreResponseNull: Boolean = false
) {
    init {
        if (response.code() == responseSuccessCode) {
            if (ignoreResponseNull) {
                actionWhenResponseSuccess(null)
            } else {
                actionWhenResponseSuccess(response.body())
            }
        } else {
            Log.d("ResponseErrorHandler", "获取数据失败，请检查网络连接并重试")
            scope.launch {
                actionWhenResponseFail(response)
                val snackBarResult = MainActivity.snackBarHostState.showSnackbar(
                    "获取数据失败，请检查网络连接并重试（当前版本未能实现点击重试按钮进行重复请求，请退出 APP，重新检查网络连接后再打开 APP 重试）。",
                    actionLabel = "重试"
                )
                if (snackBarResult == SnackbarResult.ActionPerformed) {
                    MainActivity.snackBarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}