package com.example.alarmmanager.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.repositories.GetTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GetTaskViewModel : ViewModel() {
    private val repository = GetTaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filteredTasks = MutableLiveData<List<Task>>(emptyList())
    var filteredTasks: LiveData<List<Task>> = _filteredTasks

    private val _filteredTasksofMonth = MutableStateFlow<List<Task>>(emptyList())
    var filteredTasksofMonth: StateFlow<List<Task>> = _filteredTasksofMonth

    private val _filteredTasksofDay = MutableLiveData<List<Task>>(emptyList())
    var filteredTasksofDay: LiveData<List<Task>> = _filteredTasksofDay

    var isloading by mutableStateOf(false)



    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            try {
                val taskList = repository.getTask()
                updateTaskStatuses(taskList)
                _tasks.value = taskList
                _filteredTasks.value = taskList
            } catch (e: Exception) {
                emptyList<Task>()
            }
        }
    }


    private fun updateTaskStatuses(taskList: List<Task>) {
        val dateFormats = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        try {

            taskList.forEach { task ->
                val endT = dateFormats.parse("${task.endDate} ${task.endTime}")?.time ?: 0L
                val startT = dateFormats.parse("${task.startDate} ${task.startTime}")?.time ?: 0L
                task.status =
                    if (currentTime > endT) "Completed" else if (currentTime in (startT + 1)..<endT) "In Progress" else "Pending"
            }
        } catch (e: Exception) {
            Log.d("problem", "unable to filter tasks")
        }
    }


    suspend fun filterTasks(category: String) {
        viewModelScope.launch {

            try {
                val taskrepo = repository.getTask()
                updateTaskStatuses(taskrepo)
                val taskfilter = when (category) {
                    "All" -> taskrepo
                    "In Progress" -> taskrepo.filter { it.status == category }
                    "Completed" -> taskrepo.filter { it.status == category }
                    else -> taskrepo
                }
                _filteredTasks.value = taskfilter
            } catch (e: Exception) {
                emptyList<Task>()
            }
        }
    }

    fun fetchTaskForDay(day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            isloading = true
            val filterList = _tasks.value.filter {
                val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar = Calendar.getInstance()
                taskDate?.let {
                    calendar.time = it
                }
                val isSameDay = calendar.get(Calendar.DAY_OF_MONTH) == day
                        && calendar.get(Calendar.MONTH) + 1 == month
                        && calendar.get(Calendar.YEAR) == year
                isSameDay
            }
            _filteredTasksofDay.postValue(filterList)
        }
    }

    fun fetchTaskForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            isloading = true
            val filterListForMonth = _tasks.value.filter {
                val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar = Calendar.getInstance()
                taskDate?.let {
                    calendar.time = it
                }
                val isSameMonth = calendar.get(Calendar.MONTH) + 1 == month && calendar.get(Calendar.YEAR) == year
                isSameMonth.also { isloading = false }
            }
            _filteredTasksofMonth.value=(filterListForMonth)
        }
    }

}

