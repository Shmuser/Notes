package ru.vladroid.notes.utils

import android.content.Context
import io.reactivex.Single
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NotesDatabase

class NoteGetter {
    companion object {
        fun getNoteById(context: Context, id: Int): Single<Note> {
            val db = NotesDatabase.getInstance(context)
            val noteDao = db.notesDao()
            return noteDao.getNote(id)
        }
    }
}