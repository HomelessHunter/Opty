package com.example.tasksapp.maintask

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tasksapp.R
import com.example.tasksapp.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class MainTaskViewModel(val database: TaskDataDao,
                        private val tagsDao: TagsDao,
                        val subtaskDataDao: SubtaskDataDao,
                        val todaySessionDao: TodaySessionDao,
                        private val app: Application) : AndroidViewModel(app) {

   private var _tasks = database.getAllTasks().flowOn(Dispatchers.IO)
    val tasks = _tasks

    val totalTime = todaySessionDao.getAllSessions().map {it.sum()}.flowOn(Dispatchers.Default)

    val activeProjects = database.getActiveProjects().flowOn(Dispatchers.IO).map { it.size }.flowOn(Dispatchers.Default)
    val finishedProjects = database.getFinishedProjects().flowOn(Dispatchers.IO).map { it.size }.flowOn(Dispatchers.Default)

    val finishedTasks = database.getAllFinishedTasks().flowOn(Dispatchers.IO)

    private val _isTaskRestored = MutableLiveData<Long?>()
        val isTaskRestored: LiveData<Long?> = _isTaskRestored

    private val _showStats = MutableLiveData<Long?>()
        val showStats: LiveData<Long?> = _showStats

    private val _taskId = MutableLiveData<Long?>()
        val taskId: LiveData<Long?>
            get() = _taskId

    private val _showOptionsDialog = MutableLiveData<Long?>()
       val showOptionsDialog: LiveData<Long?> = _showOptionsDialog

    private val _taskText = MutableLiveData<String>()

   suspend fun getImportantProjects(): List<Task> {
       val list = database.getImportantProjects()
       return withContext(Dispatchers.Default) {
           list.filter { it.isFinished == 0 }
       }
   }

    val tagsList = tagsDao.getAllTags().flowOn(Dispatchers.IO)

    private val _tagsTextList = MutableLiveData<List<String>?>()
    val tagsTextList: LiveData<List<String>?> = _tagsTextList
    private val _tagL = MutableStateFlow<List<Tag>>(listOf())
        private val tagL: StateFlow<List<Tag>> = _tagL

    private val _tagsText = MutableLiveData<String>()

    private val _tagsColor = MutableLiveData<Int?>()

    private val _tagName = MutableLiveData<String?>()
        val tagName: LiveData<String?> = _tagName


    fun setTagName(name: String) {
        _tagName.value = name
    }

    fun resetTagName() {
        _tagName.value = null
    }

    suspend fun getTagList() {
        tagsList.collectLatest { list ->
            _tagsTextList.value = withContext(Dispatchers.Default) {
                list?.map { it.tagName } ?: listOf(app.getString(R.string.no_tags))
            }
            list?.let {
                _tagL.value = it
            }
        }
    }

    fun insertTag() {
        viewModelScope.launch {
            val tag = Tag(tagName = _tagsText.value?.toString() ?: return@launch,
                colorValue = _tagsColor.value ?: 0)
            tagsDao.insertTags(tag)
            getTagList()
            _tagsColor.value = null
        }
    }

    fun getTagsText(text: String) {
        _tagsText.value = text
    }

    fun getTagsColor(color: Int) {
        _tagsColor.value = color
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            val tasks = database.getTasksByTag(tag.tagName)
            withContext(Dispatchers.Default) {
                tasks.map { task ->
                    task.tag = "No tag"
                    task.bookMarkColor = 0
                    database.update(task)
                }
            }
            tagsDao.deleteTag(tag)
        }
    }

    fun updateTaskState(id: Long) {
        viewModelScope.launch {
            val task = getTaskById(id)
            if (task.isFinished == 0) {
                task.isFinished = 1
            } else {
                task.isFinished = 0
            }
            database.update(task)
        }
    }

    fun updateTaskBookmark(id: Long, tagPosition: Int) {
        viewModelScope.launch {
            val task = getTaskById(id)
            withContext(Dispatchers.Default) {
                task.tag = tagsTextList.value?.get(tagPosition) ?: "No Tag"
                task.bookMarkColor = tagL.value[tagPosition].colorValue
            }
            database.update(task)
            Log.e("Tag_Test", "Test")
        }
    }

    fun setImportance(id: Long, importance: Int) {
        viewModelScope.launch {
            val task = getTaskById(id)
            task.importance = importance
            database.update(task)
        }
    }

    fun showDialog(id: Long) {
        _showOptionsDialog.value = id
    }

    fun resetShowDialog() {
        _showOptionsDialog.value = null
    }

    fun onGetTaskText(text: String) {
        _taskText.value = text
    }

    fun setShowStats(id: Long) {
        _showStats.value = id
    }

    fun resetShowStats() {
        _showStats.value = null
    }

    fun setRestoreTask(value: Long) {
        _isTaskRestored.value = value
    }

    fun resetRestoredTask() {
        _isTaskRestored.value = null
    }

    fun getTaskId(id: Long) {
        _taskId.value = id
    }
    fun onShipTaskId() {
        _taskId.value = null
    }

    private suspend fun getTaskById(id: Long): Task =
        withContext(Dispatchers.IO) {
            database.getTaskWithKey(id)
        }

    fun insertTask() {
        viewModelScope.launch {
            val newTask = Task(taskTag = _taskText.value ?: return@launch)
            database.insert(newTask)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            database.delete(id)
        }
    }

    fun deleteSubtasks(id: Long) {
        viewModelScope.launch {
            subtaskDataDao.deleteSubtasksById(id)
        }
    }
}
