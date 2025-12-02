package com.example.firebase_lsm_pp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userService: FirestoreUserService = FirestoreUserService()
) {

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun createUserWithEmail(uid: String, name: String, username: String) {
        userService.createUser(
            AppUser(uid = uid, name = name, username = username)
        )
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user
    }

    suspend fun isUserProfileComplete(uid: String): Boolean {
        val user = userService.getUser(uid)
        return user != null
    }

    suspend fun createUserWithGoogle(uid: String, name: String, username: String) {
        val user = AppUser(
            uid = uid,
            name = name,
            username = username
        )
        userService.createUser(user)
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
