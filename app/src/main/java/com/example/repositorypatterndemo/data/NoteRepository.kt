package com.example.repositorypatterndemo.data

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.delay
import kotlin.random.Random

class NoteRepository(application: Application) {

    //region variables
    private var noteDao: NoteDao
    private var allNotes: LiveData<List<Note>>
    //endregion

    init {
        val database = NoteDatabase.getInstance(application.applicationContext)!!
        noteDao = database.noteDao()
        allNotes = noteDao.getAllNotes()
    }

    //region Network calls
    suspend fun networkRequest(): Note {
        val note = fakeApiCall()
        noteDao.insert(note)
        return note
    }

    private suspend fun fakeApiCall(): Note {
        delay(3000)
        return Note(
            title = "From Network",
            description = "Fake Network Call${Random.nextInt(10000)}",
            priority = Random.nextInt(10)
        )
    }
    //endregion

    fun insert(note: Note) {
        noteDao.insert(note)
    }

    fun update(note: Note) {
        noteDao.update(note)
    }

    fun delete(note: Note) {
        noteDao.delete(note)
    }

    fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    fun getAllNotes(): Resource<LiveData<List<Note>>> {
        return Resource.success(allNotes)
    }

}