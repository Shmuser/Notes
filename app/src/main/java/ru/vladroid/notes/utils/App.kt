package ru.vladroid.notes.utils

import android.app.Application
import ru.vladroid.notes.dependencies.AppComponent
import ru.vladroid.notes.dependencies.ApplicationModule
import ru.vladroid.notes.dependencies.DaggerAppComponent

class App : Application() {

    private lateinit var daggerAppComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        daggerAppComponent = DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }

    fun getAppComponent() = daggerAppComponent
}