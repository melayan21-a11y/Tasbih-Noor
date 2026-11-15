package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import android.widget.Toast
import com.example.data.backup.BackupManager
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.example.R
import com.example.notifications.ReminderSetting
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.util.HapticIntensity
import com.example.util.SoundEffectType
import com.example.util.SpeechCadence
import com.example.util.VoiceQualityTier
import com.example.ui.viewmodel.TasbihViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: TasbihViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNightMode by viewModel.isNightMode.collectAsState()
    val beadMaterial by viewModel.beadMaterial.collectAsState()
    val beadTension by viewModel.beadTension.collectAsState()
    val singleBeadLock by viewModel.singleBeadLock.collectAsState()
    val soundType by viewModel.soundType.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val autoGuideSpeed by viewModel.autoGuideSpeedSeconds.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val speechCadence by viewModel.speechCadence.collectAsState()
    val preferArabicVoice by viewModel.preferArabicVoice.collectAsState()
    val wearConnectionStatus by viewModel.wearConnectionStatus.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var restoreDialogMessage by remember { mutableStateOf<String?>(null) }

    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val content = BackupManager.readFromUri(context, uri)
            if (content != null) {
                viewModel.restoreBackupJson(content) { result ->
                    restoreDialogMessage = result.message
                }
            } else {
                restoreDialogMessage = "Could not read the selected backup file."
            }
        }
    }

    if (restoreDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { restoreDialogMessage = null },
            title = { Text("Backup Restore", fontWeight = FontWeight.Bold) },
            text = { Text(restoreDialogMessage ?: "") },
            confirmButton = {
                Button(onClick = { restoreDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF071914) else Color(0xFFF7FAF8))
    ) {
        if (isNightMode) {
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_night),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.22f
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings & Reminders",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNightMode) GoldLight else EmeraldDeep
                )
                Text(
                    text = "Customize notifications, sounds, bead aesthetics, and haptics",
                    fontSize = 13.sp,
                    color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF5A7268)
                )
            }
        }

        // Daily Reminder Notifications Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Daily Reminders",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Reminder Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Receive gentle alerts to maintain your daily morning, evening, and nighttime adhkar routines.",
                        fontSize = 12.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )

                    // Permission Warning if missing
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notifications permission required",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                ) {
                                    Text("Allow", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    reminders.forEachIndexed { index, reminder ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = if (isNightMode) Color(0xFF143B30) else Color(0xFFEEEEEE)
                            )
                        }
                        ReminderRow(
                            reminder = reminder,
                            isNightMode = isNightMode,
                            onToggle = { enabled ->
                                if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.toggleReminder(reminder.id, enabled)
                            },
                            onPickTime = {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        viewModel.updateReminderTime(reminder.id, hourOfDay, minute)
                                    },
                                    reminder.hour,
                                    reminder.minute,
                                    false
                                ).show()
                            }
                        )
                    }
                }
            }
        }

        // Bead Material & Aesthetics Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bead_material_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Bead Material",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prayer Bead Material",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else EmeraldDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Authentic prayer bead textures with natural chatoyancy and grain patterns",
                        fontSize = 12.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Olive Wood & Textured Tiger's Eye
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(BeadMaterial.OLIVE_WOOD, BeadMaterial.TIGER_EYE).forEach { mat ->
                            val isSelected = beadMaterial == mat
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                                        } else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { viewModel.setBeadMaterial(mat) }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = mat.baseColor,
                                    modifier = Modifier.size(30.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, mat.lightColor)
                                ) {}
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (mat == BeadMaterial.OLIVE_WOOD) "Olive Wood" else "Tiger's Eye",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) (if (isNightMode) GoldLight else EmeraldDeep) else (if (isNightMode) Color.White else Color.DarkGray)
                                    )
                                    Text(
                                        text = if (mat.isTextured) "✨ Textured Agate" else "Madinah Grain",
                                        fontSize = 10.sp,
                                        color = if (mat.isTextured) GoldPrimary else (if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray),
                                        fontWeight = if (mat.isTextured) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Royal Jade, Pearl of Noor, Celestial Lapis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(BeadMaterial.ROYAL_JADE, BeadMaterial.PEARL, BeadMaterial.LAPIS_LAZULI).forEach { mat ->
                            val isSelected = beadMaterial == mat
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                                        } else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setBeadMaterial(mat) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = mat.baseColor,
                                    modifier = Modifier.size(26.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, mat.lightColor)
                                ) {}
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (mat) {
                                        BeadMaterial.ROYAL_JADE -> "Jade"
                                        BeadMaterial.PEARL -> "Pearl"
                                        BeadMaterial.LAPIS_LAZULI -> "Lapis"
                                        else -> mat.displayName
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isNightMode) Color.White else Color.DarkGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bead String Tension & Sensitivity Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tension_sensitivity_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Tension and Sensitivity",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "String Tension & Drag Sensitivity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else EmeraldDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Customize string resistance and prevent accidentally scrolling multiple beads.",
                        fontSize = 12.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Bead Lock per Drag Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isNightMode) Color(0xFF103328) else Color(0xFFF1F8F4))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1-Bead Pull Lock",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = "Stops you from scrolling 3 to 5 beads by accident. Each drag stroke advances exactly 1 bead.",
                                fontSize = 11.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF4A655A)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = singleBeadLock,
                            onCheckedChange = { viewModel.setSingleBeadLock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("single_bead_lock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tension Resistance Presets
                    Text(
                        text = "Cord Resistance Level",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color.White else Color(0xFF263238)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            BeadTension.FIRM,
                            BeadTension.STANDARD,
                            BeadTension.LIGHT
                        ).forEach { t ->
                            val isSelected = beadTension == t
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                                        } else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setBeadTension(t) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = t.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) (if (isNightMode) GoldLight else EmeraldDeep) else (if (isNightMode) Color.White else Color.DarkGray)
                                    )
                                    Text(
                                        text = t.description,
                                        fontSize = 11.sp,
                                        color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                                    )
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isNightMode) GoldPrimary else EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sound & Haptic Feedback Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sound_haptic_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Sound effects
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Sound Effect",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bead Tap Acoustic Sound",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else EmeraldDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            SoundEffectType.WOOD_CLICK to "Olive Wood",
                            SoundEffectType.CRYSTAL_CLICK to "Crystal",
                            SoundEffectType.SOFT_CHIME to "Chime",
                            SoundEffectType.PEARL_TAP to "Pearl",
                            SoundEffectType.MUTE to "Mute"
                        ).forEach { (type, label) ->
                            val isSelected = soundType == type
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    if (isNightMode) GoldPrimary else EmeraldPrimary
                                } else {
                                    if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F5F3)
                                },
                                modifier = Modifier
                                    .clickable { viewModel.setSoundType(type) }
                                    .testTag("sound_chip_${type.name.lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        if (isNightMode) Color(0xFF071914) else Color.White
                                    } else {
                                        if (isNightMode) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Haptic Feedback
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptic Feedback",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Haptic Vibration Response",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else EmeraldDeep
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            HapticIntensity.OFF to "Off",
                            HapticIntensity.MILD to "Mild",
                            HapticIntensity.MEDIUM to "Medium",
                            HapticIntensity.STRONG to "Strong"
                        ).forEach { (intensity, label) ->
                            val isSelected = hapticIntensity == intensity
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    if (isNightMode) GoldPrimary else EmeraldPrimary
                                } else {
                                    if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F5F3)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setHapticIntensity(intensity) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        if (isNightMode) Color(0xFF071914) else Color.White
                                    } else {
                                        if (isNightMode) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recitation Voice & Natural Audio Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recitation_voice_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Recitation Voice",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recitation Voice & Natural Audio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Voice Status & Quality Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F8F4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = voiceState.activeVoiceLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isNightMode) GoldLight else EmeraldDeep,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (voiceState.qualityTier) {
                                        VoiceQualityTier.HD_NEURAL -> EmeraldPrimary
                                        VoiceQualityTier.STANDARD -> Color(0xFF455A64)
                                        VoiceQualityTier.SYNTHESIZER_FALLBACK -> Color(0xFFE65100)
                                    }
                                ) {
                                    Text(
                                        text = voiceState.qualityTier.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (voiceState.qualityTier == VoiceQualityTier.SYNTHESIZER_FALLBACK) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your phone is currently using a basic offline synthesizer. Tap 'Install HD Voices' below to download Google's free natural Arabic voice pack.",
                                    fontSize = 11.sp,
                                    color = if (isNightMode) Color.White.copy(alpha = 0.78f) else Color(0xFFBF360C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Mode
                    Text(
                        text = "Recitation Language",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color.White.copy(alpha = 0.85f) else Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(true to "Arabic Voice", false to "English Translit").forEach { (isArabic, label) ->
                            val isSelected = preferArabicVoice == isArabic
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    if (isNightMode) GoldPrimary else EmeraldPrimary
                                } else {
                                    if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F5F3)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setPreferArabicVoice(isArabic) }
                                    .testTag(if (isArabic) "voice_lang_arabic" else "voice_lang_english")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isNightMode) Color(0xFF071914) else Color.White
                                    } else {
                                        if (isNightMode) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 9.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cadence Speed
                    Text(
                        text = "Recitation Cadence",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color.White.copy(alpha = 0.85f) else Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpeechCadence.entries.forEach { cadence ->
                            val isSelected = speechCadence == cadence
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    if (isNightMode) GoldPrimary else EmeraldPrimary
                                } else {
                                    if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F5F3)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setSpeechCadence(cadence) }
                                    .testTag("cadence_${cadence.name.lowercase()}")
                            ) {
                                Text(
                                    text = "${cadence.label} (${cadence.rate}x)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isNightMode) Color(0xFF071914) else Color.White
                                    } else {
                                        if (isNightMode) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (voiceState.availableVoices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Installed System Voices",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNightMode) Color.White.copy(alpha = 0.85f) else Color(0xFF37474F)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isAuto = voiceState.selectedVoiceName == null
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAuto) {
                                    if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                                } else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isAuto) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.selectTtsVoice(null) }
                                    .testTag("voice_option_auto")
                            ) {
                                Text(
                                    text = "Auto Best HD",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isNightMode) GoldLight else EmeraldDeep,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            voiceState.availableVoices.take(6).forEach { option ->
                                val selected = voiceState.selectedVoiceName == option.name
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) {
                                        if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                                    } else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                                    ),
                                    modifier = Modifier.clickable { viewModel.selectTtsVoice(option.name) }
                                ) {
                                    Text(
                                        text = option.displayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isNightMode) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.speakCurrentDhikr() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                                contentColor = if (isNightMode) Color(0xFF071914) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_voice_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Voice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.audioHapticManager.openTtsVoiceInstaller(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("install_hd_voices_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install HD Voices", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Night Recitation Mode Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("night_mode_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Night Mode",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Night Noor Theme (Default)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = "Deep midnight emerald with celestial gold Islamic motifs and low-glare beads",
                                fontSize = 11.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = isNightMode,
                        onCheckedChange = { viewModel.setNightMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }
            }
        }

        // Keep Screen Awake During Dhikr Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("keep_awake_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.StayCurrentPortrait,
                            contentDescription = "Keep Screen Awake",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Keep Screen Awake",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = "Keeps the screen lit while reciting so your phone never dims or locks during dhikr",
                                fontSize = 11.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = keepScreenAwake,
                        onCheckedChange = { viewModel.setKeepScreenAwake(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = EmeraldPrimary
                        ),
                        modifier = Modifier.testTag("keep_awake_switch")
                    )
                }
            }
        }

        // Wear OS Smartwatch Companion & Sync Section
        item {
            var showWatchSetupGuide by remember { mutableStateOf(false) }
            var watchInstallMessage by remember { mutableStateOf<String?>(null) }

            if (showWatchSetupGuide) {
                AlertDialog(
                    onDismissRequest = { showWatchSetupGuide = false },
                    title = {
                        Text(
                            "Install Tasbih Noor on Your Watch",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Android phones do not let one app directly install an APK onto a watch over Bluetooth without Google Play (blocked by Play security policies). You have two ways to install it:",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "1. Google Play Store (Easiest for Release):\nIn Google Play Console, turn on the Wear OS form factor track and upload the :wear build alongside your phone app. Then tap 'Install on Watch (Play Store)' to open it right on your watch.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "2. Testing Before Publishing (Sideload):\nFrom your exported project ZIP, build wear/build/outputs/apk/debug/wear-debug.apk (via 'gradle :wear:assembleDebug') and install it onto your watch using Watch Wireless Debugging (adb install wear-debug.apk) or Wear Installer 2.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showWatchSetupGuide = false }) {
                            Text("Got It")
                        }
                    }
                )
            }

            if (watchInstallMessage != null) {
                AlertDialog(
                    onDismissRequest = { watchInstallMessage = null },
                    title = { Text("Watch App Install", fontWeight = FontWeight.Bold) },
                    text = { Text(watchInstallMessage ?: "", fontSize = 13.sp) },
                    confirmButton = {
                        Button(onClick = { watchInstallMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wear_companion_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "Wear OS Smartwatch Companion",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wear OS Smartwatch Companion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = "Wrist bead counter, Watch Face Complication, and two-way sync",
                                fontSize = 11.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F8F4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusText = when {
                            wearConnectionStatus.installedWatches.isNotEmpty() ->
                                "Installed & Connected: ${wearConnectionStatus.installedWatches.joinToString(", ") { it.displayName }}"
                            wearConnectionStatus.missingAppWatches.isNotEmpty() ->
                                "Watch paired (${wearConnectionStatus.missingAppWatches.joinToString(", ") { it.displayName }}) — Watch app not installed yet"
                            wearConnectionStatus.connectedWatches.isNotEmpty() ->
                                "Connected: ${wearConnectionStatus.connectedWatches.joinToString(", ")}"
                            wearConnectionStatus.isPlayServicesAvailable ->
                                "Ready for Wear OS • No paired watch currently reachable"
                            else ->
                                "Wear OS Sync standing by • Pair a Wear OS watch via Bluetooth"
                        }
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (wearConnectionStatus.installedWatches.isNotEmpty()) {
                                if (isNightMode) GoldLight else EmeraldPrimary
                            } else {
                                if (isNightMode) Color.White.copy(alpha = 0.85f) else Color(0xFF4A655A)
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.openWatchPlayStore { result ->
                                watchInstallMessage = result.userMessage
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                            contentColor = if (isNightMode) Color(0xFF071914) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("install_on_watch_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install on Watch (Play Store)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncStateToWear()
                                Toast.makeText(context, "Synced dhikr & progress to Wear OS", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9),
                                contentColor = if (isNightMode) GoldLight else EmeraldDeep
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_to_watch_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync to Watch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.refreshWearConnection() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("check_watch_btn")
                        ) {
                            Text("Check Watch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showWatchSetupGuide = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watch_setup_guide_btn")
                    ) {
                        Text("How to Install Watch APK (Testing & Play Store)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Data Backup & Export Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backup_export_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Data Backup",
                            tint = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Data Backup & Export",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = "Safeguard your custom dhikrs and prayer logs locally, or export to spreadsheet",
                                fontSize = 12.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button 1: Export Full Backup (JSON)
                    Button(
                        onClick = {
                            viewModel.exportBackupJson { json ->
                                val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                BackupManager.shareContentAsFile(
                                    context = context,
                                    content = json,
                                    fileName = "tasbih_noor_backup_$dateStr.json",
                                    mimeType = "application/json"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9),
                            contentColor = if (isNightMode) GoldLight else EmeraldDeep
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_json_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backup (JSON)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button 2: Export History (CSV)
                    Button(
                        onClick = {
                            viewModel.exportHistoryCsv { csv ->
                                val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                BackupManager.shareContentAsFile(
                                    context = context,
                                    content = csv,
                                    fileName = "tasbih_history_$dateStr.csv",
                                    mimeType = "text/csv"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F8F4),
                            contentColor = if (isNightMode) GoldLight else EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_history_csv_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export History (CSV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button 3: Restore from JSON Backup
                    OutlinedButton(
                        onClick = {
                            openJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_backup_json_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore from Backup File (.json)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
}


@Composable
private fun ReminderRow(
    reminder: ReminderSetting,
    isNightMode: Boolean,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit
) {
    val amPm = if (reminder.hour < 12) "AM" else "PM"
    val displayHour = if (reminder.hour == 0) 12 else if (reminder.hour > 12) reminder.hour - 12 else reminder.hour
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, reminder.minute, amPm)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isNightMode) Color.White else Color(0xFF263238)
            )
            Text(
                text = reminder.subtitle,
                fontSize = 11.sp,
                color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Time Pill (clickable to change time)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9),
                modifier = Modifier
                    .clickable(onClick = onPickTime)
                    .testTag("time_pill_${reminder.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Set Time",
                        tint = if (isNightMode) GoldLight else EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode) GoldLight else EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GoldPrimary,
                    checkedTrackColor = EmeraldPrimary
                ),
                modifier = Modifier.testTag("reminder_switch_${reminder.id}")
            )
        }
    }
}
