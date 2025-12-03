package com.example.firebase_lsm_pp.auth

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
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
     * Converts a Firestore document to AppUser, handling Timestamp to Long conversion
     */
    private fun documentToAppUser(document: com.google.firebase.firestore.QueryDocumentSnapshot): AppUser? {
        return try {
            val data = document.data
            AppUser(
                uid = data["uid"] as? String ?: document.id,
                name = data["name"] as? String ?: "",
                username = data["username"] as? String ?: "",
                points = (data["points"] as? Number)?.toInt() ?: 0,
                streak = (data["streak"] as? Number)?.toInt() ?: 0,
                lastLogin = when (val lastLogin = data["lastLogin"]) {
                    is com.google.firebase.Timestamp -> lastLogin.toDate().time
                    is Long -> lastLogin
                    is Number -> lastLogin.toLong()
                    else -> null
                },
                createdAt = when (val createdAt = data["createdAt"]) {
                    is com.google.firebase.Timestamp -> createdAt.toDate().time
                    is Long -> createdAt
                    is Number -> createdAt.toLong()
                    else -> System.currentTimeMillis()
                }
            )
        } catch (e: Exception) {
            Log.e("FirestoreUserService", "Error converting document to AppUser: ${e.message}", e)
            null
        }
    }

    /**
     * Gets top users sorted by points (descending order)
     * @param limit Maximum number of users to return (default: 20)
     */
    suspend fun getTopUsers(limit: Int = 20): List<AppUser> {
        return try {
            Log.d("FirestoreUserService", "Fetching top users with limit: $limit")
            val snapshot = users
                .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val userList = snapshot.documents.mapNotNull { document ->
                documentToAppUser(document as QueryDocumentSnapshot)
            }
            Log.d("FirestoreUserService", "Successfully fetched ${userList.size} users")
            userList
        } catch (e: Exception) {
            Log.e("FirestoreUserService", "Error fetching top users: ${e.message}", e)
            // If orderBy fails (e.g., missing index), try without ordering
            try {
                Log.d("FirestoreUserService", "Trying to fetch users without ordering...")
                val snapshot = users
                    .limit(limit.toLong())
                    .get()
                    .await()
                
                val userList = snapshot.documents.mapNotNull { document ->
                    documentToAppUser(document as QueryDocumentSnapshot)
                }.sortedByDescending { it.points }
                
                Log.d("FirestoreUserService", "Fetched ${userList.size} users without ordering")
                userList
            } catch (e2: Exception) {
                Log.e("FirestoreUserService", "Error fetching users without ordering: ${e2.message}", e2)
                emptyList()
            }
        }
    }
}
