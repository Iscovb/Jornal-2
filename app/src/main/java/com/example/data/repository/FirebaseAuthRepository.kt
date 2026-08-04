package com.example.data.repository

import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            checkCurrentUser()
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Firebase initialization error or missing google-services.json", e)
        }
    }

    fun clearError() {
        _authError.value = null
    }

    private fun checkCurrentUser() {
        val currentUser = auth?.currentUser
        if (currentUser != null) {
            val initialProfile = UserProfile(
                uid = currentUser.uid,
                email = currentUser.email ?: "",
                displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Trader",
                photoUrl = currentUser.photoUrl?.toString() ?: ""
            )
            _userProfile.value = initialProfile
            loadUserProfileFromFirestore(currentUser.uid, initialProfile)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String
    ): Boolean {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Please enter both email and password."
            return false
        }
        if (pass.length < 6) {
            _authError.value = "Password must be at least 6 characters."
            return false
        }

        _isLoading.value = true
        _authError.value = null

        return try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = authResult.user
                if (user != null) {
                    val name = displayName.ifBlank { email.substringBefore("@") }
                    val newProfile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: email,
                        displayName = name,
                        traderTitle = "Pro Trader",
                        createdAt = System.currentTimeMillis(),
                        lastLogin = System.currentTimeMillis()
                    )
                    saveUserProfileToFirestore(newProfile)
                    _userProfile.value = newProfile
                    _isLoading.value = false
                    true
                } else {
                    _authError.value = "Failed to create user account."
                    _isLoading.value = false
                    false
                }
            } else {
                // Local mock fallback if Firebase credentials are missing
                val mockUid = "usr_${System.currentTimeMillis()}"
                val name = displayName.ifBlank { email.substringBefore("@") }
                val mockProfile = UserProfile(
                    uid = mockUid,
                    email = email.trim(),
                    displayName = name,
                    traderTitle = "Local Trader (Demo Firebase)"
                )
                _userProfile.value = mockProfile
                _isLoading.value = false
                true
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign up error", e)
            _authError.value = e.localizedMessage ?: "Sign up failed."
            _isLoading.value = false
            false
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Please enter both email and password."
            return false
        }

        _isLoading.value = true
        _authError.value = null

        return try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
                val user = authResult.user
                if (user != null) {
                    val initialProfile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: email,
                        displayName = user.displayName ?: email.substringBefore("@")
                    )
                    _userProfile.value = initialProfile
                    loadUserProfileFromFirestore(user.uid, initialProfile)
                    _isLoading.value = false
                    true
                } else {
                    _authError.value = "Authentication failed."
                    _isLoading.value = false
                    false
                }
            } else {
                // Local mock fallback
                val mockProfile = UserProfile(
                    uid = "usr_demo_123",
                    email = email.trim(),
                    displayName = email.substringBefore("@"),
                    traderTitle = "Pro Trader (Demo)"
                )
                _userProfile.value = mockProfile
                _isLoading.value = false
                true
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign in error", e)
            _authError.value = e.localizedMessage ?: "Invalid credentials or login error."
            _isLoading.value = false
            false
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Boolean {
        _isLoading.value = true
        _authError.value = null

        return try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val initialProfile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "Google Trader",
                        photoUrl = user.photoUrl?.toString() ?: ""
                    )
                    _userProfile.value = initialProfile
                    loadUserProfileFromFirestore(user.uid, initialProfile)
                    _isLoading.value = false
                    true
                } else {
                    _authError.value = "Google sign-in failed."
                    _isLoading.value = false
                    false
                }
            } else {
                val mockProfile = UserProfile(
                    uid = "google_user_${System.currentTimeMillis()}",
                    email = "trader.google@gmail.com",
                    displayName = "Google Trader",
                    traderTitle = "Google Verified Trader"
                )
                _userProfile.value = mockProfile
                _isLoading.value = false
                true
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Google Sign-In Error", e)
            _authError.value = e.localizedMessage ?: "Google Sign-In failed."
            _isLoading.value = false
            false
        }
    }

    fun signInDemoUser(email: String = "trader@journnex.com", name: String = "Alex Rivera") {
        val demoProfile = UserProfile(
            uid = "usr_firebase_demo_992",
            email = email,
            displayName = name,
            photoUrl = "",
            defaultRiskPercent = 1.0,
            targetWinRate = 72.5,
            minRiskReward = 2.5,
            maxDailyLoss = 3.0,
            traderTitle = "Elite Forex & Futures Trader"
        )
        _userProfile.value = demoProfile
        _authError.value = null
    }

    private fun loadUserProfileFromFirestore(uid: String, fallback: UserProfile) {
        val db = firestore ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = UserProfile(
                        uid = uid,
                        email = doc.getString("email") ?: fallback.email,
                        displayName = doc.getString("displayName") ?: fallback.displayName,
                        photoUrl = doc.getString("photoUrl") ?: fallback.photoUrl,
                        defaultRiskPercent = doc.getDouble("defaultRiskPercent") ?: fallback.defaultRiskPercent,
                        targetWinRate = doc.getDouble("targetWinRate") ?: fallback.targetWinRate,
                        minRiskReward = doc.getDouble("minRiskReward") ?: fallback.minRiskReward,
                        maxDailyLoss = doc.getDouble("maxDailyLoss") ?: fallback.maxDailyLoss,
                        traderTitle = doc.getString("traderTitle") ?: fallback.traderTitle,
                        createdAt = doc.getLong("createdAt") ?: fallback.createdAt,
                        lastLogin = System.currentTimeMillis()
                    )
                    _userProfile.value = profile
                } else {
                    // Document doesn't exist yet in Firestore -> Create it now
                    saveUserProfileToFirestore(fallback)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseAuthRepository", "Error loading user profile from Firestore", e)
            }
    }

    fun saveUserProfileToFirestore(profile: UserProfile, onComplete: ((Boolean) -> Unit)? = null) {
        _userProfile.value = profile
        val db = firestore
        if (db != null && profile.uid.isNotBlank()) {
            val userMap = hashMapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "photoUrl" to profile.photoUrl,
                "defaultRiskPercent" to profile.defaultRiskPercent,
                "targetWinRate" to profile.targetWinRate,
                "minRiskReward" to profile.minRiskReward,
                "maxDailyLoss" to profile.maxDailyLoss,
                "traderTitle" to profile.traderTitle,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(profile.uid)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d("FirebaseAuthRepository", "User profile saved to Firestore collection 'users/${profile.uid}'")
                    onComplete?.invoke(true)
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseAuthRepository", "Error saving user profile to Firestore", e)
                    onComplete?.invoke(false)
                }
        } else {
            onComplete?.invoke(true)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Sign out exception", e)
        }
        _userProfile.value = null
        _authError.value = null
    }
}
