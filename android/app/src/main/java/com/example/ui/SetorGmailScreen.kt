package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfigEntity
import com.example.data.GmailDepositEntity
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.GoldReward
import com.example.ui.theme.GoldRewardDark
import com.example.ui.theme.PrimaryBlue
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetorGmailScreen(
    config: AppConfigEntity?,
    onSingleSubmit: (String, String, String, String, String) -> Unit,
    onBulkSubmit: (List<GmailDepositEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tunggal, 1: Massal

    // Single Form state
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("2024") }
    var noteInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }
    var singleErrorMessage by remember { mutableStateOf<String?>(null) }

    // Bulk Form state
    var bulkTextInput by remember { mutableStateOf("") }
    var isRuleExpanded by remember { mutableStateOf(false) }

    val ratePerAccount = config?.currentRatePerAccount ?: 2500L
    val bonusRate = config?.bonusRateTier ?: 500L

    // Parsed Bulk Accounts with live validation
    val parsedBulkAccounts = remember(bulkTextInput, ratePerAccount) {
        val lines = bulkTextInput.lines().filter { it.isNotBlank() }
        lines.mapIndexed { index, line ->
            val parts = line.split("|", ",", ";", ":", "\t").map { it.trim() }
            val email = parts.getOrNull(0) ?: ""
            val pass = parts.getOrNull(1) ?: ""
            val rec = parts.getOrNull(2) ?: ""
            val yr = parts.getOrNull(3) ?: "2024"
            val isValid = email.contains("@gmail.com", ignoreCase = true) && pass.isNotBlank()

            ParsedAccount(
                index = index + 1,
                email = email,
                password = pass,
                recovery = rec,
                year = yr,
                isValid = isValid,
                estimatedReward = if (isValid) ratePerAccount else 0L
            )
        }
    }

    val validBulkCount = parsedBulkAccounts.count { it.isValid }
    val bulkBonusTotal = if (validBulkCount >= 10) validBulkCount * bonusRate else 0L
    val totalBulkEstimatedReward = (validBulkCount * ratePerAccount) + bulkBonusTotal

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Title & Reward Callout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Setor Akun Gmail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Rate: ${Formatters.formatRupiah(ratePerAccount)} / Akun Valid",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreenDark
                )
            }

            Surface(
                color = GoldReward.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldReward.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Bonus",
                        tint = GoldRewardDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bonus >= 10 Akun", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldRewardDark)
                }
            }
        }

        // 2. Mode Tabs (Setor Tunggal vs Setor Massal)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = PrimaryBlue,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Setor Tunggal (1 Akun)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Setor Massal (Banyak)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // 3. Form Content based on Tab
        if (selectedTab == 0) {
            // === SETOR TUNGGAL FORM ===
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Formulir Setor Akun Tunggal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it.lowercase().trim() },
                        label = { Text("Alamat Email Gmail") },
                        placeholder = { Text("contoh.akun@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        trailingIcon = {
                            if (emailInput.isNotBlank()) {
                                val isGmailValid = emailInput.contains("@gmail.com")
                                Icon(
                                    imageVector = if (isGmailValid) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isGmailValid) EmeraldGreen else CoralRed
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_email_input")
                    )

                    // Auto-append @gmail.com helper chips
                    if (!emailInput.contains("@") && emailInput.isNotBlank()) {
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { emailInput += "@gmail.com" }
                            ) {
                                Text(
                                    text = "+ Tambah @gmail.com",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Kata Sandi / Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recovery Email Input
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it.trim() },
                        label = { Text("Email / No Telp Pemulihan (Opsional)") },
                        placeholder = { Text("pemulihan@outlook.com") },
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("deposit_recovery_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Year Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isYearDropdownExpanded,
                            onExpandedChange = { isYearDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = "Tahun $selectedYear",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tahun Akun") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = isYearDropdownExpanded,
                                onDismissRequest = { isYearDropdownExpanded = false }
                            ) {
                                listOf("2020-2022 (Old Account)", "2023", "2024", "2025", "2026").forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text(yr) },
                                        onClick = {
                                            selectedYear = yr.take(4)
                                            isYearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Catatan Singkat
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("Catatan") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (singleErrorMessage != null) {
                        Text(
                            text = singleErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Single Estimate Card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estimasi Reward:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = Formatters.formatRupiah(ratePerAccount),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark
                                )
                            }
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Auto Masuk Saldo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!emailInput.contains("@gmail.com")) {
                                singleErrorMessage = "Format email harus menggunakan domain @gmail.com"
                            } else if (passwordInput.length < 6) {
                                singleErrorMessage = "Password minimal 6 karakter"
                            } else {
                                singleErrorMessage = null
                                onSingleSubmit(emailInput, passwordInput, recoveryInput, selectedYear, noteInput)
                                emailInput = ""
                                passwordInput = ""
                                recoveryInput = ""
                                noteInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_single_deposit_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim Setoran Akun", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        } else {
            // === SETOR MASSAL FORM ===
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Setor Massal (Multi-Akun)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = {
                            bulkTextInput = "mitra.alfa01@gmail.com|PassAlfa123#|rec01@yahoo.com|2023\n" +
                                    "mitra.beta02@gmail.com|PassBeta2024!|rec02@outlook.com|2024\n" +
                                    "mitra.gamma03@gmail.com|GammaPass99*||2024"
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Isi Contoh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Format: email|password|recovery|tahun (1 baris per akun)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bulkTextInput,
                        onValueChange = { bulkTextInput = it },
                        placeholder = {
                            Text(
                                "akun1@gmail.com|pass123|rec1@email.com|2023\n" +
                                        "akun2@gmail.com|pass456||2024\n" +
                                        "akun3@gmail.com|pass789|rec3@email.com|2024"
                            )
                        },
                        minLines = 5,
                        maxLines = 10,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bulk_deposit_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Bulk Parse Summary
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Akun Terdeteksi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$validBulkCount Akun Valid dari ${parsedBulkAccounts.size} baris", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            if (validBulkCount >= 10) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bonus Tier (>= 10 akun):", fontSize = 12.sp, color = GoldRewardDark)
                                    Text("+${Formatters.formatRupiah(bulkBonusTotal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldRewardDark)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estimasi Total Reward:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = Formatters.formatRupiah(totalBulkEstimatedReward),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val validDeposits = parsedBulkAccounts
                                .filter { it.isValid }
                                .map { item ->
                                    GmailDepositEntity(
                                        userId = 0L,
                                        username = "",
                                        email = item.email,
                                        password = item.password,
                                        recoveryInfo = item.recovery,
                                        accountYear = item.year,
                                        status = "PENDING",
                                        rewardAmount = ratePerAccount,
                                        note = "Setor massal batch"
                                    )
                                }
                            if (validDeposits.isNotEmpty()) {
                                onBulkSubmit(validDeposits)
                                bulkTextInput = ""
                            }
                        },
                        enabled = validBulkCount > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_bulk_deposit_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim $validBulkCount Akun Sekaligus", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // 4. Panduan Format & Syarat Akun (Expandable Card)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isRuleExpanded = !isRuleExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Syarat Akun Agar 100% Lolos Verifikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (isRuleExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = isRuleExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        RuleItem(text = "Akun Gmail aktif dan tidak terkena suspend / disabled.")
                        RuleItem(text = "Verifikasi 2 Langkah (2FA / Authenticator) WAJIB dalam kondisi NONAKTIF.")
                        RuleItem(text = "Password permanen & valid (minimal 6 karakter).")
                        RuleItem(text = "Disarankan melampirkan email pemulihan agar tidak terkena verifikasi nomor ponsel.")
                        RuleItem(text = "Akun tahun pembuatan 2023 ke bawah berhak mendapatkan bonus rate prioritas.")
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

private data class ParsedAccount(
    val index: Int,
    val email: String,
    val password: String,
    val recovery: String,
    val year: String,
    val isValid: Boolean,
    val estimatedReward: Long
)
