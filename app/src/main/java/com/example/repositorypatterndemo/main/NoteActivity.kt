package com.example.repositorypatterndemo.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.repositorypatterndemo.R
import com.example.repositorypatterndemo.addnote.AddNoteActivity
import com.example.repositorypatterndemo.data.Note
import com.example.repositorypatterndemo.data.NoteDatabase
import com.example.repositorypatterndemo.data.Resource.Status
import kotlinx.android.synthetic.main.activity_main.*

class NoteActivity : AppCompatActivity() {

    companion object {
        const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        when (noteViewModel.getAllNotes().status) {
            Status.SUCCESS -> {
                noteViewModel.getAllNotes().data?.observe(this, {
                    if (it.isEmpty()) {
                        getDataFromApi()
                    }
                    Toast.makeText(this, "List Updated!", Toast.LENGTH_SHORT).show()
                    pg.visibility = View.GONE
                    adapter.submitList(it)
                })
            }
            Status.LOADING -> {
                pg.visibility = View.VISIBLE
            }
            else -> {
                Toast.makeText(this, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
            }
        }

        //region Clicks
        buttonAddNote.setOnClickListener {
            startActivityForResult(
                Intent(this, AddNoteActivity::class.java),
                ADD_NOTE_REQUEST
            )
        }

        buttonAddNoteFromNetwork.setOnClickListener {
            getDataFromApi()
        }
        //endregion

    }

    private fun getDataFromApi() {
        noteViewModel.fakeNetworkCall(false).removeObservers(this)

        noteViewModel.fakeNetworkCall(true).observe(this, {
            when (it.status) {
                Status.SUCCESS -> {
                    pg.visibility = View.GONE
                }
                Status.LOADING -> {
                    pg.visibility = View.VISIBLE
                }
                else -> {
                    Toast.makeText(this, "Some Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        adapter = NoteAdapter()
        recycler_view.adapter = adapter

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.adapterPosition))
                Toast.makeText(baseContext, "Note Deleted!", Toast.LENGTH_SHORT).show()
            }
        }
        ).attachToRecyclerView(recycler_view)

        adapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                val intent = Intent(baseContext, AddNoteActivity::class.java)
                intent.putExtra(AddNoteActivity.EXTRA_ID, note.id)
                intent.putExtra(AddNoteActivity.EXTRA_TITLE, note.title)
                intent.putExtra(AddNoteActivity.EXTRA_DESCRIPTION, note.description)
                intent.putExtra(AddNoteActivity.EXTRA_PRIORITY, note.priority)

                startActivityForResult(intent, EDIT_NOTE_REQUEST)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_all_notes -> {
                noteViewModel.deleteAllNotes()
                Toast.makeText(this, "All notes deleted!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK -> {
                val newNote = data!!.getStringExtra(AddNoteActivity.EXTRA_TITLE)?.let {
                    data.getStringExtra(AddNoteActivity.EXTRA_DESCRIPTION)?.let { it1 ->
                        Note(
                            it,
                            it1,
                            data.getIntExtra(AddNoteActivity.EXTRA_PRIORITY, 1)
                        )
                    }
                }
                newNote?.let { noteViewModel.insert(it) }

            }
            requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK -> {

                when (data?.getIntExtra(AddNoteActivity.EXTRA_ID, -1)) {
                    -1 -> {
                        Toast.makeText(this, "Could not update! Error!", Toast.LENGTH_SHORT).show()
                    }
                }

                val updateNote = data!!.getStringExtra(AddNoteActivity.EXTRA_TITLE)?.let {
                    data.getStringExtra(AddNoteActivity.EXTRA_DESCRIPTION)?.let { it1 ->
                        Note(
                            it,
                            it1,
                            data.getIntExtra(AddNoteActivity.EXTRA_PRIORITY, 1)
                        )
                    }
                }
                when {
                    updateNote != null -> {
                        updateNote.id = data.getIntExtra(AddNoteActivity.EXTRA_ID, -1)
                    }
                }
                updateNote?.let { noteViewModel.update(it) }

            }
            else -> {
                Toast.makeText(this, "Note not saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NoteDatabase.destroyInstance()
    }

}
