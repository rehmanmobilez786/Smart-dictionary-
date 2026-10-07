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
import com.example.data.model.WordItem
import com.example.ui.components.WordCard
import com.example.ui.components.WordOfTheDayCard
import com.example.ui.viewmodel.SearchFilter

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
    modifier: Modifier = Modifier
) {
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
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "صاف کریں"
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
