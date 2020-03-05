package ru.vladroid.notes.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotesViewModel(application: Application): AndroidViewModel(application) {
    private val repository: NotesRepository
    var notes: LiveData<List<Note>>

    init {
        val notesDao = NotesDatabase.getInstance(application).notesDao()
        repository = NotesRepository(notesDao)
        notes = repository.allNotes
    }

    fun insert(note: Note): MutableLiveData<Long> {
        val insertedId = MutableLiveData<Long>()
        viewModelScope.launch {
            insertedId.postValue(repository.insert(note))
        }
        return insertedId
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    fun getSize() = repository.getSize()
}