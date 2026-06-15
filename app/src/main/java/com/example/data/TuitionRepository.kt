package com.example.data

import kotlinx.coroutines.flow.Flow

class TuitionRepository(private val tuitionDao: TuitionDao) {

    // --- Students ---
    val allStudents: Flow<List<Student>> = tuitionDao.getAllStudents()

    fun getStudentById(id: Int): Flow<Student?> = tuitionDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long = tuitionDao.insertStudent(student)

    suspend fun deleteStudent(student: Student) = tuitionDao.deleteStudent(student)

    // --- Attendance ---
    val allAttendance: Flow<List<Attendance>> = tuitionDao.getAllAttendance()

    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>> =
        tuitionDao.getAttendanceForDate(dateString)

    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> =
        tuitionDao.getAttendanceForStudent(studentId)

    suspend fun insertAttendance(attendance: Attendance) =
        tuitionDao.insertAttendance(attendance)

    suspend fun insertAttendanceList(attendanceList: List<Attendance>) =
        tuitionDao.insertAttendanceList(attendanceList)

    suspend fun deleteAttendance(attendance: Attendance) =
        tuitionDao.deleteAttendance(attendance)

    // --- Fee Payments ---
    val allFeePayments: Flow<List<FeePayment>> = tuitionDao.getAllFeePayments()

    fun getFeePaymentsForStudent(studentId: Int): Flow<List<FeePayment>> =
        tuitionDao.getFeePaymentsForStudent(studentId)

    suspend fun insertFeePayment(payment: FeePayment): Long =
        tuitionDao.insertFeePayment(payment)

    suspend fun deleteFeePayment(payment: FeePayment) =
        tuitionDao.deleteFeePayment(payment)

    // --- Progress Logs ---
    val allProgressLogs: Flow<List<ProgressLog>> = tuitionDao.getAllProgressLogs()

    fun getProgressLogsForStudent(studentId: Int): Flow<List<ProgressLog>> =
        tuitionDao.getProgressLogsForStudent(studentId)

    suspend fun insertProgressLog(log: ProgressLog): Long =
        tuitionDao.insertProgressLog(log)

    suspend fun deleteProgressLog(log: ProgressLog) =
        tuitionDao.deleteProgressLog(log)

    // --- Tuition Schedules ---
    val allSchedules: Flow<List<TuitionSchedule>> = tuitionDao.getAllSchedules()

    fun getSchedulesForStudent(studentId: Int): Flow<List<TuitionSchedule>> =
        tuitionDao.getSchedulesForStudent(studentId)

    suspend fun insertSchedule(schedule: TuitionSchedule): Long =
        tuitionDao.insertSchedule(schedule)

    suspend fun deleteSchedule(schedule: TuitionSchedule) =
        tuitionDao.deleteSchedule(schedule)

    // --- Exam Results ---
    val allExamResults: Flow<List<ExamResult>> = tuitionDao.getAllExamResults()

    fun getExamResultsForStudent(studentId: Int): Flow<List<ExamResult>> =
        tuitionDao.getExamResultsForStudent(studentId)

    suspend fun insertExamResult(examResult: ExamResult): Long =
        tuitionDao.insertExamResult(examResult)

    suspend fun deleteExamResult(examResult: ExamResult) =
        tuitionDao.deleteExamResult(examResult)

    // --- Session Logs ---
    val allSessionLogs: Flow<List<SessionLog>> = tuitionDao.getAllSessionLogs()

    fun getSessionLogsForStudent(studentId: Int): Flow<List<SessionLog>> =
        tuitionDao.getSessionLogsForStudent(studentId)

    suspend fun insertSessionLog(sessionLog: SessionLog): Long =
        tuitionDao.insertSessionLog(sessionLog)

    suspend fun deleteSessionLog(sessionLog: SessionLog) =
        tuitionDao.deleteSessionLog(sessionLog)

    // --- Active Timers ---
    val allActiveTimers: Flow<List<ActiveTimer>> = tuitionDao.getActiveTimers()

    suspend fun insertActiveTimer(activeTimer: ActiveTimer) =
        tuitionDao.insertActiveTimer(activeTimer)

    suspend fun deleteActiveTimer(studentId: Int) =
        tuitionDao.deleteActiveTimer(studentId)
}
