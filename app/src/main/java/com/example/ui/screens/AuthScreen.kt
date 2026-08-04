package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TradingBackground
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingSurfaceVariant
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (email: String, pass: String, name: String) -> Unit,
    onSignIn: (email: String, pass: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onDemoLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpTab by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .testTag("auth_screen")
            .fillMaxSize()
            .background(TradingBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = TradingPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "JOURNNEX TRADING",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TradingTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Firebase Auth & Firestore Journal",
                        fontSize = 11.sp,
                        color = TradingTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Auth Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Auth Mode Switcher (Sign In vs Sign Up)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TradingSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("auth_tab_signin")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isSignUpTab) TradingPrimary else Color.Transparent)
                                .clickable { isSignUpTab = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSignUpTab) Color.White else TradingTextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .testTag("auth_tab_signup")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSignUpTab) TradingPrimary else Color.Transparent)
                                .clickable { isSignUpTab = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSignUpTab) Color.White else TradingTextMuted
                            )
                        }
                    }

                    // Error Message Banner
                    if (!errorMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TradingLossRed.copy(alpha = 0.15f))
                                .border(1.dp, TradingLossRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = TradingLossRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    fontSize = 12.sp,
                                    color = TradingLossRed
                                )
                            }
                        }
                    }

                    // Display Name (for Sign Up)
                    if (isSignUpTab) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name / Trader Handle") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TradingTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .testTag("auth_name_input")
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TradingPrimary,
                                unfocusedBorderColor = TradingCardBorder,
                                focusedLabelColor = TradingPrimary,
                                unfocusedLabelColor = TradingTextMuted,
                                focusedTextColor = TradingTextPrimary,
                                unfocusedTextColor = TradingTextPrimary
                            )
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TradingTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .testTag("auth_email_input")
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TradingPrimary,
                            unfocusedBorderColor = TradingCardBorder,
                            focusedLabelColor = TradingPrimary,
                            unfocusedLabelColor = TradingTextMuted,
                            focusedTextColor = TradingTextPrimary,
                            unfocusedTextColor = TradingTextPrimary
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TradingTextMuted) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = TradingTextMuted
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .testTag("auth_password_input")
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TradingPrimary,
                            unfocusedBorderColor = TradingCardBorder,
                            focusedLabelColor = TradingPrimary,
                            unfocusedLabelColor = TradingTextMuted,
                            focusedTextColor = TradingTextPrimary,
                            unfocusedTextColor = TradingTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Submit Email/Pass Button
                    Button(
                        onClick = {
                            if (isSignUpTab) {
                                onSignUp(email, password, displayName)
                            } else {
                                onSignIn(email, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .testTag("auth_submit_button")
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpTab) "Create Account" else "Sign In with Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Divider OR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(TradingCardBorder)
                        )
                        Text(
                            text = " OR ",
                            fontSize = 11.sp,
                            color = TradingTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(TradingCardBorder)
                        )
                    }

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        enabled = !isLoading,
                        modifier = Modifier
                            .testTag("auth_google_button")
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = TradingSurfaceVariant,
                            contentColor = TradingTextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TradingCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GTranslate,
                            contentDescription = null,
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign In with Google",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Quick Demo Login Button
                    Box(
                        modifier = Modifier
                            .testTag("auth_demo_button")
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDemoLogin() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quick Test Mode / Skip Auth",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TradingPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = TradingPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Firestore collection 'users' will store your profile & risk targets under your unique Firebase UID.",
                fontSize = 11.sp,
                color = TradingTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
