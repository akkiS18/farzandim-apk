package uz.farzandim.parent.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.data.local.entity.*
import uz.farzandim.parent.data.model.*
import uz.farzandim.parent.data.repository.ParentRepository
import uz.farzandim.parent.data.sync.SyncManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ParentViewModel(
    private val repository: ParentRepository,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _networkChildren = MutableStateFlow<List<ChildEntity>>(emptyList())

    val children: StateFlow<List<ChildEntity>> = combine(
        repository.childrenFlow.catch { e ->
            android.util.Log.e("ParentViewModel", "Error in childrenFlow", e)
            emit(emptyList())
        },
        _networkChildren
    ) { dbList, netList ->
        if (dbList.isNotEmpty()) dbList else netList
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedChildId = MutableStateFlow<Int?>(null)
    val selectedChildId: StateFlow<Int?> = _selectedChildId.asStateFlow()

    val selectedChild: StateFlow<ChildEntity?> = combine(children, _selectedChildId) { list, id ->
        if (id != null) list.find { it.id == id } else list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Auto-select first child if none selected
    init {
        viewModelScope.launch {
            try {
                children.collect { list ->
                    if (_selectedChildId.value == null && list.isNotEmpty()) {
                        _selectedChildId.value = list.first().id
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("ParentViewModel", "Error in children collect", t)
            }
        }
        refreshInitialData()
        try {
            syncManager.schedulePeriodicSync()
        } catch (t: Throwable) {
            android.util.Log.e("ParentViewModel", "Failed to schedule sync", t)
        }
    }

    private val _networkGrades = MutableStateFlow<List<ParentGradeEntity>>(emptyList())

    val grades: StateFlow<List<ParentGradeEntity>> = _selectedChildId
        .filterNotNull()
        .flatMapLatest { studentId ->
            combine(
                repository.getGradesFlow(studentId).catch { emit(emptyList()) },
                _networkGrades
            ) { dbList, netList ->
                if (netList.isNotEmpty()) netList else dbList
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unapprovedGradesCount: StateFlow<Int> = grades.map { list ->
        list.count { !it.approvedByParent }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val todayGrades: StateFlow<List<ParentGradeEntity>> = grades.map { list ->
        val today = todayDateStr
        list.filter { it.gradeDate == today }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val schedule: StateFlow<List<ParentScheduleEntity>> = selectedChild
        .filterNotNull()
        .flatMapLatest { child ->
            val cId = child.classId ?: 0
            repository.getScheduleFlow(cId).catch { emit(emptyList()) }
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val holidays: StateFlow<List<HolidayEntity>> = repository.holidaysFlow
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.announcementsFlow
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _comments = MutableStateFlow<List<CommentItemDto>>(emptyList())
    val comments: StateFlow<List<CommentItemDto>> = _comments.asStateFlow()

    private val _nextCharge = MutableStateFlow<NextChargeDto?>(null)
    val nextCharge: StateFlow<NextChargeDto?> = _nextCharge.asStateFlow()

    private val _balanceHistory = MutableStateFlow<List<TransactionDto>>(emptyList())
    val balanceHistory: StateFlow<List<TransactionDto>> = _balanceHistory.asStateFlow()

    private val _dailyMenu = MutableStateFlow<DailyMenuResponse?>(null)
    val dailyMenu: StateFlow<DailyMenuResponse?> = _dailyMenu.asStateFlow()

    val clubs: StateFlow<List<ClubEntity>> = _selectedChildId
        .filterNotNull()
        .flatMapLatest { studentId ->
            repository.getClubsFlow(studentId).catch { emit(emptyList()) }
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val books: StateFlow<List<BookEntity>> = repository.booksFlow
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _aiReport = MutableStateFlow<AiReportDto?>(null)
    val aiReport: StateFlow<AiReportDto?> = _aiReport.asStateFlow()

    fun selectChild(studentId: Int) {
        _selectedChildId.value = studentId
        _networkGrades.value = emptyList()
        viewModelScope.launch {
            try {
                repository.fetchGrades(studentId).onSuccess { list ->
                    _networkGrades.value = list
                }
                repository.getNextCharge(studentId).onSuccess {
                    _nextCharge.value = it
                }
                val child = children.value.find { it.id == studentId }
                if (child?.classId != null) {
                    repository.fetchSchedule(child.classId)
                }
            } catch (t: Throwable) {
                android.util.Log.e("ParentViewModel", "Error in selectChild", t)
            }
        }
    }

    fun fetchComments() {
        viewModelScope.launch {
            try {
                repository.getCommentsFeed().onSuccess { list ->
                    _comments.value = list
                }
            } catch (t: Throwable) {
                android.util.Log.e("ParentViewModel", "Error in fetchComments", t)
            }
        }
    }

    fun refreshInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchChildren().onSuccess { list ->
                    if (list.isNotEmpty()) {
                        _networkChildren.value = list
                        val activeId = _selectedChildId.value ?: list.first().id
                        _selectedChildId.value = activeId
                        repository.fetchGrades(activeId).onSuccess { gList ->
                            _networkGrades.value = gList
                        }
                        val activeChild = list.find { it.id == activeId }
                        if (activeChild?.classId != null) {
                            repository.fetchSchedule(activeChild.classId)
                        }
                        repository.getNextCharge(activeId).onSuccess { _nextCharge.value = it }
                    }
                }
                repository.fetchHolidays()
                repository.fetchAnnouncements()
                fetchComments()
                syncManager.flushSyncQueue()
            } catch (t: Throwable) {
                android.util.Log.e("ParentViewModel", "Error in refreshInitialData", t)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveGrade(gradeId: Int) {
        viewModelScope.launch {
            repository.approveGrade(gradeId)
        }
    }

    fun approveAllWeek(gradeIds: List<Int>) {
        viewModelScope.launch {
            repository.approveAllWeek(gradeIds)
        }
    }

    fun votePoll(announcementId: Int, optionId: Int) {
        viewModelScope.launch {
            repository.votePoll(announcementId, optionId)
        }
    }

    fun createGradeComment(gradeId: Int, content: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.createGradeComment(gradeId, content)
            fetchComments()
            onComplete(result.isSuccess)
        }
    }

    fun createMenuComment(menuDate: String, content: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.createMenuComment(menuDate, content)
            fetchComments()
            onComplete(result.isSuccess)
        }
    }

    fun fetchBalanceHistory(studentId: Int) {
        viewModelScope.launch {
            repository.getBalanceHistory(studentId).onSuccess {
                _balanceHistory.value = it
            }
        }
    }

    fun fetchDailyMenu(dateStr: String) {
        viewModelScope.launch {
            repository.fetchMenu(dateStr).onSuccess {
                _dailyMenu.value = it
            }
        }
    }

    fun fetchClubs(studentId: Int) {
        viewModelScope.launch {
            repository.fetchClubs(studentId)
        }
    }

    fun requestJoinClub(clubId: Int, studentId: Int) {
        viewModelScope.launch {
            repository.requestJoinClub(clubId, studentId)
        }
    }

    fun cancelClubRequest(clubId: Int, studentId: Int) {
        viewModelScope.launch {
            repository.cancelClubRequest(clubId, studentId)
        }
    }

    fun fetchBooks(level: Int? = null) {
        viewModelScope.launch {
            repository.fetchBooks(level)
        }
    }

    fun fetchAiReport() {
        viewModelScope.launch {
            repository.getLatestAiReport().onSuccess {
                _aiReport.value = it
            }
        }
    }
}
