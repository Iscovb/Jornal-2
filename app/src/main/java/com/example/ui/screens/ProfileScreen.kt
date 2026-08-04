package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingSurfaceVariant
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.viewmodel.TradingUiState

@Composable
fun ProfileScreen(
    uiState: TradingUiState,
    onSaveProfile: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = uiState.userProfile

    if (userProfile == null) {
        // Unauthenticated State
        Column(
            modifier = modifier
                .testTag("profile_screen_unauth")
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TradingPrimary.copy(alpha = 0.15f))
                    .border(2.dp, TradingPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TradingPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FIREBASE ACCOUNT REQUIRED",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TradingTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in or create an account to persist your trading settings, risk targets, and accounts to Firestore.",
                fontSize = 13.sp,
                color = TradingTextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenAuth,
                modifier = Modifier
                    .testTag("profile_open_auth_button")
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Sign In / Register Account", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Authenticated Profile View
    var riskPercentText by remember(userProfile) { mutableStateOf(userProfile.defaultRiskPercent.toString()) }
    var winRateText by remember(userProfile) { mutableStateOf(userProfile.targetWinRate.toString()) }
    var minRrText by remember(userProfile) { mutableStateOf(userProfile.minRiskReward.toString()) }
    var maxDailyLossText by remember(userProfile) { mutableStateOf(userProfile.maxDailyLoss.toString()) }
    var traderTitleText by remember(userProfile) { mutableStateOf(userProfile.traderTitle) }

    Column(
        modifier = modifier
            .testTag("profile_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TRADER PROFILE & FIRESTORE SETTINGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted,
            letterSpacing = 1.sp
        )

        // Profile Card with Firestore UID Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TradingSurface)
                .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TradingPrimary.copy(alpha = 0.2f))
                            .border(1.5.dp, TradingPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TradingPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.displayName.ifBlank { "Trader" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TradingTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = TradingPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = userProfile.email.ifBlank { "No Email" },
                            fontSize = 12.sp,
                            color = TradingTextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TradingWinGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = TradingWinGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firestore Synced (Collection: users)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TradingWinGreen
                                )
                            }
                        }
                    }
                }

                // UID Pill Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TradingSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FIREBASE UID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextMuted
                        )
                        Text(
                            text = userProfile.uid,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TradingPrimary
                        )
                    }
                }
            }
        }

        // Editable Risk & Discipline Parameters (Persisted in Firestore)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TradingSurface)
                .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = TradingPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RISK & DISCIPLINE TARGETS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                    }
                    Text(text = "FIRESTORE DATA", fontSize = 10.sp, color = TradingWinGreen, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = traderTitleText,
                    onValueChange = { traderTitleText = it },
                    label = { Text("Trader Persona / Specialization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TradingPrimary,
                        unfocusedBorderColor = TradingCardBorder,
                        focusedLabelColor = TradingPrimary,
                        unfocusedLabelColor = TradingTextMuted,
                        focusedTextColor = TradingTextPrimary,
                        unfocusedTextColor = TradingTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = riskPercentText,
                        onValueChange = { riskPercentText = it },
                        label = { Text("Default Risk %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TradingPrimary,
                            unfocusedBorderColor = TradingCardBorder,
                            focusedLabelColor = TradingPrimary,
                            unfocusedLabelColor = TradingTextMuted,
                            focusedTextColor = TradingTextPrimary,
                            unfocusedTextColor = TradingTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = winRateText,
                        onValueChange = { winRateText = it },
                        label = { Text("Target Win Rate %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minRrText,
                        onValueChange = { minRrText = it },
                        label = { Text("Min R:R Ratio") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TradingPrimary,
                            unfocusedBorderColor = TradingCardBorder,
                            focusedLabelColor = TradingPrimary,
                            unfocusedLabelColor = TradingTextMuted,
                            focusedTextColor = TradingTextPrimary,
                            unfocusedTextColor = TradingTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = maxDailyLossText,
                        onValueChange = { maxDailyLossText = it },
                        label = { Text("Max Daily Loss %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val updatedProfile = userProfile.copy(
                            defaultRiskPercent = riskPercentText.toDoubleOrNull() ?: userProfile.defaultRiskPercent,
                            targetWinRate = winRateText.toDoubleOrNull() ?: userProfile.targetWinRate,
                            minRiskReward = minRrText.toDoubleOrNull() ?: userProfile.minRiskReward,
                            maxDailyLoss = maxDailyLossText.toDoubleOrNull() ?: userProfile.maxDailyLoss,
                            traderTitle = traderTitleText
                        )
                        onSaveProfile(updatedProfile)
                    },
                    modifier = Modifier
                        .testTag("save_profile_firestore_button")
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Settings to Firestore Collection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Database Statistics Overview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TradingSurface)
                .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "JOURNAL DATABASE OVERVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TradingTextPrimary
                )

                ProfileStatRow(label = "Total Trades Logged", value = "${uiState.trades.size} trades")
                ProfileStatRow(label = "Trading Accounts", value = "${uiState.accounts.size} accounts")
                ProfileStatRow(label = "Active Strategy Playbooks", value = "${uiState.playbooks.size} playbooks")
                ProfileStatRow(label = "Overall Win Rate", value = String.format("%.1f%%", uiState.metrics.winRatePercent))
            }
        }

        // Firebase Configuration Instructions Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TradingSurface)
                .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TradingPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FIREBASE SETUP INSTRUCTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextPrimary
                    )
                }

                Text(
                    text = "To connect your own live Firebase project to this app:",
                    fontSize = 12.sp,
                    color = TradingTextMuted
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. Download google-services.json from your Firebase Console and place it in the app/ folder.",
                        fontSize = 11.sp,
                        color = TradingTextMuted
                    )
                    Text(
                        text = "2. Enable Email/Password & Google Authentication providers under Firebase Console > Authentication.",
                        fontSize = 11.sp,
                        color = TradingTextMuted
                    )
                    Text(
                        text = "3. Create Cloud Firestore in your Firebase project and set rules for collection 'users':",
                        fontSize = 11.sp,
                        color = TradingTextMuted
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TradingSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "match /users/{userId} {\n  allow read, write: if request.auth != null && request.auth.uid == userId;\n}",
                            fontSize = 10.sp,
                            color = TradingPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Logout Button
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .testTag("logout_button")
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = TradingLossRed.copy(alpha = 0.1f),
                contentColor = TradingLossRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TradingLossRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Log Out from Firebase", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TradingTextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TradingTextPrimary)
    }
}
