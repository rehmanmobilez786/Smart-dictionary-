package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.TranslationHistoryItem
import com.example.data.model.TranslationResult
import com.example.data.model.WordItem
import com.example.data.remote.OnlineDictionaryApi
import com.example.data.remote.TranslationApi
import com.example.data.repository.DictionaryRepository
import com.example.data.translation.TranslationManager
import com.example.util.NetworkMonitor
import com.example.util.TtsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String, val urduLabel: String) {
    ALL("All", "تمام (الفاظ)"),
    EN_TO_UR("English -> Urdu", "انگریزی سے اردو"),
    UR_TO_EN("Urdu -> English", "اردو سے انگریزی"),
    ROMAN("Roman Urdu", "رومن اردو")
}

data class QuizQuestion(
    val word: WordItem,
    val options: List<String>,
    val correctUrduMeaning: String,
    val selectedOption: String? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false
)

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val onlineApi = OnlineDictionaryApi.create()
    private val translationApi = TranslationApi.create()

    val repository = DictionaryRepository(database.wordDao(), onlineApi)
    val translationManager = TranslationManager(database.translationDao(), database.wordDao(), translationApi)
    val networkMonitor = NetworkMonitor(application)
    val ttsManager = TtsManager(application)

    // Current navigation tab: 0: Dictionary, 1: Translator, 2: Favorites, 3: Categories, 4: Quiz
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Search query & category filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("تمام (All)")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    // Online search toggle & status
    val isNetworkOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isCurrentlyConnected()
    )

    private val _isOnlineModeActive = MutableStateFlow(false)
    val isOnlineModeActive: StateFlow<Boolean> = _isOnlineModeActive.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    private val _onlineSearchResult = MutableStateFlow<WordItem?>(null)
    val onlineSearchResult: StateFlow<WordItem?> = _onlineSearchResult.asStateFlow()

    private val _onlineSearchError = MutableStateFlow<String?>(null)
    val onlineSearchError: StateFlow<String?> = _onlineSearchError.asStateFlow()

    // Word of the day
    private val _wordOfTheDay = MutableStateFlow<WordItem?>(null)
    val wordOfTheDay: StateFlow<WordItem?> = _wordOfTheDay.asStateFlow()

    // Detail Sheet word
    private val _selectedWordDetail = MutableStateFlow<WordItem?>(null)
    val selectedWordDetail: StateFlow<WordItem?> = _selectedWordDetail.asStateFlow()

    // Add Word Dialog
    private val _showAddWordDialog = MutableStateFlow(false)
    val showAddWordDialog: StateFlow<Boolean> = _showAddWordDialog.asStateFlow()

    // User Feedback Message (Snackbar)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Quiz state
    private val _quizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val quizQuestion: StateFlow<QuizQuestion?> = _quizQuestion.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizStreak = MutableStateFlow(0)
    val quizStreak: StateFlow<Int> = _quizStreak.asStateFlow()

    // ================= TRANSLATION STATE =================
    private val _translateSourceText = MutableStateFlow("")
    val translateSourceText: StateFlow<String> = _translateSourceText.asStateFlow()

    private val _translateSourceLang = MutableStateFlow("en") // "en" or "ur"
    val translateSourceLang: StateFlow<String> = _translateSourceLang.asStateFlow()

    private val _translateTargetLang = MutableStateFlow("ur") // "ur" or "en"
    val translateTargetLang: StateFlow<String> = _translateTargetLang.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translationResult = MutableStateFlow<TranslationResult?>(null)
    val translationResult: StateFlow<TranslationResult?> = _translationResult.asStateFlow()

    val translationHistory: StateFlow<List<TranslationHistoryItem>> =
        translationManager.translationHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorites & History from repository
    val favorites: StateFlow<List<WordItem>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchHistory: StateFlow<List<WordItem>> = repository.recentHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered words list
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<WordItem>> = combine(
        _searchQuery,
        _selectedCategory,
        _searchFilter
    ) { query, category, filter ->
        Triple(query, category, filter)
    }.flatMapLatest { (query, category, filter) ->
        val cat = if (category.contains("تمام") || category == "All") "All" else category
        repository.searchWordsByCategory(query, cat)
    }.combine(_searchFilter) { words, filter ->
        when (filter) {
            SearchFilter.ALL -> words
            SearchFilter.EN_TO_UR -> words.filter { it.english.contains(_searchQuery.value, ignoreCase = true) }
            SearchFilter.UR_TO_EN -> words.filter { it.urdu.contains(_searchQuery.value) }
            SearchFilter.ROMAN -> words.filter { it.romanUrdu.contains(_searchQuery.value, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.ensureDatabasePopulated()
            loadWordOfTheDay()
            loadNewQuizQuestion()
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _onlineSearchResult.value = null
        _onlineSearchError.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun toggleOnlineMode() {
        _isOnlineModeActive.value = !_isOnlineModeActive.value
        if (_isOnlineModeActive.value) {
            _snackbarMessage.value = "آن لائن موڈ فعال ہے (Online search & translation mode)"
        } else {
            _snackbarMessage.value = "آف لائن موڈ فعال ہے (Offline local database mode)"
        }
    }

    fun loadWordOfTheDay() {
        viewModelScope.launch {
            val word = repository.getWordOfDay()
            _wordOfTheDay.value = word
        }
    }

    fun openWordDetail(word: WordItem) {
        _selectedWordDetail.value = word
        viewModelScope.launch {
            repository.recordSearch(word)
        }
    }

    fun closeWordDetail() {
        _selectedWordDetail.value = null
    }

    fun toggleFavorite(word: WordItem) {
        viewModelScope.launch {
            repository.toggleFavorite(word)
            if (_selectedWordDetail.value?.id == word.id) {
                _selectedWordDetail.value = _selectedWordDetail.value?.copy(isFavorite = !word.isFavorite)
            }
            val msg = if (!word.isFavorite) "الفاظ فیورٹ میں شامل کر دیا گیا (Added to Favorites)" else "فیورٹ لسٹ سے خارج کر دیا گیا"
            _snackbarMessage.value = msg
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarMessage.value = "حالیہ تلاش کا ریکارڈ صاف کر دیا گیا"
        }
    }

    fun speakWord(text: String) {
        ttsManager.speak(text)
    }

    fun searchOnline(query: String) {
        if (query.isBlank()) return
        _isSearchingOnline.value = true
        _onlineSearchError.value = null
        _onlineSearchResult.value = null

        viewModelScope.launch {
            val result = repository.searchOnlineWord(query)
            _isSearchingOnline.value = false
            result.onSuccess { wordItem ->
                _onlineSearchResult.value = wordItem
                _selectedWordDetail.value = wordItem
            }.onFailure { err ->
                _onlineSearchError.value = err.message ?: "آن لائن معلومات حاصل نہ ہو سکیں"
                _snackbarMessage.value = "آن لائن تلاش ناکام: انٹرنیٹ چیک کریں یا آف لائن لغت دیکھیں"
            }
        }
    }

    fun saveOnlineWordToLocal(word: WordItem) {
        viewModelScope.launch {
            val newId = repository.addCustomWord(word)
            val saved = word.copy(id = newId.toInt(), isFavorite = true)
            _selectedWordDetail.value = saved
            _onlineSearchResult.value = null
            _snackbarMessage.value = "لفظ کامیابی سے آف لائن لغت اور فیورٹس میں محفوظ ہو گیا!"
        }
    }

    fun setShowAddWordDialog(show: Boolean) {
        _showAddWordDialog.value = show
    }

    fun addCustomWord(
        english: String,
        urdu: String,
        romanUrdu: String,
        partOfSpeech: String,
        definition: String,
        urduDefinition: String,
        exampleEn: String,
        exampleUr: String,
        category: String
    ) {
        viewModelScope.launch {
            val newWord = WordItem(
                english = english.trim(),
                urdu = urdu.trim(),
                romanUrdu = romanUrdu.trim(),
                partOfSpeech = partOfSpeech.trim(),
                definition = definition.trim(),
                urduDefinition = urduDefinition.trim(),
                exampleEn = exampleEn.trim(),
                exampleUr = exampleUr.trim(),
                category = category,
                isCustom = true
            )
            repository.addCustomWord(newWord)
            _showAddWordDialog.value = false
            _snackbarMessage.value = "نیا لفظ کامیابی سے شامل کر دیا گیا!"
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    // ================= TRANSLATION ACTIONS =================
    fun updateTranslateSourceText(text: String) {
        _translateSourceText.value = text
    }

    fun swapTranslationLanguages() {
        val oldSource = _translateSourceLang.value
        val oldTarget = _translateTargetLang.value
        _translateSourceLang.value = oldTarget
        _translateTargetLang.value = oldSource

        val currentResult = _translationResult.value
        if (currentResult != null && currentResult.translatedText.isNotBlank()) {
            _translateSourceText.value = currentResult.translatedText
            _translationResult.value = currentResult.copy(
                sourceText = currentResult.translatedText,
                translatedText = currentResult.sourceText,
                sourceLang = oldTarget,
                targetLang = oldSource
            )
        }
    }

    fun performTranslation() {
        val text = _translateSourceText.value.trim()
        if (text.isBlank()) return

        _isTranslating.value = true
        viewModelScope.launch {
            val preferOnline = _isOnlineModeActive.value && isNetworkOnline.value
            val res = translationManager.translate(
                text = text,
                sourceLang = _translateSourceLang.value,
                targetLang = _translateTargetLang.value,
                preferOnline = preferOnline
            )
            _translationResult.value = res
            _isTranslating.value = false
        }
    }

    fun clearTranslation() {
        _translateSourceText.value = ""
        _translationResult.value = null
    }

    fun toggleFavoriteTranslation(item: TranslationHistoryItem) {
        viewModelScope.launch {
            translationManager.toggleFavorite(item)
        }
    }

    fun clearTranslationHistory() {
        viewModelScope.launch {
            translationManager.clearHistory()
            _snackbarMessage.value = "ترجمے کی تاریخ صاف کر دی گئی"
        }
    }

    // Quiz logic
    fun loadNewQuizQuestion() {
        viewModelScope.launch {
            val words = repository.getRandomQuizWords(4)
            if (words.size >= 4) {
                val targetWord = words.first()
                val options = words.map { it.urdu }.shuffled()
                _quizQuestion.value = QuizQuestion(
                    word = targetWord,
                    options = options,
                    correctUrduMeaning = targetWord.urdu
                )
            }
        }
    }

    fun answerQuiz(selectedOption: String) {
        val current = _quizQuestion.value ?: return
        if (current.isAnswered) return

        val isCorrect = selectedOption == current.correctUrduMeaning
        _quizQuestion.value = current.copy(
            selectedOption = selectedOption,
            isAnswered = true,
            isCorrect = isCorrect
        )

        if (isCorrect) {
            _quizScore.value += 10
            _quizStreak.value += 1
        } else {
            _quizStreak.value = 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
