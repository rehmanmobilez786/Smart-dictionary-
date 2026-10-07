package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddWordDialog
import com.example.ui.components.WordDetailSheet
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DictionaryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DictionaryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryApp(viewModel: DictionaryViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val isOnlineModeActive by viewModel.isOnlineModeActive.collectAsStateWithLifecycle()
    val isNetworkOnline by viewModel.isNetworkOnline.collectAsStateWithLifecycle()
    val isSearchingOnline by viewModel.isSearchingOnline.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val selectedWordDetail by viewModel.selectedWordDetail.collectAsStateWithLifecycle()
    val showAddWordDialog by viewModel.showAddWordDialog.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // Translation states
    val translateSourceText by viewModel.translateSourceText.collectAsStateWithLifecycle()
    val translateSourceLang by viewModel.translateSourceLang.collectAsStateWithLifecycle()
    val translateTargetLang by viewModel.translateTargetLang.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val translationResult by viewModel.translationResult.collectAsStateWithLifecycle()
    val translationHistory by viewModel.translationHistory.collectAsStateWithLifecycle()

    // Quiz states
    val quizQuestion by viewModel.quizQuestion.collectAsStateWithLifecycle()
    val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val quizStreak by viewModel.quizStreak.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button on secondary tabs
    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    // Snackbar notifications
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (currentTab == 1) "اردو انگلش مترجم" else "اردو انگلش ڈکشنری",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.toggleOnlineMode() },
                        modifier = Modifier.testTag("app_bar_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (isOnlineModeActive) Icons.Default.Cloud else Icons.Default.CloudOff,
                            contentDescription = "آن لائن / آف لائن موڈ",
                            tint = if (isOnlineModeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowAddWordDialog(true) },
                        modifier = Modifier.testTag("add_custom_word_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "نیا لفظ شامل کریں",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 0: لغت (Dictionary)
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "لغت") },
                    label = { Text("لغت (Dict)") },
                    modifier = Modifier.testTag("nav_tab_dictionary"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 1: مترجم (Translator)
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Translate, contentDescription = "مترجم") },
                    label = { Text("مترجم (Translate)") },
                    modifier = Modifier.testTag("nav_tab_translator"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 2: محفوظ الفاظ (Favorites)
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (favorites.isNotEmpty()) {
                                    Badge { Text("${favorites.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "محفوظ الفاظ"
                            )
                        }
                    },
                    label = { Text("محفوظ (Fav)") },
                    modifier = Modifier.testTag("nav_tab_favorites"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )

                // Tab 3: شعبہ جات (Categories)
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Category, contentDescription = "شعبہ جات") },
                    label = { Text("شعبہ جات") },
                    modifier = Modifier.testTag("nav_tab_categories"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 4: کوئز (Quiz)
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = {
                        viewModel.setTab(4)
                        viewModel.loadNewQuizQuestion()
                    },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "کوئز") },
                    label = { Text("کوئز (Quiz)") },
                    modifier = Modifier.testTag("nav_tab_quiz"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> DictionaryScreen(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        searchFilter = searchFilter,
                        onSearchFilterChange = { viewModel.setSearchFilter(it) },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { viewModel.selectCategory(it) },
                        categories = categories,
                        words = searchResults,
                        wordOfTheDay = wordOfTheDay,
                        isOnlineMode = isOnlineModeActive,
                        onToggleOnlineMode = { viewModel.toggleOnlineMode() },
                        isNetworkAvailable = isNetworkOnline,
                        isSearchingOnline = isSearchingOnline,
                        onSearchOnline = { viewModel.searchOnline(it) },
                        onWordClick = { viewModel.openWordDetail(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onSpeak = { viewModel.speakWord(it) }
                    )

                    1 -> TranslatorScreen(
                        sourceText = translateSourceText,
                        onSourceTextChange = { viewModel.updateTranslateSourceText(it) },
                        sourceLang = translateSourceLang,
                        targetLang = translateTargetLang,
                        onSwapLanguages = { viewModel.swapTranslationLanguages() },
                        isTranslating = isTranslating,
                        translationResult = translationResult,
                        onTranslate = { viewModel.performTranslation() },
                        onClear = { viewModel.clearTranslation() },
                        onSpeak = { viewModel.speakWord(it) },
                        isOnlineMode = isOnlineModeActive,
                        isNetworkAvailable = isNetworkOnline,
                        history = translationHistory,
                        onClearHistory = { viewModel.clearTranslationHistory() }
                    )

                    2 -> FavoritesScreen(
                        favorites = favorites,
                        onWordClick = { viewModel.openWordDetail(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onSpeak = { viewModel.speakWord(it) },
                        onStartQuiz = {
                            viewModel.setTab(4)
                            viewModel.loadNewQuizQuestion()
                        }
                    )

                    3 -> CategoriesScreen(
                        onSelectCategory = { cat ->
                            viewModel.selectCategory(cat)
                            viewModel.setTab(0)
                        }
                    )

                    4 -> QuizScreen(
                        question = quizQuestion,
                        score = quizScore,
                        streak = quizStreak,
                        onAnswer = { viewModel.answerQuiz(it) },
                        onNextQuestion = { viewModel.loadNewQuizQuestion() },
                        onSpeak = { viewModel.speakWord(it) }
                    )
                }
            }
        }
    }

    // Modal Word Detail Sheet
    selectedWordDetail?.let { word ->
        WordDetailSheet(
            word = word,
            onDismiss = { viewModel.closeWordDetail() },
            onFavoriteToggle = { viewModel.toggleFavorite(word) },
            onSpeak = { viewModel.speakWord(word.english) },
            onSaveToOffline = { viewModel.saveOnlineWordToLocal(it) }
        )
    }

    // Add Custom Word Dialog
    if (showAddWordDialog) {
        AddWordDialog(
            onDismiss = { viewModel.setShowAddWordDialog(false) },
            onSave = { en, ur, roman, pos, def, urDef, exEn, exUr, cat ->
                viewModel.addCustomWord(en, ur, roman, pos, def, urDef, exEn, exUr, cat)
            }
        )
    }
}
