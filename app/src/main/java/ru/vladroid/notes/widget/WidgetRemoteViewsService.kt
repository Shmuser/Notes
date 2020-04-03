package ru.vladroid.notes.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ru.vladroid.notes.R
import ru.vladroid.notes.utils.AppConstants


class WidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetFactory(applicationContext, intent)
    }

    class WidgetFactory(private val context: Context, intent: Intent?) : RemoteViewsFactory {
        private var noteContent: String = intent!!.getStringExtra(AppConstants.WIDGET_NOTE_CONTENT)
        private val noteId: Int = intent!!.getIntExtra(AppConstants.WIDGET_NOTE_ID, -1)

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
            updateIntent.putExtra(AppConstants.WIDGET_NOTE_ID, noteId)
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
