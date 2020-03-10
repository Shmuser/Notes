package ru.vladroid.notes.screenview

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import ru.vladroid.notes.R
import ru.vladroid.notes.model.Note
import java.util.*


class ViewNoteFragment : Fragment(), View.OnClickListener {
    private val colorsArray: IntArray =
        intArrayOf(
            R.id.default_card_color,
            R.id.green_card_color,
            R.id.blue_card_color,
            R.id.red_card_color,
            R.id.pink_card_color
        )

    private fun getColorByType() =
        when (noteType) {
            2 -> R.color.colorGreenCard
            3 -> R.color.colorBlueCard
            4 -> R.color.colorRedCard
            5 -> R.color.colorPinkCard
            else -> R.color.colorDefaultCard
        }

    override fun onClick(v: View?) {
        noteType = if (v == null) 1 else colorsArray.indexOf(v.id) + 1
        content?.setBackgroundResource(getColorByType())
    }

    var innerNote: Note = Note("", 0, 0, Date(0))
    var noteType = 1
    val content by lazy {
        view?.findViewById<EditText>(R.id.edit_note_content)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onStart() {
        super.onStart()
        val argNote: Note? = arguments?.getParcelable("note")
        argNote?.let {
            innerNote.id = argNote.id
            content?.setText(argNote.content)
            noteType = argNote.type
            (content?.background as ColorDrawable).color
            content?.setBackgroundResource(getColorByType())
        }
        val defaultColorBtn = view?.findViewById<ImageView>(R.id.default_card_color)
        val greenColorBtn = view?.findViewById<ImageView>(R.id.green_card_color)
        val blueColorBtn = view?.findViewById<ImageView>(R.id.blue_card_color)
        val redColorBtn = view?.findViewById<ImageView>(R.id.red_card_color)
        val pinkColorBtn = view?.findViewById<ImageView>(R.id.pink_card_color)

        defaultColorBtn?.setOnClickListener(this)
        greenColorBtn?.setOnClickListener(this)
        blueColorBtn?.setOnClickListener(this)
        redColorBtn?.setOnClickListener(this)
        pinkColorBtn?.setOnClickListener(this)
    }

    fun getNote(): Note {
        innerNote.content = content?.text.toString()
        innerNote.type = noteType
        innerNote.editDate = Calendar.getInstance().time
        return innerNote
    }

    fun setNoteId(noteId: Long) {
        innerNote.id = noteId.toInt()
    }
}
