package ru.vladroid.notes.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import io.reactivex.disposables.Disposable
import ru.vladroid.notes.R
import ru.vladroid.notes.model.DateConverter
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.utils.NoteGetter
import ru.vladroid.notes.utils.NotesListAdapter


class NoteWidget : AppWidgetProvider() {
    companion object {
        const val WIDGET_NOTE_ID = "ru_vladroid_notes_widget_note_id_"
        const val WIDGET_PREF = "ru_vladroid_notes_widget_shared_prefs"
        private val disposables = HashMap<Int, Disposable>()

        fun updateAppWidget(
            context: Context?,
            appWidgetManager: AppWidgetManager?,
            sp: SharedPreferences,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context?.packageName, R.layout.widget_layout)
            val id = sp.getInt(WIDGET_NOTE_ID + appWidgetId, -1)
            if (id == -1)
                return
            setNote(views, context, id, appWidgetId)
            disposables[appWidgetId] = NoteGetter.getNoteById(context!!, id).subscribe { t: Note? ->
                t?.let {
                    views.setTextViewText(
                        R.id.note_date,
                        DateConverter.dateFormat.format(t.editDate)
                    )
                    val color: Int
                    when (t.type) {
                        2 -> color = NotesListAdapter.getColor(context, R.color.colorGreenCard)
                        3 -> color = NotesListAdapter.getColor(context, R.color.colorBlueCard)
                        4 -> color = NotesListAdapter.getColor(context, R.color.colorRedCard)
                        5 -> color = NotesListAdapter.getColor(context, R.color.colorPinkCard)
                        else -> color = NotesListAdapter.getColor(context, R.color.colorDefaultCard)
                    }
                    views.setInt(R.id.widget_background, "setBackgroundColor", color)
                    appWidgetManager!!.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun setNote(
            remoteViews: RemoteViews,
            context: Context?,
            id: Int,
            appWidgetId: Int
        ) {
            val adapter = Intent(context, WidgetRemoteViewsService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            adapter.putExtra(WIDGET_NOTE_ID, id)
            remoteViews.setRemoteAdapter(R.id.scroll_container, adapter)
        }
    }


    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val sp = context!!.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        for (appWidgetId in appWidgetIds!!) {
            updateAppWidget(context, appWidgetManager, sp, appWidgetId)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        val editor = context!!.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds!!) {
            editor.remove(WIDGET_NOTE_ID + widgetID)
            disposables[widgetID]?.dispose()
            disposables.remove(widgetID)
        }
        editor.commit()
    }
}