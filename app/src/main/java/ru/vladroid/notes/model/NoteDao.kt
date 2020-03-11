package ru.vladroid.notes.model

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Single

@Dao
interface NoteDao {
    @Query("select * from notes order by editDate desc")
    fun getAll(): LiveData<List<Note>>

    @Query("select count(*) from notes")
    fun getSize(): LiveData<Int>

    @Query("select * from notes where type = :type order by editDate desc")
    fun getAllByType(type: Int): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long

    @Delete
    suspend fun delete(note: Note)

    @Query("delete from notes")
    suspend fun deleteAll()

    @Update
    suspend fun update(note: Note)

    @Query("select * from notes where id = :id")
    fun getNote(id: Int): Single<Note>
}