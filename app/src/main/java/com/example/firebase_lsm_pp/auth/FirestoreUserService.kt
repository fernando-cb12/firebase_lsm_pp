package com.example.firebase_lsm_pp.auth

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

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

    /**
     * Updates the user's streak based on their last login date.
     * - If lastLogin is null or doesn't exist: set streak to 1
     * - If lastLogin was yesterday: increment streak by 1
     * - If lastLogin was today: don't change streak (already logged in today)
     * - If lastLogin was more than 1 day ago: reset streak to 1
     */
    suspend fun updateStreak(uid: String) {
        val user = getUser(uid) ?: return
        
        val today = getStartOfDay(System.currentTimeMillis())
        val lastLogin = user.lastLogin?.let { getStartOfDay(it) }
        
        val newStreak: Int
        val shouldUpdateLastLogin: Boolean
        
        when {
            lastLogin == null -> {
                // First login - start streak at 1
                newStreak = 1
                shouldUpdateLastLogin = true
            }
            lastLogin == today -> {
                // Already logged in today - don't change anything
                return
            }
            lastLogin == today - 86400000 -> {
                // Logged in yesterday - increment streak
                newStreak = user.streak + 1
                shouldUpdateLastLogin = true
            }
            else -> {
                // More than 1 day ago - reset streak to 1
                newStreak = 1
                shouldUpdateLastLogin = true
            }
        }
        
        if (shouldUpdateLastLogin) {
            updateUser(uid, mapOf(
                "streak" to newStreak,
                "lastLogin" to System.currentTimeMillis()
            ))
        }
    }
    
    /**
     * Gets the start of the day (00:00:00) for a given timestamp
     */
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Gets top users sorted by points (descending order)
     * @param limit Maximum number of users to return (default: 20)
     */
    suspend fun getTopUsers(limit: Int = 20): List<AppUser> {
        return try {
            val snapshot = users
                .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(AppUser::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
