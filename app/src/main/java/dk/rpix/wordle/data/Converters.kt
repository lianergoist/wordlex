package dk.rpix.wordle.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listStringAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
    private val listIntAdapter = moshi.adapter<List<Int>>(Types.newParameterizedType(List::class.java, Int::class.javaObjectType))

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return listStringAdapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { listStringAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return listIntAdapter.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let { listIntAdapter.fromJson(it) }
    }
}
