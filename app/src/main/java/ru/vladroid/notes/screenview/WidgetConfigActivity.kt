package ru.vladroid.notes.screenview

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.vladroid.notes.R
import ru.vladroid.notes.model.Note
import ru.vladroid.notes.model.NotesViewModel
import ru.vladroid.notes.utils.NotesListAdapter
import ru.vladroid.notes.utils.SharedPrefsHelper
import ru.vladroid.notes.widget.NoteWidget


class WidgetConfigActivity : AppCompatActivity() {

    private val viewModel by lazy {
        NotesViewModel(application)
    }

    var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var resultIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        extras?.let {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_CANCELED, resultIntent)

        setContentView(R.layout.activity_widget_config)
        window.decorView.setBackgroundColor(Color.LTGRAY)

        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view)
        val adapter = NotesListAdapter(this, object : NotesListAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                onNoteClick(note)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.notes.observe(this, Observer {
            adapter.setNotes(it)
        })

    }

    private fun onNoteClick(note: Note) {
        val sp = getSharedPreferences(NoteWidget.WIDGET_PREF, Context.MODE_PRIVATE)
        SharedPrefsHelper.create(sp, widgetId, note.id)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        NoteWidget.updateAppWidget(this, appWidgetManager, sp, widgetId, note)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}