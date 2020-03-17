package ru.vladroid.notes.view

import androidx.lifecycle.LifecycleOwner
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.screenview.ViewNoteFragment

interface MainView {

    fun getNoteFromFragment(): Note?

    fun getLifecycleOwner(): LifecycleOwner

    fun getNoteFragment(): ViewNoteFragment?

    fun hideKeyboard()

    fun toNotesState()

    fun removeNoteFragment()

    fun toFragmentState()

    fun showFragmentWithNote(note: Note?)
}