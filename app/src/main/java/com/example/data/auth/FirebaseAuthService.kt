package com.example.data.auth

import android.content.Context
import com.example.data.model.FirebaseAuthUserInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthService(private val context: Context) {

    private var firebaseAuth: FirebaseAuth? = null
    private val _currentUserState = MutableStateFlow<FirebaseAuthUserInfo?>(null)
    val currentUserState: StateFlow<FirebaseAuthUserInfo?> = _currentUserState.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyDiscordHubGamingSecretKeyFallback00")
                    .setProjectId("discord-hub-project")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            setupAuthListener()
        } catch (e: Exception) {
            // Log or handle initial fallback
            e.printStackTrace()
        }
    }

    private fun setupAuthListener() {
        authListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                _currentUserState.value = mapFirebaseUser(user)
            } else {
                _currentUserState.value = null
            }
        }
        firebaseAuth?.addAuthStateListener(authListener!!)
    }

    private fun mapFirebaseUser(user: FirebaseUser): FirebaseAuthUserInfo {
        return FirebaseAuthUserInfo(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName ?: if (user.isAnonymous) "Guest Gamer" else user.email?.substringBefore("@"),
            isAnonymous = user.isAnonymous,
            creationTimestamp = user.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastSignInTimestamp = user.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseAuthUserInfo> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val info = mapFirebaseUser(user)
                        _currentUserState.value = info
                        cont.resume(Result.success(info))
                    } else {
                        cont.resume(Result.failure(Exception("No user found")))
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<FirebaseAuthUserInfo> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        if (displayName.isNotBlank()) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName.trim())
                                .build()
                            user.updateProfile(profileUpdates)
                        }
                        val info = mapFirebaseUser(user).copy(displayName = displayName.ifBlank { null })
                        _currentUserState.value = info
                        cont.resume(Result.success(info))
                    } else {
                        cont.resume(Result.failure(Exception("Could not create user")))
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseAuthUserInfo> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return suspendCancellableCoroutine { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val info = mapFirebaseUser(user)
                        _currentUserState.value = info
                        cont.resume(Result.success(info))
                    } else {
                        cont.resume(Result.failure(Exception("Anonymous login failed")))
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        return suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            _currentUserState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentUser(): FirebaseAuthUserInfo? {
        val user = firebaseAuth?.currentUser ?: return null
        return mapFirebaseUser(user)
    }
}
