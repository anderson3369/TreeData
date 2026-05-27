package com.orchardlog.treedata.shared.auth

import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isSignedIn: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val uid: String = ""
)

object AuthService {
    private val auth = Firebase.auth
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _authState = MutableStateFlow(buildAuthState(auth.currentUser))
    val authState: CommonStateFlow<AuthState> = _authState.asCommonStateFlow()

    val isSignedIn: Boolean get() = auth.currentUser != null
    val currentUid: String? get() = auth.currentUser?.uid

    init {
        scope.launch {
            auth.authStateChanged.collect { user ->
                _authState.value = buildAuthState(user)
            }
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Boolean {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val result = auth.signInWithCredential(credential)
        return result.user != null
    }

    suspend fun signInWithAppleCredential(idToken: String, nonce: String): Boolean {
        val credential = OAuthProvider.credential("apple.com", idToken, rawNonce = nonce)
        val result = auth.signInWithCredential(credential)
        return result.user != null
    }

    suspend fun signOut() {
        auth.signOut()
    }

    private fun buildAuthState(user: FirebaseUser?): AuthState {
        return AuthState(
            isSignedIn = user != null,
            displayName = user?.displayName ?: "",
            email = user?.email ?: "",
            uid = user?.uid ?: ""
        )
    }
}
