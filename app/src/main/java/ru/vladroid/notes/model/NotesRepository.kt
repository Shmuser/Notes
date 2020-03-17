package ru.vladroid.notes.model

import androidx.lifecycle.LiveData
import javax.inject.Inject

class NotesRepository @Inject constructor(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAll()

    fun getNotesByType(type: Int) = noteDao.getAllByType(type)

    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    fun getSize() = noteDao.getSize()
}
