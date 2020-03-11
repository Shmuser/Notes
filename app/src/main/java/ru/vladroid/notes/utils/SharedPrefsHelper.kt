package ru.vladroid.notes.utils

import android.content.SharedPreferences
import ru.vladroid.notes.widget.NoteWidget

class SharedPrefsHelper {
    companion object {
        fun create(sp: SharedPreferences, widgetId: Int, noteId: Int) {
            val editor = sp.edit()
            editor.putInt(NoteWidget.WIDGET_NOTE_ID + widgetId, noteId)
            val widgetsWithSameNote = sp.getInt(NoteWidget.WIDGETS_COUNT_BY_NOTE_ID + noteId, 0) + 1
            editor.putInt(
                NoteWidget.WIDGET_ID_BY_NOTE_ID_AND_COUNT + noteId + "_" + widgetsWithSameNote,
                widgetId
            )
            editor.putInt(NoteWidget.WIDGETS_COUNT_BY_NOTE_ID + noteId, widgetsWithSameNote)
            editor.commit()
        }

        fun delete(sp: SharedPreferences, widgetId: Int) {
            val noteId = sp.getInt(NoteWidget.WIDGET_NOTE_ID + widgetId, 0)
            val widgetsNewCount = sp.getInt(NoteWidget.WIDGETS_COUNT_BY_NOTE_ID + noteId, 0) - 1
            val editor = sp.edit()
            if (widgetsNewCount > 0) {
                val widgets = getWidgetIdsByNoteId(sp, noteId)
                widgets.remove(widgetId)
                for (i in 1..widgetsNewCount) {
                    editor.putInt(
                        NoteWidget.WIDGET_ID_BY_NOTE_ID_AND_COUNT + noteId + "_" + i,
                        widgets[i - 1]
                    )
                }
                editor.remove(NoteWidget.WIDGET_ID_BY_NOTE_ID_AND_COUNT + noteId + "_" + (widgetsNewCount + 1).toString())
            } else {
                editor.remove(NoteWidget.WIDGETS_COUNT_BY_NOTE_ID + noteId)
                editor.remove(NoteWidget.WIDGET_NOTE_ID + widgetId)
                editor.remove(NoteWidget.WIDGET_ID_BY_NOTE_ID_AND_COUNT + noteId + "_" + 1.toString())
            }
            editor.commit()
        }

        fun getWidgetIdsByNoteId(sp: SharedPreferences, noteId: Int): MutableList<Int> {
            val widgetsId = mutableListOf<Int>()
            val widgetsCount = sp.getInt(NoteWidget.WIDGETS_COUNT_BY_NOTE_ID + noteId, 0)
            for (i in 1..widgetsCount) {
                widgetsId.add(
                    sp.getInt(
                        NoteWidget.WIDGET_ID_BY_NOTE_ID_AND_COUNT + noteId + "_" + i,
                        0
                    )
                )
            }
            return widgetsId
        }
    }
}