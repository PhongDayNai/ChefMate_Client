package com.watb.chefmate.data

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class SafeIntAdapter : TypeAdapter<Int>() {
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Int {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                0
            }
            JsonToken.NUMBER -> {
                val number = reader.nextDouble()
                number.toInt()
            }
            JsonToken.STRING -> {
                val raw = reader.nextString()
                raw.toDoubleOrNull()?.toInt() ?: 0
            }
            else -> {
                reader.skipValue()
                0
            }
        }
    }
}
