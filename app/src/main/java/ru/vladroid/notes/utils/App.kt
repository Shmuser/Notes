package ru.vladroid.notes.utils

import android.app.Application
import ru.vladroid.notes.dependencies.AppComponent
import ru.vladroid.notes.dependencies.ApplicationModule
import ru.vladroid.notes.dependencies.DaggerAppComponent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        daggerAppComponent = DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }

    companion object {
        private lateinit var daggerAppComponent: AppComponent

        fun getAppComponent() = daggerAppComponent
    }
}