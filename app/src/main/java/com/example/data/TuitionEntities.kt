package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val grade: String,
    val subject: String,
    val monthlyFee: Double,
    val contactNumber: String,
    val notes: String = "",
    val isActive: Boolean = true,
    val paymentCycleDays: Int = 12
)

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["studentId", "dateString"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val dateString: String, // format: "YYYY-MM-DD"
    val dateMillis: Long,
    val status: String, // "PRESENT", "ABSENT", "LATE"
    val notes: String = ""
)

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val amount: Double,
    val paymentDateString: String, // format: "YYYY-MM-DD"
    val paymentDateMillis: Long,
    val monthCovered: String, // e.g. "June 2026", "July 2026"
    val notes: String = ""
)

@Entity(tableName = "progress_logs")
data class ProgressLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val dateString: String, // format: "YYYY-MM-DD"
    val dateMillis: Long,
    val topic: String,
    val rating: Int, // 1 to 5 stars
    val remarks: String = ""
)

@Entity(tableName = "tuition_schedules")
data class TuitionSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val dayOfWeek: String, // e.g. "Monday", "Tuesday", etc.
    val timeString: String, // e.g., "04:30 PM", "10:00 AM"
    val durationMinutes: Int = 90,
    val notes: String = ""
)

@Entity(tableName = "exam_results")
data class ExamResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val subject: String,
    val examName: String,
    val dateString: String, // "YYYY-MM-DD"
    val dateMillis: Long,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val remarks: String = ""
)

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val dateString: String, // "YYYY-MM-DD"
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val notes: String = ""
)

@Entity(tableName = "active_timers")
data class ActiveTimer(
    @PrimaryKey val studentId: Int,
    val startTimeMillis: Long,
    val notes: String = ""
)
