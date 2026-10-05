package uz.farzandim.teacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.SessionManager
import uz.farzandim.teacher.data.local.entity.GradeCommentEntity
import uz.farzandim.teacher.data.repository.JournalRepository
import uz.farzandim.teacher.data.sync.SyncManager

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CommentViewModel(
    private val journalRepository: JournalRepository,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _localGradeId = MutableStateFlow<String?>(null)

    val comments: StateFlow<List<GradeCommentEntity>> = _localGradeId
        .filterNotNull()
        .flatMapLatest { gradeId -> journalRepository.getCommentsForGradeFlow(gradeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    fun setLocalGradeId(gradeId: String) {
        _localGradeId.value = gradeId
    }

    fun onCommentTextChange(text: String) {
        _commentText.value = text
    }

    fun submitComment() {
        val gradeId = _localGradeId.value ?: return
        val text = _commentText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val teacherName = sessionManager.userFullNameFlow.first()
            val result = journalRepository.addComment(
                localGradeId = gradeId,
                authorId = 0, // Resolved by backend token or offline author
                authorName = teacherName,
                content = text
            )
            _isSubmitting.value = false

            result.fold(
                onSuccess = {
                    _commentText.value = ""
                    _eventMessage.emit("Izoh saqlandi ✓")
                    syncManager.updateSyncStatus()
                },
                onFailure = { error ->
                    _eventMessage.emit("Xatolik: ${error.message}")
                }
            )
        }
    }
}
