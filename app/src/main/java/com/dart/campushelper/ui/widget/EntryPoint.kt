package com.dart.campushelper.ui.widget

import com.dart.campushelper.viewmodel.AppWidgetViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EntryPoint {

    fun vm(): AppWidgetViewModel
}