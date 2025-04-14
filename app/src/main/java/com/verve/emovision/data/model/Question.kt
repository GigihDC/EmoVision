package com.verve.emovision.data.model

import android.os.Parcel
import android.os.Parcelable

data class Question(
    val id: Int,
    val question: Int,
    val options: List<Int>,
    val correctAnswer: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        mutableListOf<Int>().apply {
            parcel.readList(this, Int::class.java.classLoader)
        },
        parcel.readInt(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeInt(id)
        parcel.writeInt(question)
        parcel.writeList(options)
        parcel.writeInt(correctAnswer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Question> {
        override fun createFromParcel(parcel: Parcel): Question {
            return Question(parcel)
        }

        override fun newArray(size: Int): Array<Question?> {
            return arrayOfNulls(size)
        }
    }
}
