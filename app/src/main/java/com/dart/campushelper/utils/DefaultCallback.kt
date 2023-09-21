package com.dart.campushelper.utils

import android.util.Log
import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.ui.Root
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class DefaultCallback(
    private val scope: CoroutineScope,
    private val uiRelatedLoadingIndicators: List<MutableStateFlow<Boolean>>? = null,
    private val responseSuccessCode: Int = 200,
    val actionWhenResponseSuccess: (response: Response) -> Unit
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        Log.d("DefaultCallback", "获取数据失败，请检查网络连接并重试")
        scope.launch {
            val snackBarResult = MainActivity.snackBarHostState.showSnackbar(
                "获取数据失败，请检查网络连接并重试",
                actionLabel = "重试"
            )
            if (snackBarResult == SnackbarResult.ActionPerformed) {
                MainActivity.snackBarHostState.currentSnackbarData?.dismiss()
                Root.scheduleViewModel.loadCourses()
                Root.gradeViewModel.loadGrades()
                Root.gradeViewModel.loadRankingInfo()
            }
        }
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.code == responseSuccessCode) {
            actionWhenResponseSuccess(response)
            uiRelatedLoadingIndicators?.forEach {
                it.value = false
            }
        } else {
            scope.launch {
                // val snackBarResult = MainActivity.snackBarHostState.showSnackbar("获取数据无效，请稍候片刻，再重试")
                Log.d("DefaultCallback", "获取数据无效，请稍候片刻，再重试")
            }
        }
    }
}