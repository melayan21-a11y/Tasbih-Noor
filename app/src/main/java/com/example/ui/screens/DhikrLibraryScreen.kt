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
import com.example.data.model.DhikrItem
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.TasbihViewModel

@Composable
fun DhikrLibraryScreen(
    viewModel: TasbihViewModel,
    onDhikrSelected: (DhikrItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDhikrs by viewModel.allDhikrs.collectAsState()
    val selectedDhikr by viewModel.selectedDhikr.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Screen Header
            Text(
                text = "Adhkar & Prayers Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNightMode) GoldLight else EmeraldDeep,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            Text(
                text = "Select any prayer to recite on the beads with target guidance",
                fontSize = 13.sp,
                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF5A7268)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
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

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                            selectedLabelColor = if (isNightMode) Color(0xFF071914) else Color.White
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dhikr List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    val isCurrentlyActive = (selectedDhikr?.id == item.id) || (selectedDhikr == null && item == allDhikrs.firstOrNull())
                    DhikrCard(
                        item = item,
                        isActive = isCurrentlyActive,
                        isNightMode = isNightMode,
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
            }
        }

        // FAB to add Custom Dhikr
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
            contentColor = if (isNightMode) Color(0xFF071914) else Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_dhikr_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Dhikr")
        }

        // Add Custom Dhikr Dialog
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
    isNightMode: Boolean,
    onSelect: () -> Unit,
    onPlayAudio: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNightMode) {
                if (isActive) Color(0xFF0E382E) else Color(0xFF09241C)
            } else {
                if (isActive) Color(0xFFE8F5E9) else Color.White
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
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
            // Category & Target Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isNightMode) GoldPrimary.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNightMode) GoldLight else EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Target: ${item.defaultTarget}",
                        fontSize = 11.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
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
                            tint = if (isNightMode) GoldPrimary else EmeraldPrimary,
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

            // Arabic
            Text(
                text = item.arabicText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNightMode) GoldLight else EmeraldDeep,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Transliteration
            Text(
                text = item.transliteration,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isNightMode) Color.White else Color(0xFF263238)
            )

            // Translation
            Text(
                text = item.englishTranslation,
                fontSize = 12.sp,
                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF546E7A)
            )

            if (item.virtues.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isNightMode) Color(0xFF082018) else Color(0xFFF1F8F4)
                ) {
                    Text(
                        text = "✨ ${item.virtues}",
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0xFFA5D6A7) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Select for Tasbih
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) {
                        if (isNightMode) GoldPrimary else EmeraldPrimary
                    } else {
                        if (isNightMode) Color(0xFF133F34) else Color(0xFFE0EAE4)
                    },
                    contentColor = if (isActive) {
                        if (isNightMode) Color(0xFF071914) else Color.White
                    } else {
                        if (isNightMode) GoldLight else EmeraldDeep
                    }
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
