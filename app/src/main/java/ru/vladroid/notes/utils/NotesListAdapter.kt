package ru.vladroid.notes.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.vladroid.notes.R
import ru.vladroid.notes.model.DateConverter
import ru.vladroid.notes.model.Note

class NotesListAdapter(val context: Context, private val listener: OnItemClickListener): RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(note: Note)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val content = note.content
        if (content.contains("\n")) {
            holder.noteTitle.text = content.substring(0, content.indexOf("\n"))
            holder.noteDesc.text = content.substring(content.indexOf("\n") + 1)
        }
        else {
            holder.noteTitle.text = content
            holder.noteDesc.text = ""
        }
        holder.noteDate.text = DateConverter.dateFormat.format(note.editDate)
        holder.noteBackground.setOnClickListener {listener.onItemClick(note)}
        when (note.type) {
            2 -> holder.noteBackground.setCardBackgroundColor(
                getColor(
                    context,
                    R.color.colorGreenCard
                )
            )
            3 -> holder.noteBackground.setCardBackgroundColor(
                getColor(
                    context,
                    R.color.colorBlueCard
                )
            )
            4 -> holder.noteBackground.setCardBackgroundColor(
                getColor(
                    context,
                    R.color.colorRedCard
                )
            )
            5 -> holder.noteBackground.setCardBackgroundColor(
                getColor(
                    context,
                    R.color.colorPinkCard
                )
            )
            else -> holder.noteBackground.setCardBackgroundColor(
                getColor(
                    context,
                    R.color.colorDefaultCard
                )
            )
        }
    }

    inner class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteDesc: TextView = itemView.findViewById(R.id.note_desc)
        val noteDate:TextView = itemView.findViewById(R.id.note_date)
        val noteBackground: CardView = itemView.findViewById(R.id.item_card_view)
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    companion object {
        fun getColor(context: Context, id: Int): Int = ContextCompat.getColor(context, id)
    }
}