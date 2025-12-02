package com.example.firebase_lsm_pp.services

import com.example.firebase_lsm_pp.models.Lesson
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreLessonService {
    
    private val db = Firebase.firestore
    private val lessons = db.collection("lessons")
    
    suspend fun getAllLessons(): List<Lesson> {
        return try {
            val snapshot = lessons.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Lesson::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getLessonById(lessonId: String): Lesson? {
        return try {
            lessons.document(lessonId).get().await().toObject(Lesson::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

