package com.dart.campushelper

import android.app.Application
import android.content.Context
import com.dart.campushelper.data.AppContainer
import com.dart.campushelper.data.AppContainerImpl

class App : Application() {

    companion object {
        // AppContainer instance used by the rest of classes to obtain dependencies
        lateinit var container: AppContainer
            private set
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()

        container = AppContainerImpl(this)
        context = this
    }
}
