package com.example.firebase_lsm_pp.auth

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreUserService {

    private val db = Firebase.firestore
    private val users = db.collection("users")

    suspend fun createUser(user: AppUser) {
        users.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): AppUser? {
        return users.document(uid).get().await().toObject(AppUser::class.java)
    }

    suspend fun updateUser(uid: String, data: Map<String, Any>) {
        users.document(uid).update(data).await()
    }
}
