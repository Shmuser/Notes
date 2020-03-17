package ru.vladroid.notes.presenter

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.view.MainView

interface MainPresenter {

    fun detachView()

    fun attachView(mainView: MainView)

    fun saveNoteFromFragment()

    fun toNotesState()

    fun toNoteFragmentState(note: Note)

    fun setNotesObserver(observer: Observer<List<Note>>)

    fun getNoteAtPos(pos: Int): Note?

    fun insertNote(note: Note)

    fun deleteNote(note: Note)

    fun checkCurrentIntent(intent: Intent, context: Context)

    fun updateWidgetsWithChangedNotes(context: Context)

    fun deleteAllNotes()
}