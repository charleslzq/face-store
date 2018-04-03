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

interface ReadOnlyFaceStore<P : Meta, F : Meta> {
    val personClass: Class<P>
    val faceClass: Class<F>
    fun getPersonIds(): List<String> = emptyList()
    fun getPersonIdsAsObservable(): Observable<String> = Observable.from(getPersonIds())
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
    fun deletePerson(personId: String) {}
    fun deleteFace(personId: String, faceId: String) {}
}

interface FaceStoreChangeListener<in P : Meta, in F : Meta> {
    fun onPersonUpdate(person: P) {}
    fun onFaceUpdate(personId: String, face: F) {}
    fun onPersonDelete(personId: String) {}
    fun onFaceDelete(personId: String, faceId: String) {}
}

interface ListenableReadWriteFaceStore<P : Meta, F : Meta> : ReadWriteFaceStore<P, F> {
    val listeners: MutableList<FaceStoreChangeListener<P, F>>
}

abstract class CompositeReadWriteFaceStore<P : Meta, F : Meta>(
        readOnlyStore: ReadOnlyFaceStore<P, F>
) : ReadOnlyFaceStore<P, F> by readOnlyStore, ReadWriteFaceStore<P, F>