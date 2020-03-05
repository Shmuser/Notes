package ru.vladroid.notes

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import ru.vladroid.notes.model.Note
import java.util.*


class ViewNoteFragment: Fragment(), View.OnClickListener {
    private val colorsArray: IntArray =
        intArrayOf(R.id.default_card_color, R.id.green_card_color,
            R.id.blue_card_color, R.id.red_card_color, R.id.pink_card_color)

    fun getColorByType() =
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

    var innerNote: Note? = null
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

        innerNote = arguments?.getParcelable("note")
        Log.d("note?!", "start...")
        Log.d("note?!", (innerNote == null).toString())
        innerNote?.let {
            content?.setText(innerNote?.content)
            noteType = innerNote!!.type
            (content?.background as ColorDrawable).color
            content?.setBackgroundResource(getColorByType())
            // noteText.setBackgroundResource()
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
        Log.d("new_note", content?.text.toString() + " !")
        innerNote?.content = content?.text.toString()
        innerNote?.type = noteType
        innerNote?.editDate = Calendar.getInstance().time
        return innerNote!!
    }

    fun setNoteId(noteId: Long) {
        innerNote?.id = noteId.toInt()
    }
}
