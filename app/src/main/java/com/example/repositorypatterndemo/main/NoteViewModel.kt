package com.example.repositorypatterndemo.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.repositorypatterndemo.Coroutines
import com.example.repositorypatterndemo.data.Note
import com.example.repositorypatterndemo.data.NoteRepository
import com.example.repositorypatterndemo.data.Resource

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private var noteRepository: NoteRepository = NoteRepository(application)
    private var allNotes: Resource<LiveData<List<Note>>> = noteRepository.getAllNotes()
    private val note = MutableLiveData<Resource<Note>>()

    fun insert(note: Note) {
        Coroutines.io {
            noteRepository.insert(note)
        }
    }

    fun update(note: Note) {
        Coroutines.io {
            noteRepository.update(note)
        }
    }

    fun delete(note: Note) {
        Coroutines.io {
            noteRepository.delete(note)
        }
    }

    fun deleteAllNotes() {
        Coroutines.io {
            noteRepository.deleteAllNotes()
        }
    }

    fun fakeNetworkCall(isRefreshNeeded: Boolean): LiveData<Resource<Note>> {
        return if (isRefreshNeeded) {
            note.postValue(Resource.loading(null))
            Coroutines.io {
                note.postValue(Resource.success(noteRepository.networkRequest()))
            }
            note
        } else note
    }

    fun getAllNotes(): Resource<LiveData<List<Note>>> {
        return allNotes
    }

    override fun onCleared() {
        super.onCleared()
        Coroutines.job.cancel()
    }

}