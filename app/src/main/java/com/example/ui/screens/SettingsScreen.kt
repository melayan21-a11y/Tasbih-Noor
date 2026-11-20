package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.backup.BackupManager
import com.example.notifications.ReminderSetting
import com.example.ui.components.BeadCraftPreview
import com.example.ui.components.BeadDesign
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.LocalTasbihPalette
import com.example.ui.viewmodel.TasbihViewModel
import com.example.util.HapticIntensity
import com.example.util.SoundEffectType
import com.example.util.SpeechCadence
import com.example.util.VoiceQualityTier
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: TasbihViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalTasbihPalette.current
    val appThemeStyle by viewModel.appThemeStyle.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val beadMaterial by viewModel.beadMaterial.collectAsState()
    val beadDesign by viewModel.beadDesign.collectAsState()
    val beadTension by viewModel.beadTension.collectAsState()
    val singleBeadLock by viewModel.singleBeadLock.collectAsState()
    val soundType by viewModel.soundType.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val milestoneChimeEnabled by viewModel.milestoneChimeEnabled.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val speechCadence by viewModel.speechCadence.collectAsState()
    val preferArabicVoice by viewModel.preferArabicVoice.collectAsState()

    val cardHeadingColor = if (palette.isDark) palette.accentLight else palette.textPrimary

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
            .background(palette.background)
    ) {
        Image(
            painter = painterResource(id = palette.backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (palette.isDark) 0.24f else 0.16f
        )

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
                        text = "Sanctuary & Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = cardHeadingColor
                    )
                    Text(
                        text = "Choose your sanctuary atmosphere, prayer bead craft, sounds, and daily reminders",
                        fontSize = 13.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // Sanctuary Color Themes & Architectural Atmosphere Gallery Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sanctuary_theme_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Sanctuary App Themes",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sanctuary App Themes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardHeadingColor
                                )
                                Text(
                                    text = "Full-screen sacred artwork, braided cord & custom palettes across all tabs",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val themePairs = AppThemeStyle.entries.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            themePairs.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    pair.forEach { style ->
                                        SanctuaryThemePreviewCard(
                                            style = style,
                                            isSelected = appThemeStyle == style,
                                            fallbackBorderColor = palette.cardBorder,
                                            onClick = { viewModel.setThemeStyle(style) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bead Material & Islamic Bead Craft Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bead_material_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
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
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prayer Bead Stone & Material",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Natural stones and woods rendered with 3D sheen and matching silk tassel.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BeadMaterial.entries.forEach { mat ->
                                val isSelected = beadMaterial == mat
                                val shortLabel = when (mat) {
                                    BeadMaterial.OLIVE_WOOD -> "Olive Wood"
                                    BeadMaterial.TIGER_EYE -> "Tiger's Eye"
                                    BeadMaterial.ROYAL_JADE -> "Royal Jade"
                                    BeadMaterial.PEARL -> "Pearl"
                                    BeadMaterial.LAPIS_LAZULI -> "Lapis"
                                    BeadMaterial.BLACK_ONYX -> "Black Onyx"
                                    BeadMaterial.AMBER_HONEY -> "Honey Amber"
                                }
                                Column(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) {
                                                palette.primaryContainer.copy(alpha = if (isNightMode) 0.9f else 0.65f)
                                            } else palette.surfaceVariant.copy(alpha = 0.45f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) palette.accent else palette.cardBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.setBeadMaterial(mat) }
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                        .testTag("bead_material_${mat.name.lowercase()}"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    BeadCraftPreview(
                                        material = mat,
                                        design = beadDesign,
                                        isNightMode = isNightMode,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = shortLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected && isNightMode) palette.accentLight else palette.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Islamic Bead Craft",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Islamic Bead Design & Engraving",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Carve timeless Islamic motifs and gold inlays onto every bead in your strand.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.testTag("bead_design_card"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BeadDesign.entries.forEach { design ->
                                val isSelected = beadDesign == design
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) {
                                                palette.primaryContainer.copy(alpha = if (isNightMode) 0.9f else 0.65f)
                                            } else Color.Transparent
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) palette.accent else palette.cardBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.setBeadDesign(design) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("bead_design_${design.name.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BeadCraftPreview(
                                        material = beadMaterial,
                                        design = design,
                                        isNightMode = isNightMode,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = design.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected && isNightMode) palette.accentLight else palette.textPrimary
                                        )
                                        Text(
                                            text = design.subtitle,
                                            fontSize = 11.sp,
                                            color = palette.textSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = palette.accent,
                                            modifier = Modifier.size(16.dp)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Reminder Notifications Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminders_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Daily Reminders",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Reminder Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Receive gentle alerts to maintain your daily morning, evening, and nighttime adhkar routines.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )

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
                                    color = palette.cardBorder.copy(alpha = 0.55f)
                                )
                            }
                            ReminderRow(
                                reminder = reminder,
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

            // Bead String Tension & Sensitivity Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tension_sensitivity_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
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
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "String Tension & Drag Sensitivity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Customize string resistance and prevent accidentally scrolling multiple beads.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.surfaceVariant.copy(alpha = 0.7f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1-Bead Pull Lock",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cardHeadingColor
                                )
                                Text(
                                    text = "Stops you from scrolling 3 to 5 beads by accident. Each drag stroke advances exactly 1 bead.",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Switch(
                                checked = singleBeadLock,
                                onCheckedChange = { viewModel.setSingleBeadLock(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = palette.onPrimaryColor,
                                    checkedTrackColor = palette.accent
                                ),
                                modifier = Modifier.testTag("single_bead_lock_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Cord Resistance Level",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
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
                                            if (isSelected) palette.primaryContainer.copy(alpha = if (isNightMode) 0.9f else 0.65f) else Color.Transparent
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) palette.accent else palette.cardBorder,
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
                                            color = if (isSelected) cardHeadingColor else palette.textPrimary
                                        )
                                        Text(
                                            text = t.description,
                                            fontSize = 11.sp,
                                            color = palette.textSecondary
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = palette.accent,
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
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Sound Effect",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bead Tap Acoustic Sound",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
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
                                    color = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .clickable { viewModel.setSoundType(type) }
                                        .testTag("sound_chip_${type.name.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) palette.onPrimaryColor else palette.textPrimary.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Haptic Feedback",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Haptic Vibration Response",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
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
                                    color = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setHapticIntensity(intensity) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) palette.onPrimaryColor else palette.textPrimary.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lap & Milestone Chimes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cardHeadingColor
                                )
                                Text(
                                    text = "Play a gentle harmonic bell chord when completing a target lap, playlist, or daily goal.",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = milestoneChimeEnabled,
                                onCheckedChange = { viewModel.setMilestoneChimeEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = palette.onPrimaryColor,
                                    checkedTrackColor = palette.accent
                                ),
                                modifier = Modifier.testTag("milestone_chime_switch")
                            )
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
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Recitation Voice",
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recitation Voice & Natural Audio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardHeadingColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.surfaceVariant.copy(alpha = 0.75f),
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
                                        color = cardHeadingColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (voiceState.qualityTier) {
                                            VoiceQualityTier.HD_NEURAL -> palette.accent
                                            VoiceQualityTier.STANDARD -> Color(0xFF455A64)
                                            VoiceQualityTier.SYNTHESIZER_FALLBACK -> Color(0xFFE65100)
                                        }
                                    ) {
                                        Text(
                                            text = voiceState.qualityTier.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (voiceState.qualityTier == VoiceQualityTier.HD_NEURAL) palette.onPrimaryColor else Color.White,
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

                        Text(
                            text = "Recitation Language",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
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
                                    color = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setPreferArabicVoice(isArabic) }
                                        .testTag(if (isArabic) "voice_lang_arabic" else "voice_lang_english")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) palette.onPrimaryColor else palette.textPrimary.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 9.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Recitation Cadence",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
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
                                    color = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setSpeechCadence(cadence) }
                                        .testTag("cadence_${cadence.name.lowercase()}")
                                ) {
                                    Text(
                                        text = "${cadence.label} (${cadence.rate}x)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) palette.onPrimaryColor else palette.textPrimary.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
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
                                color = palette.textPrimary
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
                                    color = if (isAuto) palette.primaryContainer else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isAuto) palette.accent else palette.cardBorder
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.selectTtsVoice(null) }
                                        .testTag("voice_option_auto")
                                ) {
                                    Text(
                                        text = "Auto Best HD",
                                        fontSize = 11.sp,
                                        fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                        color = cardHeadingColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                voiceState.availableVoices.take(6).forEach { option ->
                                    val selected = voiceState.selectedVoiceName == option.name
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) palette.primaryContainer else Color.Transparent,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selected) palette.accent else palette.cardBorder
                                        ),
                                        modifier = Modifier.clickable { viewModel.selectTtsVoice(option.name) }
                                    ) {
                                        Text(
                                            text = option.displayLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = palette.textPrimary,
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
                                    containerColor = palette.accent,
                                    contentColor = palette.onPrimaryColor
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

            // Keep Screen Awake During Dhikr Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keep_awake_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
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
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Keep Screen Awake",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardHeadingColor
                                )
                                Text(
                                    text = "Keeps the screen lit while reciting so your phone never dims or locks during dhikr",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = keepScreenAwake,
                            onCheckedChange = { viewModel.setKeepScreenAwake(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.onPrimaryColor,
                                checkedTrackColor = palette.accent
                            ),
                            modifier = Modifier.testTag("keep_awake_switch")
                        )
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
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.cardBorder)
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
                                tint = palette.accent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Data Backup & Export",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardHeadingColor
                                )
                                Text(
                                    text = "Safeguard your custom dhikrs and prayer logs locally, or export to spreadsheet",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.exportBackupJson { json ->
                                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
                                    BackupManager.shareContentAsFile(
                                        context = context,
                                        content = json,
                                        fileName = "tasbih_noor_backup_$dateStr.json",
                                        mimeType = "application/json"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primaryContainer,
                                contentColor = cardHeadingColor
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

                        Button(
                            onClick = {
                                viewModel.exportHistoryCsv { csv ->
                                    val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
                                    BackupManager.shareContentAsFile(
                                        context = context,
                                        content = csv,
                                        fileName = "tasbih_history_$dateStr.csv",
                                        mimeType = "text/csv"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.surfaceVariant,
                                contentColor = cardHeadingColor
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
private fun SanctuaryThemePreviewCard(
    style: AppThemeStyle,
    isSelected: Boolean,
    fallbackBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(152.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(style.background)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) style.accent else fallbackBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("theme_chip_${style.name.lowercase()}")
    ) {
        // Sanctuary artwork crop filling card
        Image(
            painter = painterResource(id = style.backgroundRes),
            contentDescription = "${style.title} sanctuary theme",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.92f
        )

        // Contrast scrim rising from the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.14f),
                            style.background.copy(alpha = if (style.isDark) 0.56f else 0.42f),
                            style.background.copy(alpha = if (style.isDark) 0.94f else 0.92f)
                        )
                    )
                )
        )

        // Top bar: Dark/Light pill + active checkmark or mini palette swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 9.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = style.background.copy(alpha = 0.78f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, style.accent.copy(alpha = 0.55f))
            ) {
                Text(
                    text = if (style.isDark) "Dark" else "Light",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textPrimary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = style.accent,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected theme",
                            tint = style.onPrimaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(style.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(style.accent)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }
        }

        // Bottom info & 3-swatch signature bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Text(
                text = style.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (style.isDark) style.accentLight else style.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = style.subtitle,
                fontSize = 10.sp,
                color = style.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(modifier = Modifier.weight(1.2f).fillMaxSize().background(style.surface))
                Box(modifier = Modifier.weight(1.4f).fillMaxSize().background(style.accent))
                Box(modifier = Modifier.weight(1f).fillMaxSize().background(style.accentLight))
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: ReminderSetting,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit
) {
    val palette = LocalTasbihPalette.current
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
                color = palette.textPrimary
            )
            Text(
                text = reminder.subtitle,
                fontSize = 11.sp,
                color = palette.textSecondary,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.primaryContainer,
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
                        tint = if (palette.isDark) palette.accentLight else palette.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (palette.isDark) palette.accentLight else palette.accent
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.onPrimaryColor,
                    checkedTrackColor = palette.accent
                ),
                modifier = Modifier.testTag("reminder_switch_${reminder.id}")
            )
        }
    }
}
