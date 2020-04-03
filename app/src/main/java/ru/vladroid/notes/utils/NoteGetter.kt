package ru.vladroid.notes.utils

import io.reactivex.Single
import ru.vladroid.notes.model.Note

class NoteGetter {
    companion object {
        fun getNoteById(id: Int): Single<Note> {
            val notesRepository = App.getAppComponent().getNotesRepository()
            return notesRepository.getNote(id)
        }
    }
}