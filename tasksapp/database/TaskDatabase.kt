package com.example.tasksapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class, Subtask::class, DailyTodoTask::class, Tag::class,
    TaskMatrixResult::class, TodaySessionResult::class, DailyTodoSchedule::class,
    AverageTodoTime::class], version = 50, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract val taskDataDao: TaskDataDao
    abstract val subtaskDataDao: SubtaskDataDao
    abstract val todoScheduleDao: TodoScheduleDao
    abstract val todoTaskDao: TodoTaskDataDao
    abstract val tagsDao: TagsDao
    abstract val matrixResultDao: MatrixResultDao
    abstract val todaySessionDao: TodaySessionDao
    abstract val averageTodoTimeDao: AverageTodoTimeDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            synchronized(this) {

                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        TaskDatabase::class.java, "task_history_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}