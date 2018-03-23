package com.github.charleslzq.facestore

import org.joda.time.LocalDateTime
import rx.Observable

/**
 * Created by charleslzq on 18-2-28.
 */
fun <T> createObservableFromNullable(data: T?): Observable<T> = Observable.create {
    data?.run { it.onNext(this) }
    it.onCompleted()
}

fun <T> createObservableFromNullable(generator: () -> T?): Observable<T> = Observable.create {
    generator()?.run { it.onNext(this) }
    it.onCompleted()
}

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
    fun getPersonIdsAsObservable(): Observable<String> = Observable.from(getPersonIds())
    fun getFaceData(personId: String): FaceData<P, F>? = null
    fun getFaceDataAsObservable(personId: String): Observable<FaceData<P, F>> = createObservableFromNullable(getFaceData(personId))
    fun getPerson(personId: String): P? = null
    fun getPersonAsObservable(personId: String): Observable<P> = createObservableFromNullable(getPerson(personId))
    fun getFaceIdList(personId: String): List<String> = emptyList()
    fun getFaceIdListAsObservable(personId: String): Observable<String> = Observable.from(getFaceIdList(personId))
    fun getFace(personId: String, faceId: String): F? = null
    fun getFaceAsObservable(personId: String, faceId: String): Observable<F> = createObservableFromNullable(getFace(personId, faceId))
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