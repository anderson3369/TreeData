package com.orchardlog.treedata.ui.login

import android.os.Bundle
import android.os.CancellationSignal
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetCustomCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.orchardlog.treedata.R
import com.orchardlog.treedata.databinding.FragmentLoginBinding
import com.orchardlog.treedata.ui.data.model.UserPreferencesViewModel
import com.orchardlog.treedata.utils.RoomBackUp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.SecureRandom

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private lateinit var auth: FirebaseAuth

    companion object {
        const val TAG = "LoginFragment"
    }

    private val userPreferencesViewModel: UserPreferencesViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        val nonce = getNonce()

        binding.signInButton.visibility = View.GONE
        binding.signInButton.setOnClickListener {
            //startSignIn()
            val signInWithGoogleOption = GetSignInWithGoogleOption(
                R.string.clientServerId.toString(),
                "com.orchardlog.treedata",
                nonce
            )
            startSignIn(signInWithGoogleOption)

        }

        binding.backup.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Data Backup")
                .setMessage("Login to backup your data")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        userPreferencesViewModel.setBackup(true)
                    }

                    //Try and restore just in case
                    val uid = auth.currentUser?.uid
                    RoomBackUp.restoreDatabase(requireContext(), uid)
                }.setNegativeButton("No") { _, _ ->
                    Log.i(TAG, "No backup")
                }.show()
        }

        //binding.logout
      return binding.root

    }

    private fun getNonce(): String {
        val byteArray = ByteArray(128)
        SecureRandom().nextBytes(byteArray)
        return Base64.encodeToString(byteArray, Base64.URL_SAFE)
    }

    private fun startSignIn(googleIdOption: GetCustomCredentialOption) {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(requireContext())

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption).build()

            val cancellationSignal = CancellationSignal()
            cancellationSignal.setOnCancelListener {
                Toast.makeText(requireContext(), "Your data cannot be backed up", Toast.LENGTH_LONG).show()
            }

            try {
                val result = credentialManager.getCredential(
                    context = requireContext(),
                    request = request
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                if(e is NoCredentialException) {
                    //show signup button
                    if(binding.signInButton.visibility == View.VISIBLE) {
                        return@launch
                    } else {
                        binding.signInButton.visibility = View.VISIBLE
                        val googleId = getGoogleIdOption(false)
                        startSignIn(googleId)
                    }
                }
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential
        if(credential is PublicKeyCredential || credential is PasswordCredential) {
            return
        } else if(credential is CustomCredential) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val auth = Firebase.auth
                val token = googleIdTokenCredential.idToken
                val cred = GoogleAuthProvider.getCredential(token, null)
                auth.signInWithCredential(cred).addOnCompleteListener {
                    if(it.isSuccessful) {
                        lifecycleScope.launch {
                            userPreferencesViewModel.setFirstTime(false)
                        }
                        //Navigate to home
                        val action = LoginFragmentDirections.actionNavLoginToNavHome()
                        view?.findNavController()?.navigate(action)
                    }
                }.addOnFailureListener {
                    Log.e(TAG + " Firebase failed", it.toString())
                }

            } catch(e: Exception) {
                Log.e(TAG + " handleSignIn", e.toString())
            }
        }
    }


    private fun shouldStartSignIn(): Boolean {
        val user = Firebase.auth.currentUser
        return user == null || user.isAnonymous

    }


    override fun onStart() {
        super.onStart()

        val nonce = getNonce()
        // Start sign in if necessary
        if (shouldStartSignIn()) {
            userPreferencesViewModel.getIsFirstTime().observe(viewLifecycleOwner) {
                var googleIdOption: GetGoogleIdOption? = null
                if(it) {
                    googleIdOption = getGoogleIdOption(false)
                } else {
                    googleIdOption = getGoogleIdOption(true)
                }
                startSignIn(googleIdOption)
            }

            return
        }
    }

    private fun getGoogleIdOption(authorizedAccounts: Boolean): GetGoogleIdOption {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(authorizedAccounts)
            .setServerClientId(getString(R.string.clientServerId))
            .build()

        return googleIdOption
    }

    override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }


}