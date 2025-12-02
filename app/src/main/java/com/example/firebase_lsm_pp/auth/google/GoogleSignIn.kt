package com.example.firebase_lsm_pp.auth.google

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.example.firebase_lsm_pp.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class GoogleSignIn(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "GoogleSignIn"
    }

    /**
     * Inicia el flujo de login con Google.
     * @param onResult Callback que devuelve FirebaseUser si tuvo éxito, o null si falló
     */
    fun signIn(onResult: (FirebaseUser?) -> Unit) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val user = handleSignIn(result.credential)
                onResult(user)

            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error al obtener credenciales: ${e.localizedMessage}", e)
                onResult(null)
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado durante el login con Google: ${e.localizedMessage}", e)
                onResult(null)
            }
        }
    }

    private suspend fun handleSignIn(credential: Credential): FirebaseUser? {
        return if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleCredential.idToken)
        } else {
            Log.w(TAG, "La credencial no es de tipo Google ID")
            null
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String?): FirebaseUser? {
        if (idToken == null) {
            Log.w(TAG, "ID token es null")
            return null
        }

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            Log.e(TAG, "Error al autenticar con Firebase: ${e.localizedMessage}", e)
            null
        }
    }


    fun signOut(onComplete: (() -> Unit)? = null) {
        auth.signOut()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "No se pudo limpiar las credenciales: ${e.localizedMessage}")
            } finally {
                onComplete?.invoke()
            }
        }
    }
}