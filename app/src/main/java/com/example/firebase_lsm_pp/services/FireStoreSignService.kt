package com.example.firebase_lsm_pp.services

import com.example.firebase_lsm_pp.models.Sign
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class FirestoreSignService {

    private val db = Firebase.firestore
    private val dictionary = db.collection("dictionary")

    suspend fun getAllSigns(): List<Sign> {
        return try {
            val snapshot = dictionary.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Sign::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSignsByCategory(category: String): List<Sign> {
        return try {
            val snapshot = if (category == "Todas") {
                dictionary.get().await()
            } else {
                dictionary.whereEqualTo("category", category).get().await()
            }
            snapshot.documents.mapNotNull { document ->
                document.toObject(Sign::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchSigns(query: String): List<Sign> {
        return try {
            val snapshot = dictionary.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(Sign::class.java)
            }.filter { sign ->
                sign.word.contains(query, ignoreCase = true) ||
                        sign.description.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

