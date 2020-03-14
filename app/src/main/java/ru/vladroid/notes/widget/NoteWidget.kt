package ru.vladroid.notes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.RemoteViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.vladroid.notes.R
import ru.vladroid.notes.model.DateConverter
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.screenview.MainActivity
import ru.vladroid.notes.utils.NoteGetter
import ru.vladroid.notes.utils.NotesListAdapter
import ru.vladroid.notes.utils.SharedPrefsHelper


class NoteWidget : AppWidgetProvider() {
    companion object {
        const val WIDGET_NOTE_ID = "ru_vladroid_notes_widget_note_id_"
        const val WIDGET_PREF = "ru_vladroid_notes_widget_shared_prefs"
        const val WIDGET_ID_BY_NOTE_ID_AND_COUNT =
            "ru_vladroid_notes_widget_id_by_note_id_and_count_"
        const val WIDGETS_COUNT_BY_NOTE_ID = "ru_vladroid_widgets_count_by_note_id_"
        const val WIDGET_NOTE_CONTENT = "ru_vladroid_widget_note_content"
        private val disposables = HashMap<Int, Disposable>()

        fun updateAppWidget(
            context: Context?,
            appWidgetManager: AppWidgetManager?,
            sp: SharedPreferences,
            appWidgetId: Int,
            note: Note
        ) {
            val views = RemoteViews(context?.packageName, R.layout.widget_layout)
            views.setTextViewText(
                R.id.note_date,
                DateConverter.dateFormat.format(note.editDate)
            )
            val color: Int
            when (note.type) {
                2 -> color = NotesListAdapter.getColor(context!!, R.color.colorGreenCard)
                3 -> color = NotesListAdapter.getColor(context!!, R.color.colorBlueCard)
                4 -> color = NotesListAdapter.getColor(context!!, R.color.colorRedCard)
                5 -> color = NotesListAdapter.getColor(context!!, R.color.colorPinkCard)
                else -> color = NotesListAdapter.getColor(context!!, R.color.colorDefaultCard)
            }
            views.setInt(R.id.widget_background, "setBackgroundColor", color)
            setNote(views, context, note.content, note.id, appWidgetId)
            val pIntent = constructOnClickPendingIntent(context, appWidgetId, note.id)
            views.setOnClickPendingIntent(R.id.widget_background, pIntent)
            appWidgetManager!!.updateAppWidget(appWidgetId, views)
        }

        private fun setNote(
            remoteViews: RemoteViews,
            context: Context?,
            content: String,
            noteId: Int,
            appWidgetId: Int
        ) {
            val adapter = Intent(context, WidgetRemoteViewsService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            adapter.putExtra(WIDGET_NOTE_CONTENT, content)
            adapter.putExtra(WIDGET_NOTE_ID, noteId)
            adapter.data = Uri.parse("$appWidgetId $content")
            remoteViews.setRemoteAdapter(R.id.scroll_container, adapter)
            val updateIntent = Intent(context, MainActivity::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            remoteViews.setPendingIntentTemplate(
                R.id.scroll_container,
                PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    updateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }

        private fun constructOnClickPendingIntent(
            context: Context?,
            appWidgetId: Int,
            noteId: Int
        ): PendingIntent {
            val updateIntent = Intent(context, MainActivity::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            updateIntent.putExtra(WIDGET_NOTE_ID, noteId)
            updateIntent.data = Uri.parse(noteId.toString())
            return PendingIntent.getActivity(context, appWidgetId, updateIntent, 0)
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val sp = context!!.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        for (appWidgetId in appWidgetIds!!) {
            val id = sp.getInt(WIDGET_NOTE_ID + appWidgetId, -1)
            if (id == -1)
                continue
            if (disposables.containsKey(appWidgetId)) {
                disposables[appWidgetId]?.dispose()
                disposables.remove(appWidgetId)
            }
            //TODO нужно ли тут всё так?
            disposables[appWidgetId] = NoteGetter.getNoteById(context, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { updateAppWidget(context, appWidgetManager, sp, appWidgetId, it) },
                    { })
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        val sp = context!!.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        for (widgetID in appWidgetIds!!) {
            SharedPrefsHelper.delete(sp, widgetID)
            disposables[widgetID]?.dispose()
            disposables.remove(widgetID)
        }
    }
}