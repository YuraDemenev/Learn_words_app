package com.example.learn_words_app.data.proto

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.app.proto.UserProto
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserProtoSerializer : Serializer<UserProto> {
    override val defaultValue: UserProto = UserProto.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): UserProto {
        try {
            return UserProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserProto, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.userParamsDataStore: DataStore<UserProto> by dataStore(
    fileName = "user_params.pb",
    serializer = UserProtoSerializer
)