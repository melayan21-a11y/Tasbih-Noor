package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.AthkarRepository
import com.example.data.model.DhikrItem
import com.example.ui.theme.ArabicCalligraphyFontFamily
import com.example.ui.theme.DayEmeraldDeep
import com.example.ui.theme.DayGoldAccent
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.data.model.AthkarType
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.LocalTasbihPalette
import com.example.ui.viewmodel.TasbihViewModel

@Composable
fun DhikrLibraryScreen(
    viewModel: TasbihViewModel,
    onDhikrSelected: (DhikrItem) -> Unit,
    onOpenAthkarReader: (AthkarType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalTasbihPalette.current
    val allDhikrs by viewModel.allDhikrs.collectAsState()
    val selectedDhikr by viewModel.selectedDhikr.collectAsState()
    val isNightMode = palette.isDark
    val headingColor = if (isNightMode) palette.accentLight else palette.textPrimary

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "After Salah", "Daily", "Forgiveness", "Quranic", "Custom")

    val filteredList = allDhikrs.distinctBy { it.transliteration.trim().lowercase() }.filter { item ->
        val matchesCategory = if (selectedCategory == "All") {
            true
        } else if (selectedCategory == "Custom") {
            item.isCustom
        } else {
            item.category.equals(selectedCategory, ignoreCase = true)
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            item.transliteration.contains(searchQuery, ignoreCase = true) ||
            item.englishTranslation.contains(searchQuery, ignoreCase = true) ||
            item.arabicText.contains(searchQuery)
        }

        matchesCategory && matchesSearch
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
            alpha = if (isNightMode) 0.25f else 0.17f
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Screen Header
            item {
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                    Text(
                        text = "Adhkar & Prayers Library",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = headingColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select any prayer to recite on the beads with target guidance",
                        fontSize = 13.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // 2. Featured Daily Booklets (Morning & Evening Athkar with Full Duas & Counter)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Morning Adhkar Card
                    Card(
                        onClick = { onOpenAthkarReader(AthkarType.MORNING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("morning_adhkar_booklet_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.surface.copy(alpha = 0.94f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            palette.accent.copy(alpha = 0.55f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌅", fontSize = 18.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = palette.primaryContainer
                                ) {
                                    Text(
                                        text = "${AthkarRepository.morningAthkar.size} Duas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headingColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Morning Adhkar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = headingColor
                            )
                            Text(
                                text = "أَذْكَارُ الصَّبَاحِ",
                                fontFamily = ArabicCalligraphyFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = if (isNightMode) palette.textPrimary else palette.accent
                            )
                        }
                    }

                    // Evening Adhkar Card
                    Card(
                        onClick = { onOpenAthkarReader(AthkarType.EVENING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("evening_adhkar_booklet_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.surface.copy(alpha = 0.94f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            palette.cardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌇", fontSize = 18.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = palette.surfaceVariant
                                ) {
                                    Text(
                                        text = "${AthkarRepository.eveningAthkar.size} Duas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headingColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Evening Adhkar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = headingColor
                            )
                            Text(
                                text = "أَذْكَارُ الْمَسَاءِ",
                                fontFamily = ArabicCalligraphyFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = if (isNightMode) palette.textPrimary else palette.accent
                            )
                        }
                    }
                }
            }

            // 3. Chained After-Salah Tasbih Set (33-33-33-1 Set)
            item {
                val isSalahSetMode by viewModel.isSalahSetMode.collectAsState()
                val salahSetStage by viewModel.salahSetStage.collectAsState()

                Card(
                    onClick = {
                        viewModel.startSalahSetMode()
                        val subhanAllah = allDhikrs.find { it.id == 1L || it.transliteration.contains("SubhanAllah", ignoreCase = true) } ?: allDhikrs.firstOrNull()
                        if (subhanAllah != null) {
                            onDhikrSelected(subhanAllah)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salah_set_library_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSalahSetMode) palette.primaryContainer else palette.surface.copy(alpha = 0.94f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSalahSetMode) 2.dp else 1.dp,
                        if (isSalahSetMode) palette.accent else palette.cardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🕌", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "After-Salah Tasbih Set",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headingColor
                                    )
                                    Text(
                                        text = "Prophetic 100 Praises Chain • Sunnah",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSalahSetMode) palette.accent else palette.surfaceVariant
                            ) {
                                Text(
                                    text = if (isSalahSetMode) "ACTIVE" else "100 TOTAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSalahSetMode) palette.onPrimaryColor else headingColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "33 SubhanAllah",
                                fontSize = 11.sp,
                                fontWeight = if (isSalahSetMode && salahSetStage.stepIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSalahSetMode && salahSetStage.stepIndex == 1) headingColor else palette.textPrimary.copy(alpha = 0.85f)
                            )
                            Text(text = "➔", fontSize = 11.sp, color = palette.accent)
                            Text(
                                text = "33 Alhamdulillah",
                                fontSize = 11.sp,
                                fontWeight = if (isSalahSetMode && salahSetStage.stepIndex == 2) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSalahSetMode && salahSetStage.stepIndex == 2) headingColor else palette.textPrimary.copy(alpha = 0.85f)
                            )
                            Text(text = "➔", fontSize = 11.sp, color = palette.accent)
                            Text(
                                text = "33 Allahu Akbar",
                                fontSize = 11.sp,
                                fontWeight = if (isSalahSetMode && salahSetStage.stepIndex == 3) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSalahSetMode && salahSetStage.stepIndex == 3) headingColor else palette.textPrimary.copy(alpha = 0.85f)
                            )
                            Text(text = "➔", fontSize = 11.sp, color = palette.accent)
                            Text(
                                text = "1 Seal",
                                fontSize = 11.sp,
                                fontWeight = if (isSalahSetMode && salahSetStage.stepIndex == 4) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSalahSetMode && salahSetStage.stepIndex == 4) headingColor else palette.textPrimary.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.startSalahSetMode()
                                val subhanAllah = allDhikrs.find { it.id == 1L || it.transliteration.contains("SubhanAllah", ignoreCase = true) } ?: allDhikrs.firstOrNull()
                                if (subhanAllah != null) {
                                    onDhikrSelected(subhanAllah)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSalahSetMode) palette.accent else palette.primaryContainer,
                                contentColor = if (isSalahSetMode) palette.onPrimaryColor else headingColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("start_salah_set_library_button")
                        ) {
                            Text(
                                text = if (isSalahSetMode) "Continue Chained Recitation on Beads ➔" else "Start Chained After-Salah Set (100) ➔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search adhkar, virtues, translation...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dhikr_search_field")
                )
            }

            // 5. Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accent,
                                selectedLabelColor = palette.onPrimaryColor
                            ),
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }

            // 6. Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All") "All Supplications" else "$selectedCategory Prayers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = headingColor
                    )
                    Text(
                        text = "${filteredList.size} prayers",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // 7. Individual Dhikr Items
            items(filteredList, key = { it.id }) { item ->
                val isCurrentlyActive = (selectedDhikr?.id == item.id) || (selectedDhikr == null && item == allDhikrs.firstOrNull())
                DhikrCard(
                    item = item,
                    isActive = isCurrentlyActive,
                    palette = palette,
                    onSelect = {
                        viewModel.selectDhikr(item)
                        onDhikrSelected(item)
                    },
                    onPlayAudio = {
                        viewModel.audioHapticManager.speakRecitation(item.arabicText, item.transliteration)
                    },
                    onDelete = if (item.isCustom) {
                        { viewModel.deleteDhikr(item) }
                    } else null
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to add Custom Dhikr
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = palette.accent,
            contentColor = palette.onPrimaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_dhikr_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Dhikr")
        }

        if (showAddDialog) {
            AddCustomDhikrDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { arabic, transliteration, translation, target, category ->
                    viewModel.addCustomDhikr(arabic, transliteration, translation, target, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun DhikrCard(
    item: DhikrItem,
    isActive: Boolean,
    palette: AppThemeStyle,
    onSelect: () -> Unit,
    onPlayAudio: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val headingColor = if (palette.isDark) palette.accentLight else palette.textPrimary
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) palette.primaryContainer.copy(alpha = if (palette.isDark) 0.94f else 0.72f) else palette.surface.copy(alpha = 0.94f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) palette.accent else palette.cardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.accent.copy(alpha = if (palette.isDark) 0.22f else 0.12f)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (palette.isDark) palette.accentLight else palette.accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Target: ${item.defaultTarget}",
                        fontSize = 11.sp,
                        color = palette.textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen to audio",
                            tint = palette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete custom dhikr",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.arabicText,
                fontFamily = ArabicCalligraphyFontFamily,
                fontSize = 23.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 36.sp,
                color = headingColor,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.transliteration,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )

            Text(
                text = item.englishTranslation,
                fontSize = 12.sp,
                color = palette.textSecondary
            )

            if (item.virtues.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = "✨ ${item.virtues}",
                        fontSize = 11.sp,
                        color = if (palette.isDark) palette.accentLight else palette.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) palette.accent else palette.surfaceVariant,
                    contentColor = if (isActive) palette.onPrimaryColor else headingColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_dhikr_btn_${item.id}")
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Currently Reciting", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Select for Prayer Beads", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AddCustomDhikrDialog(
    onDismiss: () -> Unit,
    onConfirm: (arabic: String, transliteration: String, translation: String, target: Int, category: String) -> Unit
) {
    var arabic by remember { mutableStateOf("") }
    var transliteration by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("33") }
    var category by remember { mutableStateOf("Custom") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Dhikr / Dua", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text("Arabic Text (optional)") },
                    placeholder = { Text("e.g. رَبِّ زِدْنِي عِلْمًا") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transliteration,
                    onValueChange = { transliteration = it },
                    label = { Text("Transliteration / Title *") },
                    placeholder = { Text("e.g. Rabbi zidni 'ilma") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text("English Translation") },
                    placeholder = { Text("e.g. My Lord, increase me in knowledge") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Count (e.g. 33, 99, 100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transliteration.isNotBlank()) {
                        val target = targetText.toIntOrNull() ?: 33
                        onConfirm(arabic, transliteration, translation, target, category)
                    }
                },
                enabled = transliteration.isNotBlank()
            ) {
                Text("Save Dhikr")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
