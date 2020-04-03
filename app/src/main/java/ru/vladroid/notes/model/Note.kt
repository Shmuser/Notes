package ru.vladroid.notes.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "notes")
class Note : Parcelable{

    constructor()

    constructor(content: String, type: Int, id: Int, date: Date) {
        this.content = content
        this.type = type
        this.id = id
        this.editDate = date
    }

    var content: String = ""
    var type: Int = 1
    var editDate: Date = Date(0)

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong())
    )


    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (dest != null) {
            dest.writeString(content)
            dest.writeInt(type)
            dest.writeInt(id)
            dest.writeLong(editDate.time)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}

class DateConverter {
    companion object {
        val dateFormat: DateFormat = SimpleDateFormat.getDateTimeInstance()
    }

    @TypeConverter
    fun toDate(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun fromDate(value: Date): Long {
        return value.time
    }
}