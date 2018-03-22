package com.github.charleslzq.facestore

import org.joda.time.LocalDateTime

/**
 * Created by charleslzq on 18-2-28.
 */
interface Meta {
    val id: String
    val createTime: LocalDateTime
    val updateTime: LocalDateTime
}

data class FaceData<out P : Meta, out F : Meta>(val person: P, val faces: List<F> = emptyList())

interface FaceDataType<P : Meta, F : Meta> {
    val personClass: Class<P>
    val faceClass: Class<F>
}

interface ReadOnlyFaceStore<P : Meta, F : Meta> {
    val dataType: FaceDataType<P, F>
    fun getPersonIds(): List<String> = emptyList()
    fun getFaceData(personId: String): FaceData<P, F>? = null
    fun getPerson(personId: String): P? = null
    fun getFaceIdList(personId: String): List<String> = emptyList()
    fun getFace(personId: String, faceId: String): F? = null
}

interface ReadWriteFaceStore<P : Meta, F : Meta> : ReadOnlyFaceStore<P, F> {
    fun savePerson(person: P) {}
    fun saveFace(personId: String, face: F) {}
    fun saveFaceData(faceData: FaceData<P, F>) {}
    fun deleteFaceData(personId: String) {}
    fun deleteFace(personId: String, faceId: String) {}
    fun clearFace(personId: String) {}
}

interface FaceStoreChangeListener<in P : Meta, in F : Meta> {
    fun onPersonUpdate(person: P) {}
    fun onFaceUpdate(personId: String, face: F) {}
    fun onFaceDataDelete(personId: String) {}
    fun onFaceDelete(personId: String, faceId: String) {}
    fun onPersonFaceClear(personId: String) {}
}

abstract class CompositeReadWriteFaceStore<P : Meta, F : Meta>(
        readOnlyStore: ReadOnlyFaceStore<P, F>
) : ReadOnlyFaceStore<P, F> by readOnlyStore, ReadWriteFaceStore<P, F>