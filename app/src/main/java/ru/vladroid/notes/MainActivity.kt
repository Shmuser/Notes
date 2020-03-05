package ru.vladroid.notes

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import ru.vladroid.notes.NotesListAdapter.OnItemClickListener
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NotesViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    private var menu: Menu? = null
    private var noteFragment: ViewNoteFragment? = null

    private val viewModel by lazy {
        NotesViewModel(application)
    }

    private val motionLayout by lazy {
        findViewById<MotionLayout>(R.id.motion_layout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.setBackgroundColor(Color.LTGRAY)
       // this.deleteDatabase("notes-db")
        val fab = findViewById<FloatingActionButton>(R.id.add_note_fab)
        val fragmentBackground = findViewById<LinearLayout>(R.id.fragment_background)
        fragmentBackground.setOnClickListener {
            toNotesState()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view)
        val adapter = NotesListAdapter(this, object : OnItemClickListener {
            override fun onItemClick(note: Note) {
                toNoteFragmentState(note)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.notes.observe(this, Observer {
            adapter.setNotes(it)
        })
        //populateDb()

        fab.setOnClickListener {
            toNoteFragmentState(Note("", 0, 0, Date(0)))
        }

        val itemTouchCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT)
                        .or(makeMovementFlags(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT))
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewHolder.itemView.alpha = 1.0f
                    val pos = viewHolder.adapterPosition
                    val note = viewModel.notes.value?.get(pos)
                    if (note != null) {
                        viewModel.delete(note)
                        adapter.notifyDataSetChanged()
                    }

                    val undoSnackbar: Snackbar =
                        Snackbar.make(recyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                    undoSnackbar.setAction(
                        "undo",
                        View.OnClickListener { note?.let { viewModel.insert(note) } })
                    undoSnackbar.show()
                }
            }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        this.menu = menu
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_delete_all) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("delete all notes?")
                .setPositiveButton("Yes") { _, _ -> viewModel.deleteAll() }
                .setNegativeButton("No") { _, _ -> }
            alertDialog.show()
            return true
        }
        if (item?.itemId == R.id.action_save_note) {
            val note = noteFragment?.getNote()
            if (note?.content?.isNotEmpty()!!) {
                val isUpdate = viewModel.notes.value?.filter { x -> x.id == note.id }?.size != 0
                if (isUpdate) {
                    viewModel.update(note)
                }
                else {
                    val noteId = viewModel.insert(note)
                    noteId.observe(this, object : Observer<Long> {
                        override fun onChanged(t: Long?) {
                            t?.let {
                                noteFragment?.setNoteId(it)
                            }
                        }
                    })
                }
                hideKeyboard()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = false
    }

    private fun showOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = true
    }

    private fun populateDb() {
        val random = Random()
        viewModel.deleteAll()
       // this.deleteDatabase("notes-db")
        for (i in 1..10) {
            viewModel.insert(Note("$i", random.nextInt()%5 + 1,i, Date()))
        }
    }

    private fun toNoteFragmentState(note: Note?) {
        noteFragment = ViewNoteFragment()
        note?.let {
            val noteBundle = Bundle()
            noteBundle.putParcelable("note", note)
            noteFragment?.arguments = noteBundle
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, noteFragment!!)
        transaction.commit()
        motionLayout.transitionToEnd()
        hideOption(R.id.action_delete_all)
        showOption(R.id.action_save_note)
    }

    private fun toNotesState() {
        noteFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        motionLayout.transitionToStart()
        showOption(R.id.action_delete_all)
        hideOption(R.id.action_save_note)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        if (motionLayout.currentState == motionLayout.endState) {
            toNotesState()
        } else {
            super.onBackPressed()
        }
    }
}
