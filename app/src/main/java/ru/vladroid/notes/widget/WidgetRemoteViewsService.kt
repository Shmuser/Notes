package ru.vladroid.notes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ru.vladroid.notes.R


class WidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetFactory(applicationContext, intent)
    }

    class WidgetFactory : RemoteViewsService.RemoteViewsFactory {
        private var noteContent: String
        private val context: Context
        private val widgetId: Int
        private val noteId: Int

        constructor(context: Context, intent: Intent?) {
            noteContent = intent!!.getStringExtra(NoteWidget.WIDGET_NOTE_CONTENT)
            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
            noteId = intent.getIntExtra(NoteWidget.WIDGET_NOTE_ID, -1)
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
            val updateIntent = Intent()
            updateIntent.putExtra(NoteWidget.WIDGET_NOTE_ID, noteId)
            view.setOnClickFillInIntent(R.id.widget_note_content, updateIntent)
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
