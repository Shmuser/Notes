package ru.vladroid.notes.dependencies

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.vladroid.notes.model.NotesDatabase
import ru.vladroid.notes.model.NotesModel
import ru.vladroid.notes.model.NotesRepository
import ru.vladroid.notes.utils.AppConstants
import javax.inject.Singleton


@Component(modules = [SharedPrefsModule::class, NotesModule::class])
@Singleton
interface AppComponent {

    fun getSharedPrefs(): SharedPreferences

    fun getNotesRepository(): NotesRepository

    fun getNotesModel(): NotesModel
}


@Module(includes = [ApplicationModule::class])
class SharedPrefsModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(app: Application): SharedPreferences =
        app.getSharedPreferences(AppConstants.WIDGET_PREF, Context.MODE_PRIVATE)
}


@Module(includes = [ApplicationModule::class, SharedPrefsModule::class])
class NotesModule {

    @Provides
    @Singleton
    fun provideNotesRepository(application: Application): NotesRepository {
        val notesDao = NotesDatabase.getInstance(application).notesDao()
        return NotesRepository(notesDao)
    }

    @Provides
    @Singleton
    fun provideNotesModel(
        application: Application,
        notesRepository: NotesRepository,
        sharedPreferences: SharedPreferences
    ): NotesModel = NotesModel(application, notesRepository, sharedPreferences)
}


@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplication() = application
}