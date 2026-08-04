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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playbook
import com.example.data.model.StrategyRule
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
fun PlaybooksScreen(
    uiState: TradingUiState,
    onCreatePlaybook: (String, String, String, Double, Double, List<String>) -> Unit,
    onDeletePlaybook: (Playbook) -> Unit,
    modifier: Modifier = Modifier
) {
    val playbooks = uiState.playbooks
    val rulesMap = uiState.rulesMap
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .testTag("playbooks_screen")
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "PLAYBOOKS & STRATEGY RULES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Define setup rules & track rule compliance rate",
                        fontSize = 12.sp,
                        color = TradingTextMuted
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("button_create_playbook")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Playbook", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            if (playbooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TradingSurface)
                        .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = TradingTextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No strategy playbooks created.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TradingTextPrimary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playbooks, key = { it.id }) { pb ->
                        val rules = rulesMap[pb.id] ?: emptyList()
                        PlaybookCard(
                            playbook = pb,
                            rules = rules,
                            onDelete = { onDeletePlaybook(pb) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlaybookDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { title, desc, tf, winGoal, targetRR, rules ->
                onCreatePlaybook(title, desc, tf, winGoal, targetRR, rules)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlaybookCard(
    playbook: Playbook,
    rules: List<StrategyRule>,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag("playbook_card_${playbook.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TradingSurface)
            .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TradingPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = playbook.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TradingTextPrimary)
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete playbook", tint = TradingTextMuted, modifier = Modifier.size(18.dp))
                }
            }

            if (playbook.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = playbook.description, fontSize = 12.sp, color = TradingTextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecBadge(label = "TF", value = playbook.timeframe)
                SpecBadge(label = "Target WR", value = "${playbook.winRateGoal.toInt()}%")
                SpecBadge(label = "Target R:R", value = "1:${playbook.targetRiskReward}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rules Checklist Box
            Text(text = "STRATEGY CHECKLIST RULES (${rules.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TradingTextMuted, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TradingSurfaceVariant)
                    .padding(10.dp)
            ) {
                rules.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = TradingWinGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = r.ruleText, fontSize = 12.sp, color = TradingTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TradingSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = "$label: $value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TradingPrimary)
    }
}

@Composable
private fun AddPlaybookDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Double, Double, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var tf by remember { mutableStateOf("15m") }
    var winGoalStr by remember { mutableStateOf("70") }
    var targetRRStr by remember { mutableStateOf("2.5") }
    val rules = remember { mutableStateListOf("1. Higher timeframe trend aligned", "2. Risk strictly 1.0%") }
    var newRuleText by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TradingSurface,
        title = { Text("Create Strategy Playbook", color = TradingTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Playbook Title (e.g. ICT Liquidity Sweep)") },
                    colors = dialogTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Strategy Description") },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tf,
                        onValueChange = { tf = it },
                        label = { Text("Timeframe") },
                        colors = dialogTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetRRStr,
                        onValueChange = { targetRRStr = it },
                        label = { Text("Target R:R") },
                        colors = dialogTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Checklist Rules:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TradingTextMuted)
                rules.forEachIndexed { idx, r ->
                    Text("${idx + 1}. $r", fontSize = 12.sp, color = TradingTextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newRuleText,
                        onValueChange = { newRuleText = it },
                        label = { Text("Add Rule (e.g. 3. Volume confirmation)") },
                        colors = dialogTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newRuleText.isNotBlank()) {
                            rules.add(newRuleText)
                            newRuleText = ""
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add rule", tint = TradingPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val wg = winGoalStr.toDoubleOrNull() ?: 70.0
                        val tr = targetRRStr.toDoubleOrNull() ?: 2.0
                        onSubmit(title, desc, tf, wg, tr, rules.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary)
            ) {
                Text("Create Playbook", color = TradingTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TradingSurfaceVariant)) {
                Text("Cancel", color = TradingTextMuted)
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TradingPrimary,
    unfocusedBorderColor = TradingCardBorder,
    focusedContainerColor = TradingSurfaceVariant,
    unfocusedContainerColor = TradingSurfaceVariant,
    focusedTextColor = TradingTextPrimary,
    unfocusedTextColor = TradingTextPrimary
)
