package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TuitionViewModel(
    application: Application,
    private val repository: TuitionRepository
) : AndroidViewModel(application) {

    // --- Formatters ---
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // --- State Variables ---
    val activeSection = MutableStateFlow(Section.DASHBOARD)
    val selectedDate = MutableStateFlow(apiDateFormat.format(Date()))
    val selectedMonthFilter = MutableStateFlow(monthFormat.format(Date()))

    // Active details states (non-persistent UI state)
    var selectedStudentForDetail by mutableStateOf<Student?>(null)
    var showAddStudentDialog by mutableStateOf(false)
    var studentToEdit by mutableStateOf<Student?>(null)
    
    // Quick log dialogs state
    var showAddPaymentDialogForStudent by mutableStateOf<Student?>(null)
    var showAddProgressDialogForStudent by mutableStateOf<Student?>(null)
    var showAddScheduleDialogForStudent by mutableStateOf<Student?>(null)
    var showAddExamDialogForStudent by mutableStateOf<Student?>(null)

    // --- Flows ---
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<FeePayment>> = repository.allFeePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgressLogs: StateFlow<List<ProgressLog>> = repository.allProgressLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<TuitionSchedule>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExamResults: StateFlow<List<ExamResult>> = repository.allExamResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessionLogs: StateFlow<List<SessionLog>> = repository.allSessionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTimers: StateFlow<List<ActiveTimer>> = repository.allActiveTimers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val attendanceForSelectedDate: StateFlow<List<Attendance>> = selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of months for payment filtering
    val availablePaymentMonths: StateFlow<List<String>> = allPayments
        .map { payments -> 
            val monthsSet = mutableSetOf<String>()
            // Always have current month
            monthsSet.add(monthFormat.format(Date()))
            payments.forEach { monthsSet.add(it.monthCovered) }
            monthsSet.toList().sortedWith { m1, m2 ->
                try {
                    val d1 = monthFormat.parse(m1)
                    val d2 = monthFormat.parse(m2)
                    d2.compareTo(d1) // descending
                } catch (e: Exception) {
                    0
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(monthFormat.format(Date())))

    // --- Business Operations ---

    fun selectSection(section: Section) {
        activeSection.value = section
        // Reset detailed states when changing tabs to prevent state leakage
        selectedStudentForDetail = null
    }

    fun setDate(date: Date) {
        selectedDate.value = apiDateFormat.format(date)
    }

    fun changeDate(offsetDays: Int) {
        try {
            val current = apiDateFormat.parse(selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = current
                add(Calendar.DAY_OF_YEAR, offsetDays)
            }
            selectedDate.value = apiDateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Student CRUD ---
    fun saveStudent(
        id: Int = 0,
        name: String,
        grade: String,
        subject: String,
        monthlyFee: Double,
        contactNumber: String,
        notes: String,
        isActive: Boolean = true,
        paymentCycleDays: Int = 12
    ) {
        viewModelScope.launch {
            val student = Student(
                id = id,
                name = name,
                grade = grade,
                subject = subject,
                monthlyFee = monthlyFee,
                contactNumber = contactNumber,
                notes = notes,
                isActive = isActive,
                paymentCycleDays = paymentCycleDays
            )
            repository.insertStudent(student)
            // Update selected student detail state if that student was edited
            if (id != 0 && selectedStudentForDetail?.id == id) {
                selectedStudentForDetail = student
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (selectedStudentForDetail?.id == student.id) {
                selectedStudentForDetail = null
            }
        }
    }

    // --- Attendance Operations ---
    fun toggleAttendance(studentId: Int, currentStatus: String?) {
        viewModelScope.launch {
            val date = selectedDate.value
            val timeMillis = System.currentTimeMillis()
            
            // Map: null / "ABSENT" -> "PRESENT", "PRESENT" -> "LATE", "LATE" -> "ABSENT"
            val newStatus = when (currentStatus) {
                "PRESENT" -> "LATE"
                "LATE" -> "ABSENT"
                else -> "PRESENT"
            }
            
            val existing = attendanceForSelectedDate.value.find { it.studentId == studentId }
            val attendance = Attendance(
                id = existing?.id ?: 0,
                studentId = studentId,
                dateString = date,
                dateMillis = existing?.dateMillis ?: timeMillis,
                status = newStatus,
                notes = existing?.notes ?: ""
            )
            repository.insertAttendance(attendance)
        }
    }

    fun setStudentAttendanceNotes(studentId: Int, notes: String) {
        viewModelScope.launch {
            val existing = attendanceForSelectedDate.value.find { it.studentId == studentId }
            if (existing != null) {
                repository.insertAttendance(existing.copy(notes = notes))
            } else {
                val attendance = Attendance(
                    studentId = studentId,
                    dateString = selectedDate.value,
                    dateMillis = System.currentTimeMillis(),
                    status = "PRESENT",
                    notes = notes
                )
                repository.insertAttendance(attendance)
            }
        }
    }

    // --- Fee Payment Operations ---
    fun logFeePayment(
        studentId: Int,
        amount: Double,
        monthCovered: String,
        paymentDate: Date = Date(),
        notes: String = ""
    ) {
        viewModelScope.launch {
            val payment = FeePayment(
                studentId = studentId,
                amount = amount,
                paymentDateString = apiDateFormat.format(paymentDate),
                paymentDateMillis = paymentDate.time,
                monthCovered = monthCovered,
                notes = notes
            )
            repository.insertFeePayment(payment)
        }
    }

    fun deleteFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deleteFeePayment(payment)
        }
    }

    // --- Progress Operations ---
    fun logProgress(
        studentId: Int,
        topic: String,
        rating: Int,
        remarks: String = "",
        date: Date = Date()
    ) {
        viewModelScope.launch {
            val progress = ProgressLog(
                studentId = studentId,
                dateString = apiDateFormat.format(date),
                dateMillis = date.time,
                topic = topic,
                rating = rating,
                remarks = remarks
            )
            repository.insertProgressLog(progress)
        }
    }

    fun deleteProgressLog(log: ProgressLog) {
        viewModelScope.launch {
            repository.deleteProgressLog(log)
        }
    }

    // --- History Fetching (for details view) ---
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> {
        return repository.getAttendanceForStudent(studentId)
    }

    fun getFeePaymentsForStudent(studentId: Int): Flow<List<FeePayment>> {
        return repository.getFeePaymentsForStudent(studentId)
    }

    fun getProgressLogsForStudent(studentId: Int): Flow<List<ProgressLog>> {
        return repository.getProgressLogsForStudent(studentId)
    }

    // --- Schedule Operations ---
    fun saveSchedule(
        studentId: Int,
        dayOfWeek: String,
        timeString: String,
        durationMinutes: Int = 90,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val schedule = TuitionSchedule(
                studentId = studentId,
                dayOfWeek = dayOfWeek,
                timeString = timeString,
                durationMinutes = durationMinutes,
                notes = notes
            )
            repository.insertSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: TuitionSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun getSchedulesForStudent(studentId: Int): Flow<List<TuitionSchedule>> {
        return repository.getSchedulesForStudent(studentId)
    }

    // --- Exam Operations ---
    fun logExamResult(
        studentId: Int,
        subject: String,
        examName: String,
        totalMarks: Double,
        obtainedMarks: Double,
        remarks: String = "",
        date: Date = Date()
    ) {
        viewModelScope.launch {
            val examResult = ExamResult(
                studentId = studentId,
                subject = subject,
                examName = examName,
                dateString = apiDateFormat.format(date),
                dateMillis = date.time,
                totalMarks = totalMarks,
                obtainedMarks = obtainedMarks,
                remarks = remarks
            )
            repository.insertExamResult(examResult)
        }
    }

    fun deleteExamResult(examResult: ExamResult) {
        viewModelScope.launch {
            repository.deleteExamResult(examResult)
        }
    }

    fun getExamResultsForStudent(studentId: Int): Flow<List<ExamResult>> {
        return repository.getExamResultsForStudent(studentId)
    }

    // --- Session Timer Operations ---
    fun startTimerForStudent(studentId: Int, notes: String = "") {
        viewModelScope.launch {
            val activeTimer = ActiveTimer(
                studentId = studentId,
                startTimeMillis = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertActiveTimer(activeTimer)
        }
    }

    fun stopTimerForStudent(studentId: Int, notes: String = "") {
        viewModelScope.launch {
            val allTimers = activeTimers.value
            val active = allTimers.find { it.studentId == studentId }
            if (active != null) {
                val end = System.currentTimeMillis()
                val start = active.startTimeMillis
                val durationMs = end - start
                // Math limits to minimum 1 min
                val durationMin = if (durationMs < 60000) 1 else (durationMs / 60000).toInt()
                
                val sessionLog = SessionLog(
                    studentId = studentId,
                    dateString = apiDateFormat.format(Date(end)),
                    startTimeMillis = start,
                    endTimeMillis = end,
                    durationMinutes = durationMin,
                    notes = notes.ifBlank { active.notes }
                )
                repository.insertSessionLog(sessionLog)
            }
            repository.deleteActiveTimer(studentId)
        }
    }

    fun cancelTimerForStudent(studentId: Int) {
        viewModelScope.launch {
            repository.deleteActiveTimer(studentId)
        }
    }

    fun addManualSessionLog(
        studentId: Int,
        dateString: String,
        startTimeStr: String,
        endTimeStr: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val startDate = format.parse("$dateString $startTimeStr") ?: Date()
                val endDate = format.parse("$dateString $endTimeStr") ?: Date()
                val durationMs = endDate.time - startDate.time
                val durationMin = if (durationMs < 60000) 1 else (durationMs / 60000).toInt()

                val sessionLog = SessionLog(
                    studentId = studentId,
                    dateString = dateString,
                    startTimeMillis = startDate.time,
                    endTimeMillis = endDate.time,
                    durationMinutes = maxOf(1, durationMin),
                    notes = notes
                )
                repository.insertSessionLog(sessionLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSessionLog(sessionLog: SessionLog) {
        viewModelScope.launch {
            repository.deleteSessionLog(sessionLog)
        }
    }

    fun getSessionLogsForStudent(studentId: Int): Flow<List<SessionLog>> {
        return repository.getSessionLogsForStudent(studentId)
    }
}

enum class Section {
    DASHBOARD,
    STUDENTS,
    SCHEDULE,
    ATTENDANCE,
    FEES,
    PROGRESS
}

class TuitionViewModelFactory(
    private val application: Application,
    private val repository: TuitionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TuitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TuitionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
