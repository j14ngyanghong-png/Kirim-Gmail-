package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnnouncementEntity
import com.example.data.AppConfigEntity
import com.example.data.GmailDepositEntity
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.GoldReward
import com.example.ui.theme.GoldRewardDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.WarningAmber
import com.example.util.Formatters

@Composable
fun AdminDashboardScreen(
    config: AppConfigEntity?,
    allDeposits: List<GmailDepositEntity>,
    allWithdrawals: List<WithdrawalEntity>,
    allUsers: List<UserEntity>,
    announcements: List<AnnouncementEntity>,
    onApproveDeposit: (GmailDepositEntity) -> Unit,
    onRejectDeposit: (GmailDepositEntity, String) -> Unit,
    onCompleteWithdrawal: (WithdrawalEntity) -> Unit,
    onRejectWithdrawal: (WithdrawalEntity, String) -> Unit,
    onToggleMaintenance: (Boolean) -> Unit,
    onUpdateRate: (Long) -> Unit,
    onCreateAnnouncement: (String, String, String, Boolean) -> Unit,
    onDeleteAnnouncement: (Long) -> Unit,
    onSwitchRoleUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableIntStateOf(0) }
    var showRejectDialogForDeposit by remember { mutableStateOf<GmailDepositEntity?>(null) }
    var selectedRejectReason by remember { mutableStateOf("Verifikasi 2 Langkah (2FA) Terdeteksi Aktif") }
    var showEditRateDialog by remember { mutableStateOf(false) }
    var newRateInput by remember { mutableStateOf(config?.currentRatePerAccount?.toString() ?: "2500") }

    val pendingDeposits = remember(allDeposits) { allDeposits.filter { it.status == "PENDING" } }
    val pendingWithdrawals = remember(allWithdrawals) { allWithdrawals.filter { it.status == "PROCESSED" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Admin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldReward.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = GoldRewardDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin Control Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Kelola verifikasi, penarikan, & sistem",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onSwitchRoleUser,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("admin_switch_to_user")
                ) {
                    Text("Mode User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Metrics Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Antrian Verifikasi",
                    value = "${pendingDeposits.size} Akun",
                    icon = Icons.Default.HourglassEmpty,
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Antrian Payout",
                    value = "${pendingWithdrawals.size} Transaksi",
                    icon = Icons.Default.Payments,
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. System Config Controls (Maintenance & Rate)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Maintenance Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = CoralRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Mode Maintenance Server", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (config?.isMaintenanceMode == true) "Aktif (User diarahkan ke mode maintenance)" else "Nonaktif (Server beroperasi normal)",
                                    fontSize = 11.sp,
                                    color = if (config?.isMaintenanceMode == true) CoralRed else EmeraldGreen
                                )
                            }
                        }
                        Switch(
                            checked = config?.isMaintenanceMode ?: false,
                            onCheckedChange = { onToggleMaintenance(it) },
                            modifier = Modifier.testTag("maintenance_toggle")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Rate Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Rate Reward Saat Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "${Formatters.formatRupiah(config?.currentRatePerAccount ?: 2500)} / Akun Valid",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { showEditRateDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Ubah Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Admin Management Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = adminTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = PrimaryBlue,
                edgePadding = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    text = { Text("Antrian Verifikasi (${pendingDeposits.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    text = { Text("Penarikan (${pendingWithdrawals.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    text = { Text("Data Pengguna (${allUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    text = { Text("Buat Pengumuman", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 },
                    text = { Text("Rekap Harian", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // 5. Tab Contents
        when (adminTab) {
            0 -> {
                // Verification Queue
                if (pendingDeposits.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.CheckCircle,
                            title = "Tidak Ada Antrian Verifikasi",
                            description = "Semua setoran akun Gmail telah diperiksa. Menunggu setoran baru dari mitra."
                        )
                    }
                } else {
                    items(pendingDeposits) { deposit ->
                        AdminDepositReviewCard(
                            deposit = deposit,
                            onApprove = { onApproveDeposit(deposit) },
                            onReject = { showRejectDialogForDeposit = deposit }
                        )
                    }
                }
            }
            1 -> {
                // Withdrawals Manager
                if (pendingWithdrawals.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.Payments,
                            title = "Tidak Ada Antrian Penarikan",
                            description = "Semua pencairan saldo telah selesai diproses."
                        )
                    }
                } else {
                    items(pendingWithdrawals) { withdrawal ->
                        AdminWithdrawalReviewCard(
                            withdrawal = withdrawal,
                            onComplete = { onCompleteWithdrawal(withdrawal) },
                            onReject = { onRejectWithdrawal(withdrawal, "Data rekening tidak valid") }
                        )
                    }
                }
            }
            2 -> {
                // User Management
                items(allUsers) { u ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = u.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = u.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "WA: ${u.phone} • Ref: ${u.referralCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatRupiah(u.balance),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EmeraldGreenDark
                                )
                                Text(
                                    text = "${u.validCount} Valid / ${u.totalDeposited} Total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                // Create Announcement Form
                item {
                    AdminCreateAnnouncementCard(onCreate = onCreateAnnouncement)
                }
                item {
                    SectionHeader(title = "Daftar Pengumuman Aktif")
                }
                items(announcements) { ann ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = ann.content, maxLines = 2, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteAnnouncement(ann.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = CoralRed)
                            }
                        }
                    }
                }
            }
            4 -> {
                // Daily Revenue & Activity Recap
                item {
                    AdminDailyRecapCard(
                        allDeposits = allDeposits,
                        allWithdrawals = allWithdrawals,
                        allUsers = allUsers
                    )
                }
            }
        }
    }

    // Reject Deposit Dialog with predefined reasons
    if (showRejectDialogForDeposit != null) {
        val targetDeposit = showRejectDialogForDeposit!!
        AlertDialog(
            onDismissRequest = { showRejectDialogForDeposit = null },
            title = { Text("Tolak Setoran Akun", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pilih alasan penolakan untuk akun: ${targetDeposit.email}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val reasons = listOf(
                        "Verifikasi 2 Langkah (2FA) Terdeteksi Aktif",
                        "Password Salah / Tidak Cocok",
                        "Akun Terkunci / Butuh Verifikasi Nomor HP",
                        "Email Sudah Pernah Disetor Sebelumnya (Duplikat)",
                        "Akun Gmail Baru Dibuat (< 1 Minggu)"
                    )

                    reasons.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedRejectReason = r }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedRejectReason == r) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (selectedRejectReason == r) PrimaryBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = r, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectDeposit(targetDeposit, selectedRejectReason)
                        showRejectDialogForDeposit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Konfirmasi Tolak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialogForDeposit = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Edit Rate Dialog
    if (showEditRateDialog) {
        AlertDialog(
            onDismissRequest = { showEditRateDialog = false },
            title = { Text("Ubah Rate Reward Akun", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Masukkan nominal reward per akun valid (Rp):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newRateInput,
                        onValueChange = { newRateInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Rate per Akun (Rp)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = newRateInput.toLongOrNull() ?: 2500L
                        onUpdateRate(rate)
                        showEditRateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan Rate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AdminDepositReviewCard(
    deposit: GmailDepositEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = deposit.email, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Password: ${deposit.password}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    if (deposit.recoveryInfo.isNotBlank()) {
                        Text(text = "Recovery: ${deposit.recoveryInfo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "Oleh: ${deposit.username} • Thn ${deposit.accountYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = GoldReward.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Formatters.formatRupiah(deposit.rewardAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldRewardDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setujui (+Cairkan)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tolak...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalReviewCard(
    withdrawal: WithdrawalEntity,
    onComplete: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${withdrawal.method} - ${withdrawal.accountNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Nama: ${withdrawal.accountHolderName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "User: ${withdrawal.username} • ${Formatters.formatDate(withdrawal.requestedAt)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = Formatters.formatRupiah(withdrawal.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = EmeraldGreenDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transfer Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("Tolak & Refund", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminCreateAnnouncementCard(
    onCreate: (String, String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("RATE_UPDATE") }
    var isImportant by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Buat Pengumuman Baru", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Pengumuman") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Isi Pesan / Pengumuman") },
                minLines = 3,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isImportant, onCheckedChange = { isImportant = it })
                Text("Tandai sebagai PENTING (Disematkan / Pinned)", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onCreate(title, content, category, isImportant)
                        title = ""
                        content = ""
                        isImportant = false
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim Broadcast Pengumuman", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminDailyRecapCard(
    allDeposits: List<GmailDepositEntity>,
    allWithdrawals: List<WithdrawalEntity>,
    allUsers: List<UserEntity>,
    modifier: Modifier = Modifier
) {
    val totalApproved = allDeposits.count { it.status == "APPROVED" }
    val totalRejected = allDeposits.count { it.status == "REJECTED" }
    val totalRewardPaid = allDeposits.filter { it.status == "APPROVED" }.sumOf { it.rewardAmount }
    val totalWithdrawalSuccess = allWithdrawals.filter { it.status == "SUCCESS" }.sumOf { it.amount }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "📊 Rekap Akumulasi Sistem", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Akun Gmail Masuk:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${allDeposits.size} Akun", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Akun Berhasil Disetujui:", fontSize = 13.sp, color = EmeraldGreenDark)
                Text("$totalApproved Akun", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldGreenDark)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Akun Ditolak / Tidak Valid:", fontSize = 13.sp, color = CoralRed)
                Text("$totalRejected Akun", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CoralRed)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Reward Dicatat:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Formatters.formatRupiah(totalRewardPaid), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Payout Ditransfer:", fontSize = 13.sp, color = EmeraldGreenDark)
                Text(Formatters.formatRupiah(totalWithdrawalSuccess), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = EmeraldGreenDark)
            }
        }
    }
}
