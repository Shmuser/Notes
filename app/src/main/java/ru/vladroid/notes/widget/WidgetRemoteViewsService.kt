package ru.vladroid.notes.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ru.vladroid.notes.R
import ru.vladroid.notes.model.NoteDao
import ru.vladroid.notes.model.NotesDatabase


class WidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetFactory(applicationContext, intent)
    }

    class WidgetFactory : RemoteViewsService.RemoteViewsFactory {
        private val noteDao: NoteDao
        private var noteContent: String
        private val context: Context

        constructor(context: Context, intent: Intent?) {
            val db = NotesDatabase.getInstance(context)
            noteDao = db.notesDao()
            noteContent = intent!!.getStringExtra(NoteWidget.WIDGET_NOTE_CONTENT)
            this.context = context
        }

        override fun onCreate() {
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
            view.setTextViewText(R.id.widget_note_content, noteContent)
            return view
        }

        override fun getCount() = 1

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {
        }
    }
}
