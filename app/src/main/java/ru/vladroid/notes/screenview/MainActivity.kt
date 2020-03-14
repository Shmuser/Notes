package ru.vladroid.notes.screenview

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.vladroid.notes.R
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NotesViewModel
import ru.vladroid.notes.utils.NoteGetter
import ru.vladroid.notes.utils.NotesListAdapter
import ru.vladroid.notes.utils.NotesListAdapter.OnItemClickListener
import ru.vladroid.notes.utils.SharedPrefsHelper
import ru.vladroid.notes.utils.SwipeToDeleteCallback
import ru.vladroid.notes.widget.NoteWidget
import java.util.*

class MainActivity : AppCompatActivity() {

    private var menu: Menu? = null
    private var noteFragment: ViewNoteFragment? = null
    private val changedNotes = mutableSetOf<Note>()
    private var isNoteState = false

    private val viewModel by lazy {
        NotesViewModel(application)
    }

    private val constraintLayout by lazy {
        findViewById<ConstraintLayout>(R.id.motion_layout)
    }

    private val sp by lazy {
        getSharedPreferences(NoteWidget.WIDGET_PREF, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_all_notes)
        window.decorView.setBackgroundColor(Color.LTGRAY)
        // this.deleteDatabase("notes-db")
        val fab = findViewById<FloatingActionButton>(R.id.add_note_fab)
        val fragmentBackground = findViewById<LinearLayout>(R.id.fragment_background)
        fragmentBackground.setOnClickListener {
            saveNoteFromFragment()
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
        // populateDb()

        fab.setOnClickListener {
            toNoteFragmentState(Note("", 0, 0, Date(0)))
        }

        val itemTouchCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(
                        ItemTouchHelper.ACTION_STATE_IDLE,
                        ItemTouchHelper.LEFT
                    )
                        .or(
                            makeMovementFlags(
                                ItemTouchHelper.ACTION_STATE_SWIPE,
                                ItemTouchHelper.LEFT
                            )
                        )
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

    override fun onStart() {
        super.onStart()
        intent?.let {
            if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                val noteId = intent.getIntExtra(NoteWidget.WIDGET_NOTE_ID, -1)
                NoteGetter.getNoteById(this, noteId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : SingleObserver<Note> {
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                            }

                            override fun onSuccess(t: Note) {
                                toNoteFragmentState(t)
                            }
                        })
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onPause() {
        super.onPause()
        noteFragment?.isVisible?.let {
            saveNoteFromFragment()
        }
        for (note in changedNotes) {
            updateWidgets(note)
        }
        changedNotes.clear()
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
            saveNoteFromFragment()
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
            viewModel.insert(Note("$i", random.nextInt() % 5 + 1, i, Date()))
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
        transaction.replace(R.id.fragment_container, noteFragment!!)
        transaction.commit()
        updateConstraints(R.layout.activity_main_note_view)
        isNoteState = true
        hideOption(R.id.action_delete_all)
        showOption(R.id.action_save_note)
    }

    private fun toNotesState() {
        noteFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        updateConstraints(R.layout.activity_main_all_notes)
        isNoteState = false
        showOption(R.id.action_delete_all)
        hideOption(R.id.action_save_note)
    }

    private fun updateConstraints(id: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, id)
        constraintSet.applyTo(constraintLayout)
        TransitionManager.beginDelayedTransition(constraintLayout)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun saveNoteFromFragment() {
        val note = noteFragment?.getNote()
        if (note?.content?.isNotEmpty()!!) {
            val notesArray = viewModel.notes.value?.filter { x -> x.id == note.id }
            if (notesArray != null && notesArray.isNotEmpty()) {
                val oldNote = notesArray[0]
                if (note.content != oldNote.content || note.type != oldNote.type) {
                    viewModel.update(note)
                    changedNotes.add(note)
                }
            } else {
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

    private fun updateWidgets(note: Note) {
        // loadSharedPrefs()
        val widgetIds = SharedPrefsHelper.getWidgetIdsByNoteId(sp, note.id)
        if (widgetIds.size > 0) {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            for (widgetId in widgetIds) {
                NoteWidget.updateAppWidget(this, appWidgetManager, sp, widgetId, note)
            }
        }
    }

    override fun onBackPressed() {
        if (isNoteState) {
            saveNoteFromFragment()
            toNotesState()
        } else {
            super.onBackPressed()

        }
    }

    fun loadSharedPrefs() {
        Log.i("Loading Shared Prefs", "-----------------------------------")
        Log.i("----------------", "---------------------------------------")

        val preference = getSharedPreferences(NoteWidget.WIDGET_PREF, MODE_PRIVATE)
        for (key in preference.all.keys) {
            Log.i(key, preference.getInt(key, -1).toString())
        }
        Log.i("----------------", "---------------------------------------")

        Log.i("Finished Shared Prefs", "----------------------------------")
    }
}
