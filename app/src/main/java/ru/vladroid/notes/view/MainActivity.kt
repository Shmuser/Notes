package ru.vladroid.notes.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import ru.vladroid.notes.R
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.presenter.MainPresenterImpl
import ru.vladroid.notes.screenview.ViewNoteFragment
import ru.vladroid.notes.utils.NotesListAdapter
import ru.vladroid.notes.utils.NotesListAdapter.OnItemClickListener
import ru.vladroid.notes.utils.SwipeToDeleteCallback
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var mainPresenter: MainPresenterImpl
    private lateinit var fab: FloatingActionButton
    private var isNoteState = false
    private var menu: Menu? = null
    private var noteFragment: ViewNoteFragment? = null

    private val constraintLayout by lazy {
        findViewById<ConstraintLayout>(R.id.motion_layout)
    }

    companion object {
        val defaultNote = Note("", 0, 0, Date(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_all_notes)
        mainPresenter = MainPresenterImpl(application)
        mainPresenter.attachView(this)

        fab = findViewById<FloatingActionButton>(R.id.add_note_fab)
        val fragmentBackground = findViewById<LinearLayout>(R.id.fragment_background)
        fragmentBackground.setOnClickListener {
            mainPresenter.saveNoteFromFragment()
            mainPresenter.toNotesState()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view)
        val adapter = NotesListAdapter(this, object : OnItemClickListener {
            override fun onItemClick(note: Note) {
                mainPresenter.toNoteFragmentState(note)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        mainPresenter.setNotesObserver(Observer {
            adapter.setNotes(it)
        })

        fab.setOnClickListener {
            mainPresenter.toNoteFragmentState(defaultNote)
        }
        val itemTouchCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewHolder.itemView.alpha = 1.0f
                    val pos = viewHolder.adapterPosition
                    val note = mainPresenter.getNoteAtPos(pos)
                    if (note != null) {
                        mainPresenter.deleteNote(note)
                        adapter.notifyDataSetChanged()
                    }

                    val undoSnackbar: Snackbar =
                        Snackbar.make(recyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                    undoSnackbar.setAction(
                        "undo"
                    ) { note?.let { mainPresenter.insertNote(note) } }
                    undoSnackbar.show()
                }
            }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onStart() {
        super.onStart()
        mainPresenter.checkCurrentIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onPause() {
        super.onPause()
        noteFragment?.isVisible?.let {
            if (it)
                mainPresenter.saveNoteFromFragment()
        }
        mainPresenter.updateWidgetsWithChangedNotes(this)
    }

    override fun onBackPressed() {
        if (noteFragment != null && noteFragment!!.isVisible) {
            mainPresenter.saveNoteFromFragment()
            mainPresenter.toNotesState()
        } else {
            super.onBackPressed()
        }
    }

    override fun getNoteFromFragment(): Note? = noteFragment?.getNote()

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun getNoteFragment(): ViewNoteFragment? = noteFragment

    override fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun toNotesState() {
        updateConstraints(R.layout.activity_main_all_notes)
        isNoteState = false
        showOption(R.id.action_delete_all)
        hideOption(R.id.action_save_note)
    }

    override fun toFragmentState() {
        updateConstraints(R.layout.activity_main_note_view)
        isNoteState = true
        hideOption(R.id.action_delete_all)
        showOption(R.id.action_save_note)
    }

    override fun showFragmentWithNote(note: Note?) {
        noteFragment = ViewNoteFragment()
        note?.let {
            val noteBundle = Bundle()
            noteBundle.putParcelable("note", note)
            noteFragment?.arguments = noteBundle
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, noteFragment!!)
        transaction.commit()
    }

    override fun removeNoteFragment() {
        noteFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        noteFragment = null
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
                .setPositiveButton("Yes") { _, _ -> mainPresenter.deleteAllNotes() }
                .setNegativeButton("No") { _, _ -> }
            alertDialog.show()
            return true
        }
        if (item?.itemId == R.id.action_save_note) {
            mainPresenter.saveNoteFromFragment()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        mainPresenter.detachView()
        super.onDestroy()
    }

    private fun hideOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = false
    }

    private fun showOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = true
    }

    private fun updateConstraints(id: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, id)
        constraintSet.applyTo(constraintLayout)
        TransitionManager.beginDelayedTransition(constraintLayout)
    }
}
