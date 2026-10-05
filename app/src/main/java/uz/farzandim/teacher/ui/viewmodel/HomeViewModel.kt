package uz.farzandim.teacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.entity.ClassEntity
import uz.farzandim.teacher.data.local.entity.TodayLessonEntity
import uz.farzandim.teacher.data.repository.JournalRepository
import uz.farzandim.teacher.data.repository.ScheduleRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val managedClasses: StateFlow<List<ClassEntity>> = journalRepository.getClassesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleMeta = scheduleRepository.scheduleMeta

    val todayLessons: StateFlow<List<TodayLessonEntity>> = _selectedDate
        .flatMapLatest { date -> scheduleRepository.getTodayLessonsFlow(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            journalRepository.cleanupStaleGrades()
        }
        refresh()
        viewModelScope.launch {
            journalRepository.refreshClasses()
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            scheduleRepository.refreshTodayLessons(_selectedDate.value)
            _isLoading.value = false
        }
    }
}
