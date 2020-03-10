package ru.vladroid.notes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import io.reactivex.disposables.Disposable
import ru.vladroid.notes.R
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NoteDao
import ru.vladroid.notes.model.NotesDatabase
import ru.vladroid.notes.utils.NoteGetter


class WidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetFactory(applicationContext, intent)
    }

    class WidgetFactory : RemoteViewsService.RemoteViewsFactory {
        private val noteDao: NoteDao
        private lateinit var note: Note
        private val context: Context
        private val widgetId: Int
        private val noteId: Int
        private lateinit var observer: Disposable

        constructor(context: Context, intent: Intent?) {
            val db = NotesDatabase.getInstance(context)
            noteDao = db.notesDao()
            widgetId = intent!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            noteId = intent.getIntExtra(NoteWidget.WIDGET_NOTE_ID, -1)
            this.context = context
        }

        override fun onCreate() {
            observer = NoteGetter.getNoteById(context, noteId).subscribe { t ->
                note = t
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.scroll_container)
            }
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.widget_item)
            view.setTextViewText(R.id.widget_note_content, note.content)
            return view
        }

        override fun getCount() = if (note == null) 0 else 1


        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {
            observer.dispose()
        }
    }
}
