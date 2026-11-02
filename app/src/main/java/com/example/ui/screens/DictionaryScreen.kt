package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.WordItem
import com.example.ui.components.WordCard
import com.example.ui.components.WordOfTheDayCard
import com.example.ui.viewmodel.SearchFilter
import com.example.util.OcrHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchFilter: SearchFilter,
    onSearchFilterChange: (SearchFilter) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    categories: List<String>,
    words: List<WordItem>,
    wordOfTheDay: WordItem?,
    isOnlineMode: Boolean,
    onToggleOnlineMode: () -> Unit,
    isNetworkAvailable: Boolean,
    isSearchingOnline: Boolean,
    onSearchOnline: (String) -> Unit,
    onWordClick: (WordItem) -> Unit,
    onFavoriteToggle: (WordItem) -> Unit,
    onSpeak: (String) -> Unit,
    scannedWords: List<String> = emptyList(),
    onSelectScannedWord: (String) -> Unit = {},
    onClearScannedWords: () -> Unit = {},
    onOcrScannedText: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isOcrProcessing by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isOcrProcessing = true
            coroutineScope.launch {
                val res = OcrHelper.recognizeTextFromBitmap(bitmap)
                isOcrProcessing = false
                res.onSuccess { text ->
                    onOcrScannedText(text)
                }.onFailure {
                    Toast.makeText(context, "تصویر سے متن پڑھنے میں خرابی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "کیمرہ کی اجازت درکار ہے (Camera permission required)", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isOcrProcessing = true
            coroutineScope.launch {
                val res = OcrHelper.recognizeTextFromUri(context, uri)
                isOcrProcessing = false
                res.onSuccess { text ->
                    onOcrScannedText(text)
                }.onFailure {
                    Toast.makeText(context, "گیلری سے تصویر پڑھنے میں خرابی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val categoryList = listOf("تمام (All)") + categories.filter { it.isNotBlank() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dictionary_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Search & Mode Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Online / Offline Status & Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isNetworkAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isNetworkAvailable) Icons.Default.Cloud else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isNetworkAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNetworkAvailable) "انٹرنیٹ منسلک ہے (Online)" else "آف لائن لغت موڈ (Offline)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNetworkAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Online Mode Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnlineMode) "آن لائن سرچ" else "آف لائن سرچ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isOnlineMode,
                            onCheckedChange = { onToggleOnlineMode() },
                            modifier = Modifier.testTag("online_mode_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input"),
                    placeholder = {
                        Text("انگریزی یا اردو لفظ تلاش کریں / Search English or اردو...")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "تلاش",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(36.dp).testTag("clear_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "صاف کریں"
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                modifier = Modifier.size(36.dp).testTag("dict_camera_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "کیمرہ سے اسکین کریں (Scan from Camera)",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(36.dp).testTag("dict_gallery_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "گیلری تصویر سے اسکین کریں (Scan from Gallery)",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                if (isOcrProcessing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تصویر سے الفاظ پڑھے جا رہے ہیں... (Scanning image...)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                if (scannedWords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تصویر سے حاصل شدہ الفاظ (Scanned Words - Tap to Search):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                                IconButton(
                                    onClick = onClearScannedWords,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "بند کریں",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                scannedWords.forEach { word ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.clickable { onSelectScannedWord(word) }
                                    ) {
                                        Text(
                                            text = word,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Filter Direction Chips (English -> Urdu, Urdu -> English, Roman)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchFilter.values().forEach { filter ->
                        FilterChip(
                            selected = searchFilter == filter,
                            onClick = { onSearchFilterChange(filter) },
                            label = { Text(filter.urduLabel, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("filter_chip_${filter.name}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryList.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategoryChange(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }
            }
        }

        // Word of the Day (shown when user is not actively typing a query)
        if (searchQuery.isEmpty() && wordOfTheDay != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    WordOfTheDayCard(
                        word = wordOfTheDay,
                        onClick = { onWordClick(wordOfTheDay) },
                        onSpeak = { onSpeak(wordOfTheDay.english) },
                        onFavoriteToggle = { onFavoriteToggle(wordOfTheDay) }
                    )
                }
            }
        }

        // Online Live Search Action Card (if Online mode is toggled or when searching)
        if (isOnlineMode && searchQuery.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "آن لائن تلاش (Live Dictionary)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            )
                            Text(
                                text = "انٹرنیٹ کے ذریعے لفظ \"$searchQuery\" کی تفصیلی تعریف دریافت کریں",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isSearchingOnline) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Button(
                                onClick = { onSearchOnline(searchQuery) },
                                modifier = Modifier.testTag("online_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سرچ کریں")
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Results Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "جامع لغت کے الفاظ (${words.size})" else "تلاش کے نتائج (${words.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = selectedCategory,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Words List
        if (words.isNotEmpty()) {
            items(words, key = { it.id }) { word ->
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    WordCard(
                        word = word,
                        onClick = { onWordClick(word) },
                        onFavoriteToggle = { onFavoriteToggle(word) },
                        onSpeak = { onSpeak(word.english) }
                    )
                }
            }
        } else {
            // Empty State
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "کوئی لفظ نہیں ملا / No words found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "دیگر ہجے آزمائیں یا آن لائن موڈ استعمال کر کے تلاش کریں۔",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isNotBlank() && isNetworkAvailable) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onSearchOnline(searchQuery) },
                            modifier = Modifier.testTag("empty_state_online_search_btn")
                        ) {
                            Icon(Icons.Default.TravelExplore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("آن لائن لغت میں \"$searchQuery\" تلاش کریں")
                        }
                    }
                }
            }
        }
    }
}
