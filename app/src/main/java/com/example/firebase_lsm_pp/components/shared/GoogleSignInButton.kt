package com.example.firebase_lsm_pp.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.firebase_lsm_pp.R
import com.example.firebase_lsm_pp.auth.google.GoogleSignIn
import com.google.firebase.auth.FirebaseUser

@Composable
fun GoogleSignInButton(
    text: String = "Continuar con Google",
    onResult: (FirebaseUser?) -> Unit
) {
    val context = LocalContext.current
    val googleSignIn = remember { GoogleSignIn(context) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .clickable {
                googleSignIn.signIn { user ->
                    onResult(user)
                }
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Sign-In",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                color = Color.Black
            )
        }
    }
}
