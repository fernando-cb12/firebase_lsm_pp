package com.example.firebase_lsm_pp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var needsUsername by mutableStateOf(false)
        private set

    var googleUserName by mutableStateOf("")

    var googleUid by mutableStateOf("")

    suspend fun handleGoogleLogin(user: FirebaseUser): Boolean {
        googleUid = user.uid
        googleUserName = user.displayName ?: ""

        val profileExists = repo.isUserProfileComplete(user.uid)

        needsUsername = !profileExists

        return true
    }

    suspend fun saveGoogleUsername(username: String) {
        repo.createUserWithGoogle(
            uid = googleUid,
            name = googleUserName,
            username = username
        )
    }

    suspend fun registerWithEmail(
        name: String,
        username: String,
        email: String,
        password: String
    ): Boolean {
        return try {
            val result = repo.registerWithEmail(email, password)
            val uid = result?.uid ?: return false

            repo.createUserWithEmail(
                uid = uid,
                name = name,
                username = username
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            repo.login(email, password)
            true
        } catch (e: Exception) {
            false
        }
    }

}
