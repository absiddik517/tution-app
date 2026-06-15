package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    // --- Student Queries ---
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Int): Flow<Student?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Delete
    suspend fun deleteStudent(student: Student)

    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY dateMillis DESC")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance ORDER BY dateMillis DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    // --- Fee Payments Queries ---
    @Query("SELECT * FROM fee_payments ORDER BY paymentDateMillis DESC")
    fun getAllFeePayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDateMillis DESC")
    fun getFeePaymentsForStudent(studentId: Int): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(payment: FeePayment): Long

    @Delete
    suspend fun deleteFeePayment(payment: FeePayment)

    // --- Progress Logs Queries ---
    @Query("SELECT * FROM progress_logs ORDER BY dateMillis DESC")
    fun getAllProgressLogs(): Flow<List<ProgressLog>>

    @Query("SELECT * FROM progress_logs WHERE studentId = :studentId ORDER BY dateMillis DESC")
    fun getProgressLogsForStudent(studentId: Int): Flow<List<ProgressLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressLog(log: ProgressLog): Long

    @Delete
    suspend fun deleteProgressLog(log: ProgressLog)

    // --- Tuition Schedule Queries ---
    @Query("SELECT * FROM tuition_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<TuitionSchedule>>

    @Query("SELECT * FROM tuition_schedules WHERE studentId = :studentId")
    fun getSchedulesForStudent(studentId: Int): Flow<List<TuitionSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TuitionSchedule): Long

    @Delete
    suspend fun deleteSchedule(schedule: TuitionSchedule)

    // --- Exam Result Queries ---
    @Query("SELECT * FROM exam_results ORDER BY dateMillis DESC")
    fun getAllExamResults(): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId ORDER BY dateMillis DESC")
    fun getExamResultsForStudent(studentId: Int): Flow<List<ExamResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(examResult: ExamResult): Long

    @Delete
    suspend fun deleteExamResult(examResult: ExamResult)

    // --- Session Logs Queries ---
    @Query("SELECT * FROM session_logs ORDER BY startTimeMillis DESC")
    fun getAllSessionLogs(): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs WHERE studentId = :studentId ORDER BY startTimeMillis DESC")
    fun getSessionLogsForStudent(studentId: Int): Flow<List<SessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionLog(sessionLog: SessionLog): Long

    @Delete
    suspend fun deleteSessionLog(sessionLog: SessionLog)

    // --- Active Timer Queries ---
    @Query("SELECT * FROM active_timers")
    fun getActiveTimers(): Flow<List<ActiveTimer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveTimer(activeTimer: ActiveTimer)

    @Query("DELETE FROM active_timers WHERE studentId = :studentId")
    suspend fun deleteActiveTimer(studentId: Int)
}
