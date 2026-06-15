package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Student::class,
        Attendance::class,
        FeePayment::class,
        ProgressLog::class,
        TuitionSchedule::class,
        ExamResult::class,
        SessionLog::class,
        ActiveTimer::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TuitionDatabase : RoomDatabase() {

    abstract fun tuitionDao(): TuitionDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDatabase? = null

        fun getDatabase(context: Context): TuitionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDatabase::class.java,
                    "tuition_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
