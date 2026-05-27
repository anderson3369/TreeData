package com.orchardlog.treedata.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.OAuthProvider
import com.orchardlog.treedata.R
import com.orchardlog.treedata.shared.auth.AuthService
import com.orchardlog.treedata.shared.auth.AuthState
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import com.orchardlog.treedata.ui.theme.TreeDataTheme
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {

    companion object {
        const val TAG = "LoginFragment"
        // Web client ID from google-services.json (client_type: 3)
        const val WEB_CLIENT_ID = "781169082005-kjbuigu7dk12l4herln1j9cvib0tpvq3.apps.googleusercontent.com"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TreeDataTheme {
                    LoginScreen(
                        onSignedIn = {
                            findNavController().navigate(R.id.action_nav_login_to_nav_orchardTask)
                        },
                        fragment = this@LoginFragment
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    fragment: Fragment
) {
    val authState by AuthService.authState.collectAsStateWithLifecycle(
        initialValue = AuthState()
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // If already signed in, show account info
    if (authState.isSignedIn) {
        SignedInView(
            authState = authState,
            onSignOut = {
                scope.launch {
                    AuthService.signOut()
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Sign in to sync your orchard data across devices",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        } else {
            // Google Sign-In Button
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(fragment.requireActivity())

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(LoginFragment.WEB_CLIENT_ID)
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                context = fragment.requireActivity(),
                                request = request
                            )

                            handleSignInResult(result, onSignedIn, context)
                        } catch (e: Exception) {
                            Log.e(LoginFragment.TAG, "Google sign-in failed", e)
                            val message = when (e) {
                                is androidx.credentials.exceptions.NoCredentialException -> 
                                    "No Google accounts found. Please ensure you are signed into Google on this device."
                                is androidx.credentials.exceptions.GetCredentialCancellationException ->
                                    "Sign-in cancelled."
                                is androidx.credentials.exceptions.GetCredentialCustomException ->
                                    "Configuration error: ${e.message}. Check your Google Cloud console settings."
                                else -> "Sign-in failed: ${e.localizedMessage ?: "Unknown error"}"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Sign in with Google",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apple Sign-In Button
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val provider = OAuthProvider.newBuilder("apple.com")
                            provider.scopes = listOf("email", "name")
                            
                            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            val pending = firebaseAuth.pendingAuthResult
                            
                            val result = if (pending != null) {
                                pending.await()
                            } else {
                                firebaseAuth.startActivityForSignInWithProvider(fragment.requireActivity(), provider.build()).await()
                            }
                            
                            if (result.user != null) {
                                // Delay slightly to ensure Firebase Auth state is recognized by Firestore
                                kotlinx.coroutines.delay(500)
                                // Push all existing local data to Firestore on first sign-in
                                FirestoreSync.pushAllLocalData(ViewModelProvider.getDb())
                                Toast.makeText(context, "Signed in with Apple", Toast.LENGTH_SHORT).show()
                                onSignedIn()
                            }
                        } catch (e: Exception) {
                            Log.e(LoginFragment.TAG, "Apple sign-in failed", e)
                            Toast.makeText(context, "Apple sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Sign in with Apple",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SignedInView(
    authState: AuthState,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Signed In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (authState.displayName.isNotEmpty()) {
            Text(
                text = authState.displayName,
                fontSize = 18.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (authState.email.isNotEmpty()) {
            Text(
                text = authState.email,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEEEEEE))
        ) {
            Text("Sign Out", color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}

private suspend fun handleSignInResult(
    result: GetCredentialResponse,
    onSignedIn: () -> Unit,
    context: android.content.Context
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        AuthService.signInWithGoogleCredential(idToken)
        // Delay slightly to ensure Firebase Auth state is recognized by Firestore
        kotlinx.coroutines.delay(500)
        // Push all existing local data to Firestore on first sign-in
        FirestoreSync.pushAllLocalData(ViewModelProvider.getDb())
        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
        onSignedIn()
    } else {
        Log.w(LoginFragment.TAG, "Unexpected credential type: ${credential.type}")
    }
}
