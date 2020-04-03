package ru.vladroid.notes.model

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vladroid.notes.utils.App
import javax.inject.Inject


class NotesModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private var notes: LiveData<List<Note>>
    private var repository: NotesRepository = App.getAppComponent().getNotesRepository()
    private var sp: SharedPreferences = App.getAppComponent().getSharedPrefs()

    init {
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

    fun getNotesList() = notes

    fun getNoteAtPos(pos: Int): Note? = notes.value?.get(pos)

    fun getSP() = sp
}