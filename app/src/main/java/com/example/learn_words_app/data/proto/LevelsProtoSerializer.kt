package com.example.learn_words_app.data.proto

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.app.proto.LevelsProto
import com.example.learn_words_app.data.dataBase.Levels
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object LevelsProtoSerializer : Serializer<LevelsProto> {
    override val defaultValue: LevelsProto = LevelsProto.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): LevelsProto {
        try {
            return LevelsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LevelsProto, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.levelsParamsDataStore: DataStore<LevelsProto> by dataStore(
    fileName = "user_params.pb",
    serializer = LevelsProtoSerializer
)

//Функция для конвертации ProtoLevel в Levels
fun convertLevelsProtoToLevels(levelsProto: LevelsProto): Levels {
    return Levels(
        id = levelsProto.id,
        name = levelsProto.name,
        countLearnedWords = 0
    )
}

//Функция для конвертации Levels в ProtLevels
fun convertProtoLevelsToLevels(levels: List<Levels>): MutableList<LevelsProto> {
    val listOfLevelsBuilders = mutableListOf<LevelsProto>()

    //Проходим по list Levels и заполняем List LevelsProto
    levels.forEach { level ->
        if (level.id != null) {
            listOfLevelsBuilders.add(
                LevelsProto.newBuilder().setId(level.id).setName(level.name).build()
            )
        } else {
            Log.e("Check user data. get levels from db", "level id is null")
            throw Exception()
        }
    }
    return listOfLevelsBuilders
}