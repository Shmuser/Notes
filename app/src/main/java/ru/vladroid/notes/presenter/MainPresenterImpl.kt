package ru.vladroid.notes.presenter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NotesModel
import ru.vladroid.notes.utils.App
import ru.vladroid.notes.utils.AppConstants
import ru.vladroid.notes.utils.NoteGetter
import ru.vladroid.notes.utils.SharedPrefsHelper
import ru.vladroid.notes.view.MainView
import ru.vladroid.notes.widget.NoteWidget

class MainPresenterImpl : MainPresenter {
    private var mainView: MainView? = null

    private var notesModel: NotesModel = App.getAppComponent()
        .getNotesModel()

    private val changedNotes = mutableSetOf<Note>()

    override fun detachView() {
        mainView = null
    }

    override fun attachView(mainView: MainView) {
        this.mainView = mainView

    }

    override fun saveNoteFromFragment() {
        val note = mainView?.getNoteFromFragment()
        if (note?.content?.isNotEmpty()!!) {
            val notesArray = notesModel.getNotesList().value?.filter { x -> x.id == note.id }
            if (notesArray != null && notesArray.isNotEmpty()) {
                val oldNote = notesArray[0]
                if (note.content != oldNote.content || note.type != oldNote.type) {
                    notesModel.update(note)
                    changedNotes.add(note)
                }
            } else {
                val noteId = notesModel.insert(note)
                noteId.observe(mainView!!.getLifecycleOwner(),
                    Observer<Long> { t ->
                        t?.let {
                            mainView!!.getNoteFragment()?.setNoteId(it)
                        }
                    })
            }
            mainView?.hideKeyboard()
        }
    }

    override fun toNotesState() {
        mainView?.removeNoteFragment()
        mainView?.toNotesState()
    }

    override fun toNoteFragmentState(note: Note) {
        mainView?.showFragmentWithNote(note)
        mainView?.toFragmentState()
    }

    override fun setNotesObserver(observer: Observer<List<Note>>) {
        notesModel.getNotesList().observe(mainView!!.getLifecycleOwner(), observer)
    }

    override fun getNoteAtPos(pos: Int): Note? = notesModel.getNoteAtPos(pos)

    override fun insertNote(note: Note) {
        notesModel.insert(note)
    }

    override fun deleteNote(note: Note) {
        notesModel.delete(note)
    }

    override fun checkCurrentIntent(intent: Intent, context: Context) {
        intent.let {
            if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                val noteId = intent.getIntExtra(AppConstants.WIDGET_NOTE_ID, -1)
                NoteGetter.getNoteById(noteId)
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

    override fun updateWidgetsWithChangedNotes(context: Context) {
        for (note in changedNotes) {
            updateWidgets(note, context)
        }
        changedNotes.clear()
    }

    override fun deleteAllNotes() {
        notesModel.deleteAll()
    }

    private fun updateWidgets(note: Note, context: Context) {
        val sp = notesModel.getSP()
        val widgetIds = SharedPrefsHelper.getWidgetIdsByNoteId(sp, note.id)
        if (widgetIds.size > 0) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            for (widgetId in widgetIds) {
                NoteWidget.updateAppWidget(context, appWidgetManager, widgetId, note)
            }
        }
    }
}