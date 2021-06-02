package com.example.tasksapp.database

import androidx.room.*

@Entity(tableName = "tasks_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val taskId: Long = 0L,
    @ColumnInfo(name = "task_tag")
    val taskTag: String = "",
    @ColumnInfo(name = "isFinished")
    var isFinished: Int = 0,
    @ColumnInfo(name = "startTime")
    var startTime: Long = 0L,
    @ColumnInfo(name = "finishTime")
    var finishTime: Long = 0L,
    @ColumnInfo(name = "bookMarkColor")
    var bookMarkColor: Int = 0,
    @ColumnInfo(name = "tag")
    var tag: String = "No tag",
    @ColumnInfo(name = "reminder_date")
    var date: Long = 0L,
    @ColumnInfo(name = "importance")
    var importance: Int = 0
)

@Entity(tableName = "subtask_table")
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val subtaskId: Long = 0L,
    @ColumnInfo(name = "subrask_text")
    val subtaskText: String = "",
    @ColumnInfo(name = "parent_id")
    val parentId: Long = 0L,
    @ColumnInfo(name = "position")
    var position: Long = 1L,
    @ColumnInfo(name = "isCompleted")
    var isCompleted: Int = 0,
    @ColumnInfo(name = "matrixValue")
    var matrixValue: Int = -1,
    @ColumnInfo(name = "subtask_finish_time")
    var subtaskFinishTime: Long = 0L
)

@Entity(tableName = "task_matrix_result")
data class TaskMatrixResult(
    @PrimaryKey
    val taskMatrixResultId: String = "",
    @ColumnInfo(name = "task_id")
    val taskParentId: Long = 0L,
    @ColumnInfo(name = "finish_matrix_time")
    var finishMatrixTime: Long = 0L,
    @ColumnInfo(name = "matrix_name")
    var matrixName: String = ""
)

@Entity(tableName = "today_session_table")
data class TodaySessionResult(
    @PrimaryKey(autoGenerate = true)
    val todaySessionId: Long = 0L,
    @ColumnInfo(name = "parent_task_id")
    val parentTaskId: Long = 0L,
    @ColumnInfo(name = "session_duration")
    var sessionDuration: Long = 0L,
    @ColumnInfo(name = "session_date")
    var sessionDate: Long = 0L,
    @ColumnInfo(name = "finished")
    var finished: Int = 1
)

data class TaskWithSubtasks(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "parent_id"
    )
    val subtasks: List<Subtask>
)

@Entity(tableName = "todo_schedule_table")
data class DailyTodoSchedule(
    @PrimaryKey
    val todoScheduleId: String = "",
    @ColumnInfo(name = "schedule_date")
    val scheduleDate: Long = 0L,
    @ColumnInfo(name = "total_time")
    var totalTime: Long = 0L,
    @ColumnInfo(name = "archive_value")
    var archiveVal: Int = 0,
    @ColumnInfo(name = "reminder_date")
    var reminderDate: Long = 0
)

@Entity(tableName = "todotask_table")
data class DailyTodoTask(
    @PrimaryKey(autoGenerate = true)
    val todoTaskId: Long = 0L,
    @ColumnInfo(name = "parent_id")
    val parentId: String = "",
    @ColumnInfo(name = "todoTask_text")
    val todoTaskText: String = "",
    @ColumnInfo(name = "todoHeader_text")
    val todoHeader: String = "",
    @ColumnInfo(name = "position_todo")
    var position: Long = 1L,
    @ColumnInfo(name = "isCompleted_todo")
    var isCompleted: Int = 0,
    @ColumnInfo(name = "daily_task_finish_time")
    var todoFinishTime: Long = 0L,
    @ColumnInfo(name = "isRoutine")
    var isRoutine: Int = 0,
    @ColumnInfo(name = "archive_todo")
    var archive: Int = 0
)

data class ScheduleWithTodo(
    @Embedded val schedule: DailyTodoSchedule,
    @Relation(
        parentColumn = "todoScheduleId",
        entityColumn = "parent_id"
    )
    val todoList: List<DailyTodoTask>
)

@Entity(tableName = "averageTodoTime_table")
data class AverageTodoTime(
    @PrimaryKey
    val avTimeId: String = "",
    @ColumnInfo(name = "time")
    val time: Long = 0L,
    @ColumnInfo(name = "finishDate")
    val finishDate: Long = 0L
)

@Entity(tableName = "tags_table")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val tagsId: Long = 0L,
    @ColumnInfo(name = "tag_name")
    val tagName: String = "",
    @ColumnInfo(name = "colorValue")
    val colorValue: Int = 0
)
