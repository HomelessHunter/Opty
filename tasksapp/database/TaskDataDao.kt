package com.example.tasksapp.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks_table WHERE taskId = :key")
    suspend fun delete(key: Long)

    @Query("SELECT * FROM tasks_table ORDER BY taskId DESC LIMIT 1")
    fun getTask(): Task

    @Query("SELECT * FROM tasks_table WHERE taskId = :key")
    suspend fun getTaskWithKey(key: Long): Task

    @Query("SELECT * FROM tasks_table WHERE taskId = :key")
    fun getFlowTaskWithKey(key: Long): Flow<Task>

    @Query("SELECT * FROM tasks_table WHERE isFinished = 0 ORDER BY taskId DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE isFinished = 1 ORDER BY taskId DESC")
    fun getAllFinishedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE reminder_date > 0 ORDER BY taskId DESC")
    suspend fun getAllTaskWithDate(): List<Task>

    @Query("SELECT * FROM tasks_table WHERE task_tag = :taskTag")
    suspend fun getTaskByTaskTag(taskTag: String): Task

    @Transaction
    @Query("SELECT * FROM tasks_table WHERE taskId = :id")
    suspend fun getTaskWithSubtasks(id: Long): TaskWithSubtasks

    @Transaction
    @Query("SELECT * FROM tasks_table WHERE tag = :tag")
    fun getAllTasksWithSubtasks(tag: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks_table WHERE task_tag = :taskTag")
    suspend fun getTaskWithSubtaskskByTag(taskTag: String): List<TaskWithSubtasks>

    @Query("SELECT * FROM tasks_table WHERE isFinished = 0")
    fun getActiveProjects(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE isFinished = 1")
    fun getFinishedProjects(): Flow<List<Task>>

    @Query("SELECT * FROM tasks_table WHERE tag = :tag")
    suspend fun getTasksByTag(tag: String): List<Task>

    @Query("SELECT * FROM tasks_table WHERE importance = 1")
    suspend fun getImportantProjects():List<Task>
}

@Dao
interface SubtaskDataDao {
    @Insert
    suspend fun putSubtask(subtask: Subtask)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("SELECT * FROM subtask_table WHERE parent_id = :id ORDER BY position DESC")
    fun getAllSubtasks(id: Long): PagingSource<Int, Subtask>

    @Query("SELECT * FROM subtask_table WHERE parent_id = :id ORDER BY position DESC")
    fun getAllMatrixSubtasks(id: Long): Flow<List<Subtask>>

    @Query("DELETE FROM subtask_table WHERE parent_id = :id")
    suspend fun deleteSubtasksById(id: Long)

    @Query("SELECT * FROM subtask_table WHERE subtaskId = :id")
    suspend fun getSubtaskById(id: Long): Subtask

    @Query("SELECT MAX(position) FROM subtask_table")
    suspend fun getMaxPosition(): Long?

    @Query("SELECT MIN(position) FROM subtask_table")
    suspend fun getMinPosition(): Long?

    @Query("SELECT * FROM subtask_table")
    fun getAllAllSubtasks(): PagingSource<Int, Subtask>

    @Query("SELECT subtask_finish_time FROM subtask_table")
    fun getFinishedSubtasksFinishTime(): Flow<List<Long>>
}

@Dao
interface MatrixResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatrixResult(matrixResult: TaskMatrixResult)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMatrixResult(matrixResult: TaskMatrixResult)
    @Delete
    suspend fun deleteMatrixResult(matrixResult: TaskMatrixResult)

    @Query("SELECT * FROM task_matrix_result WHERE taskMatrixResultId = :id")
    suspend fun getMatrixResultById(id: String): TaskMatrixResult?

    @Query("SELECT * FROM task_matrix_result")
    fun getMatrixResultList(): Flow<List<TaskMatrixResult>>

    @Query("SELECT * FROM task_matrix_result WHERE task_id = :id")
    fun getAllResultsById(id: Long): Flow<List<TaskMatrixResult>>

}

@Dao
interface TodaySessionDao {
    @Query("SELECT * FROM today_session_table WHERE parent_task_id = :id ORDER BY todaySessionId DESC LIMIT 1")
    suspend fun getTodaySession(id: Long): TodaySessionResult?

    @Query("SELECT * FROM today_session_table WHERE parent_task_id = :id ORDER BY todaySessionId DESC LIMIT 1")
    fun getTodaySessionFlow(id: Long): Flow<TodaySessionResult>

    @Query("SELECT * FROM today_session_table WHERE parent_task_id = :id ORDER BY session_date DESC")
    fun getSessionFlowList(id: Long): Flow<List<TodaySessionResult>>

    @Query("SELECT * FROM today_session_table WHERE parent_task_id = :id ORDER BY session_date ASC")
    suspend fun getSessionListASC(id: Long): List<TodaySessionResult>?

    @Query("SELECT * FROM today_session_table WHERE todaySessionId = :id")
    suspend fun getSessionById(id: Long): TodaySessionResult

    @Query("SELECT session_duration FROM today_session_table")
    fun getAllSessions(): Flow<List<Long>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTodaySession(todaySessionResult: TodaySessionResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodaySession(todaySessionResult: TodaySessionResult)
}

@Dao
interface TodoScheduleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTodoSchedule(todoSchedule: DailyTodoSchedule)

    @Update
    suspend fun updateSchedule(todoSchedule: DailyTodoSchedule)

    @Delete
    suspend fun deleteSchedule(todoSchedule: DailyTodoSchedule)

    @Query("SELECT * FROM todo_schedule_table WHERE todoScheduleId = :id")
    suspend fun getTodoScheduleById(id: String): DailyTodoSchedule

    @Query("SELECT * FROM todo_schedule_table")
    suspend fun getScheduleList(): List<DailyTodoSchedule>

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE todoScheduleId = :id ORDER BY schedule_date LIMIT 1")
    fun getScheduleWithTodo(id: String): Flow<ScheduleWithTodo>

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE todoScheduleId = :id ORDER BY schedule_date LIMIT 1")
    suspend fun getInfoScheduleWithTodo(id: String): ScheduleWithTodo

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE archive_value = 0 ORDER BY schedule_date")
    fun getScheduleWithTodoFlowList(): PagingSource<Int, ScheduleWithTodo>

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE archive_value = 0")
    fun getScheduleWithTodoFlow(): Flow<List<ScheduleWithTodo>>

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE archive_value = 1 ORDER BY schedule_date")
    fun getArchivedScheduleWithTodoFlowList(): PagingSource<Int, ScheduleWithTodo>

    @Transaction
    @Query("SELECT * FROM todo_schedule_table WHERE archive_value = 1")
    fun getArchiveScheduleWithTodoFlow(): Flow<List<ScheduleWithTodo>>
}

@Dao
interface TodoTaskDataDao {
    @Insert
    suspend fun insertTodoTask(todo: DailyTodoTask)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTodo(todo: DailyTodoTask)

    @Query("SELECT * FROM todotask_table ORDER BY position_todo DESC")
    fun getAllTodoTasks(): PagingSource<Int, DailyTodoTask>

    @Query("SELECT * FROM todotask_table WHERE parent_id = :id ORDER BY position_todo DESC")
    fun getAllTodoTasksByParentID(id: String): PagingSource<Int, DailyTodoTask>

    @Query("SELECT * FROM todotask_table WHERE parent_id = :id")
    fun getEmptyListCheckByParentID(id: String): Flow<List<DailyTodoTask>>

    @Query("DELETE FROM todotask_table")
    suspend fun deleteTodoTasks()

    @Query("SELECT * FROM todotask_table WHERE parent_id = :id")
    fun getRoutineTasksForCounter(id: String): Flow<List<DailyTodoTask>>

    @Query("SELECT * FROM todotask_table WHERE parent_id = :id ORDER BY todoTaskId")
    suspend fun getRoutineList(id: String): List<DailyTodoTask>

    @Query("SELECT * FROM todotask_table WHERE parent_id = :id ORDER BY todoTaskId")
    suspend fun getTodoList(id: String): List<DailyTodoTask>

    @Query("SELECT * FROM todotask_table")
    suspend fun getAllTodo(): List<DailyTodoTask>

    @Delete
    suspend fun deleteTodoTask(todo: DailyTodoTask)

    @Query("DELETE FROM todotask_table WHERE parent_id = :id")
    suspend fun deleteTodoTasksById(id: String)

    @Query("SELECT * FROM todotask_table WHERE todoTaskId = :id")
    suspend fun getTodoByID(id: Long): DailyTodoTask

    @Query("SELECT MAX(position_todo) FROM todotask_table")
    suspend fun getMaxPosition(): Long?

    @Query("SELECT MIN(position_todo) FROM todotask_table")
    suspend fun getMinPosition(): Long?
}
@Dao
interface AverageTodoTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAverageTime(averageTodoTime: AverageTodoTime)

    @Delete
    suspend fun deleteAverageTime(averageTodoTime: AverageTodoTime)

    @Query("SELECT * FROM averageTodoTime_table ORDER BY finishDate")
    fun getAverageTimeList(): Flow<List<AverageTodoTime>?>
}

@Dao
interface TagsDao {
    @Insert
    suspend fun insertTags(tag: Tag)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags_table ORDER BY tagsId DESC")
    fun getAllTags(): Flow<List<Tag>?>

    @Query("DELETE FROM tags_table")
    suspend fun deleteAllTags()

    @Query("DELETE FROM tags_table WHERE tagsId=:id")
    suspend fun deleteTagById(id: Long)
}
