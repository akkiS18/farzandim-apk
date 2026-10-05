package uz.farzandim.teacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.entity.ClassEntity
import uz.farzandim.teacher.data.local.entity.GradeEntity
import uz.farzandim.teacher.data.local.entity.StudentEntity
import uz.farzandim.teacher.data.local.entity.SubjectEntity
import uz.farzandim.teacher.data.model.JournalLessonItem
import uz.farzandim.teacher.data.repository.JournalRepository
import uz.farzandim.teacher.data.sync.SyncManager
import uz.farzandim.teacher.data.sync.SyncState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val classes: StateFlow<List<ClassEntity>> = journalRepository.getClassesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SubjectEntity>> = journalRepository.getSubjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClassId = MutableStateFlow<Int?>(null)
    val selectedClassId: StateFlow<Int?> = _selectedClassId.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Int?>(null)
    val selectedSubjectId: StateFlow<Int?> = _selectedSubjectId.asStateFlow()

    private val _selectedLessonNumber = MutableStateFlow(1)
    val selectedLessonNumber: StateFlow<Int> = _selectedLessonNumber.asStateFlow()

    private val _selectedGradeCategory = MutableStateFlow("DAILY")
    val selectedGradeCategory: StateFlow<String> = _selectedGradeCategory.asStateFlow()

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _journalLessonsToday = MutableStateFlow<List<JournalLessonItem>>(emptyList())
    val journalLessonsToday: StateFlow<List<JournalLessonItem>> = _journalLessonsToday.asStateFlow()

    private val _currentTopic = MutableStateFlow<String?>(null)
    val currentTopic: StateFlow<String?> = _currentTopic.asStateFlow()

    private val _currentHomework = MutableStateFlow<String?>(null)
    val currentHomework: StateFlow<String?> = _currentHomework.asStateFlow()

    private val _currentPlanId = MutableStateFlow<Int?>(null)
    val currentPlanId: StateFlow<Int?> = _currentPlanId.asStateFlow()

    private val _isTopicLoading = MutableStateFlow(false)
    val isTopicLoading: StateFlow<Boolean> = _isTopicLoading.asStateFlow()

    private val _isNavigatingLesson = MutableStateFlow(false)
    val isNavigatingLesson: StateFlow<Boolean> = _isNavigatingLesson.asStateFlow()

    private val _isSavingTopic = MutableStateFlow(false)
    val isSavingTopic: StateFlow<Boolean> = _isSavingTopic.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val lastSyncedAt: StateFlow<Long?> = syncManager.lastSyncedAt

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val students: StateFlow<List<StudentEntity>> = combine(
        _selectedClassId,
        _selectedDate
    ) { classId, date ->
        Pair(classId, date)
    }.flatMapLatest { (classId, date) ->
        if (classId != null) {
            journalRepository.getStudentsForClassAndDateFlow(classId, date)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<GradeEntity>> = combine(
        _selectedSubjectId,
        _selectedLessonNumber,
        _selectedDate
    ) { subjId, lessonNum, date ->
        Triple(subjId, lessonNum, date)
    }.flatMapLatest { (subjId, lessonNum, date) ->
        if (subjId != null) {
            journalRepository.getGradesForLessonFlow(subjId, lessonNum, date)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * For each student (by studentId), how many days before the SELECTED JOURNAL DATE
     * did they last receive a MASTERY/DAILY grade in the SELECTED subject?
     * Reference date = selectedDate (NOT today) to match what the teacher sees in the journal.
     * Null means no previous grade found in local DB.
     */
    val lastGradeDaysAgoMap: StateFlow<Map<Int, Int>> = combine(
        students,
        _selectedSubjectId,
        _selectedDate
    ) { studs, subjId, date ->
        Triple(studs, subjId, date)
    }.flatMapLatest { (studs, subjId, date) ->
        if (subjId == null || studs.isEmpty()) {
            flowOf(emptyMap())
        } else {
            journalRepository.getLastGradeDatesBeforeFlow(subjId, date).map { list ->
                val dateMap = list.associate { it.studentId to it.maxDate }
                val result = mutableMapOf<Int, Int>()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val selectedDateObj = try { sdf.parse(date) } catch (_: Exception) { null }
                if (selectedDateObj != null) {
                    for (student in studs) {
                        val lastDateStr = dateMap[student.id]
                        if (lastDateStr != null) {
                            val lastDateObj = try { sdf.parse(lastDateStr) } catch (_: Exception) { null }
                            if (lastDateObj != null) {
                                val diffMs = selectedDateObj.time - lastDateObj.time
                                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                                if (diffDays > 0) {
                                    result[student.id] = diffDays
                                }
                            }
                        }
                    }
                }
                result
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    init {
        viewModelScope.launch {
            journalRepository.refreshClasses()
            journalRepository.refreshSubjects()
        }
    }

    fun selectGradeCategory(category: String) {
        _selectedGradeCategory.value = category
    }

    fun clearSubject() {
        _selectedSubjectId.value = null
        _currentTopic.value = null
        _currentHomework.value = null
        _currentPlanId.value = null
        _selectedGradeCategory.value = "DAILY"
    }

    fun clearClass() {
        _selectedClassId.value = null
        _selectedSubjectId.value = null
        _currentTopic.value = null
        _currentHomework.value = null
        _currentPlanId.value = null
        _journalLessonsToday.value = emptyList()
        _selectedGradeCategory.value = "DAILY"
    }

    fun selectClass(classId: Int?) {
        _selectedClassId.value = classId
        _selectedSubjectId.value = null
        _currentTopic.value = null
        _currentHomework.value = null
        _currentPlanId.value = null
        _selectedGradeCategory.value = "DAILY"

        if (classId != null) {
            viewModelScope.launch {
                journalRepository.refreshStudents(classId)
                val lessons = journalRepository.fetchClassScheduleToday(classId, _selectedDate.value)
                _journalLessonsToday.value = lessons

                // Prioritize teacher's primary assigned subject if it exists and is scheduled today
                val curClass = classes.value.find { it.id == classId }
                val defaultSubjId = curClass?.defaultSubjectId
                if (defaultSubjId != null && defaultSubjId > 0) {
                    val matchingLesson = lessons.find { it.subjectId == defaultSubjId }
                    if (matchingLesson != null) {
                        selectSubject(matchingLesson.subjectId, matchingLesson.lessonNumber)
                    }
                }
            }
        } else {
            _journalLessonsToday.value = emptyList()
        }
    }

    private suspend fun refreshLessonsForClass(classId: Int, date: String) {
        val lessons = journalRepository.fetchClassScheduleToday(classId, date)
        _journalLessonsToday.value = lessons
    }

    fun selectSubject(subjectId: Int, lessonNumber: Int = 1) {
        _selectedSubjectId.value = subjectId
        _selectedLessonNumber.value = lessonNumber
        _selectedGradeCategory.value = "DAILY" // Always default to DAILY (Kunlik)

        val classId = _selectedClassId.value
        if (classId != null) {
            viewModelScope.launch {
                journalRepository.refreshStudents(classId)
                journalRepository.refreshGrades(classId, subjectId, _selectedDate.value)
                loadLessonTopicAndHomework(classId, subjectId, lessonNumber, _selectedDate.value)
            }
        }
    }

    fun selectLesson(classId: Int, subjectId: Int, lessonNumber: Int, date: String) {
        _selectedDate.value = date
        _selectedClassId.value = classId
        _selectedSubjectId.value = subjectId
        _selectedLessonNumber.value = lessonNumber
        _selectedGradeCategory.value = "DAILY"

        viewModelScope.launch {
            refreshLessonsForClass(classId, date)
            journalRepository.refreshStudents(classId)
            journalRepository.refreshGrades(classId, subjectId, date)
            loadLessonTopicAndHomework(classId, subjectId, lessonNumber, date)
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        _selectedGradeCategory.value = "DAILY" // Always default to DAILY (Kunlik)
        val classId = _selectedClassId.value
        val subjectId = _selectedSubjectId.value
        val lessonNumber = _selectedLessonNumber.value

        viewModelScope.launch {
            if (classId != null) {
                refreshLessonsForClass(classId, date)
                if (subjectId != null) {
                    journalRepository.refreshGrades(classId, subjectId, date)
                    loadLessonTopicAndHomework(classId, subjectId, lessonNumber, date)
                }
            }
        }
    }

    private suspend fun loadLessonTopicAndHomework(classId: Int, subjectId: Int, lessonNumber: Int, date: String) {
        _isTopicLoading.value = true
        val info = journalRepository.fetchLessonTopicAndHomework(classId, subjectId, lessonNumber, date)
        _currentTopic.value = info.topic
        _currentHomework.value = info.homework
        _currentPlanId.value = info.planId
        _isTopicLoading.value = false
    }

    fun navigateAdjacentLesson(direction: String) {
        val classId = _selectedClassId.value ?: return
        val subjectId = _selectedSubjectId.value ?: return
        val lessonNumber = _selectedLessonNumber.value
        val date = _selectedDate.value

        if (_isNavigatingLesson.value) return

        viewModelScope.launch {
            _isNavigatingLesson.value = true
            val result = journalRepository.fetchAdjacentLesson(classId, subjectId, lessonNumber, date, direction)
            _isNavigatingLesson.value = false

            result.fold(
                onSuccess = { adjacent ->
                    if (adjacent != null) {
                        selectLesson(classId, subjectId, adjacent.lessonNumber, adjacent.date)
                        val dirText = if (direction == "previous") "Oldingi" else "Keyingi"
                        _toastMessage.emit("$dirText darsga o'tildi: ${adjacent.date} (${adjacent.lessonNumber}-soat)")
                    } else {
                        _toastMessage.emit("Shu fan uchun bir yil oralig‘ida boshqa dars topilmadi")
                    }
                },
                onFailure = { err ->
                    _toastMessage.emit("Darsni yuklab bo'lmadi: ${err.message}")
                }
            )
        }
    }

    fun saveTopicAndHomework(topic: String, homework: String) {
        val classId = _selectedClassId.value ?: return
        val subjectId = _selectedSubjectId.value ?: return
        val lessonNumber = _selectedLessonNumber.value
        val date = _selectedDate.value
        val existingPlanId = _currentPlanId.value

        viewModelScope.launch {
            _isSavingTopic.value = true
            val res = journalRepository.saveLessonTopicAndHomework(
                classId = classId,
                subjectId = subjectId,
                lessonNumber = lessonNumber,
                date = date,
                topic = topic,
                homework = homework,
                existingPlanId = existingPlanId
            )
            _isSavingTopic.value = false

            res.fold(
                onSuccess = { planId ->
                    _currentTopic.value = topic.ifBlank { null }
                    _currentHomework.value = homework.ifBlank { null }
                    if (planId != null) {
                        _currentPlanId.value = planId
                    }
                    _toastMessage.emit("Mavzu va vazifa saqlandi ✓")
                },
                onFailure = { err ->
                    _toastMessage.emit("Saqlashda xatolik: ${err.message}")
                }
            )
        }
    }

    fun refreshCurrentJournal() {
        val classId = _selectedClassId.value
        val subjectId = _selectedSubjectId.value
        val date = _selectedDate.value
        val lessonNumber = _selectedLessonNumber.value

        if (classId != null && subjectId != null) {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    journalRepository.refreshStudents(classId)
                    journalRepository.refreshGrades(classId, subjectId, date)
                    loadLessonTopicAndHomework(classId, subjectId, lessonNumber, date)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    // Step 1: Draft save on cell blur / input change (< 10ms response)
    fun onCellChanged(
        studentId: Int,
        gradeType: String,
        value: String
    ) {
        val subjectId = _selectedSubjectId.value ?: return
        val lessonNumber = _selectedLessonNumber.value
        val date = _selectedDate.value
        val category = _selectedGradeCategory.value

        viewModelScope.launch {
            val entity = journalRepository.saveGradeDraft(
                studentId = studentId,
                subjectId = subjectId,
                lessonNumber = lessonNumber,
                gradeDate = date,
                gradeType = gradeType,
                value = value,
                gradeCategory = category
            )
            if (entity == null && value.isNotBlank()) {
                _toastMessage.emit("Ushbu baho tasdiqlangan, tahrirlab bo'lmaydi")
            }
            syncManager.updateSyncStatus()
        }
    }

    // Step 2: Finalize & Approve ("Saqlash" button)
    fun finalizeAndApprove() {
        val subjectId = _selectedSubjectId.value ?: return
        val lessonNumber = _selectedLessonNumber.value
        val date = _selectedDate.value

        viewModelScope.launch {
            _isSaving.value = true
            val result = journalRepository.finalizeAndApproveGrades(subjectId, lessonNumber, date)
            _isSaving.value = false

            result.fold(
                onSuccess = { count ->
                    if (count > 0) {
                        _toastMessage.emit("$count ta baho tasdiqlandi va saqlandi 🔒")
                    } else {
                        _toastMessage.emit("Yangi saqlanadigan baholar mavjud emas")
                    }
                    syncManager.updateSyncStatus()
                },
                onFailure = { error ->
                    _toastMessage.emit("Xatolik: ${error.message}")
                }
            )
        }
    }

    fun triggerManualSync() {
        syncManager.triggerImmediateSync()
    }
}
