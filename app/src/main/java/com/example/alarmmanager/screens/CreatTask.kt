package com.example.alarmmanager.screens

import CreateTaskViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.R
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreatTask : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                CreateTaskcom(
                    navController,
                    CreateTaskViewModel()
                )
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateTaskcom(
        navController: NavController,
        viewmodel: CreateTaskViewModel,
        taskid: String? = null,
        taskTitleArg: String? = null,
        taskDescriptionArg: String? = null,
        startDateArg: String? = null,
        endDateArg: String? = null,
        startTimeArg: String? = null,
        endTimeArg: String? = null,
        priorityArg: String? = null
    ) {
        var taskTitle by rememberSaveable { mutableStateOf(taskTitleArg ?: "") }
        var taskDescription by rememberSaveable { mutableStateOf(taskDescriptionArg ?: "") }
        var startDate by rememberSaveable { mutableStateOf(startDateArg ?: "") }
        var endDate by rememberSaveable { mutableStateOf(endDateArg ?: "") }
        var startTime by rememberSaveable { mutableStateOf(startTimeArg ?: "") }
        var endTime by rememberSaveable { mutableStateOf(endTimeArg ?: "") }
        var titleError by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedPriorityState by rememberSaveable { mutableStateOf(priorityArg ?: "") }
        val context = LocalContext.current
        val scrollState = rememberScrollState()
        var isloading by rememberSaveable { mutableStateOf(false) }
        val status by rememberSaveable { mutableStateOf("") }

        val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(id = R.color.light_pink),
            unfocusedBorderColor = colorResource(id = R.color.light_pink),
            cursorColor = colorResource(id = R.color.button_color),
            focusedLabelColor = colorResource(id = R.color.button_color)
        )

        fun showDatePicker(isStartDate: Boolean) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.format(calendar.time)
                    if (isStartDate) {
                        startDate = selectedDate
                    } else {
                        endDate = selectedDate
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        @SuppressLint("DefaultLocale")
        fun showTimePicker(isStartTime: Boolean) {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val selectedTime = timeFormat.format(calendar.time)
                    if (isStartTime) {
                        startTime = selectedTime
                    } else {
                        endTime = selectedTime
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        @Composable
        fun priorityButton(text: String, selectedCategory: String, onClick: (String) -> Unit) {

            val isSelected = selectedCategory == text

            val containerColor = if (isSelected) {
                when (text) {
                    "High" -> colorResource(id = R.color.dark_pink)
                    "Medium" -> colorResource(id = R.color.darkBlue)
                    "Low" -> colorResource(R.color.darkYellow)
                    else -> Color.White
                }

            } else {
                when (text) {
                    "High" -> colorResource(id = R.color.light_pink)
                    "Medium" -> colorResource(id = R.color.lightBlue)
                    "Low" -> colorResource(id = R.color.lightYellow)
                    else -> Color.White
                }
            }
            val contentColor = if (isSelected) {
                Color.White
            } else {
                when (text) {
                    "High" -> colorResource(id = R.color.dark_pink)
                    "Medium" -> colorResource(id = R.color.darkBlue)
                    "Low" -> colorResource(id = R.color.darkYellow)
                    else -> Color.White
                }
            }
            Button(
                onClick = { onClick(text) },

                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                )
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        fun convertStrToTime(timeStr:String): Date? {
            return try {
                SimpleDateFormat("HH:mm",Locale.getDefault()).parse(timeStr)
            } catch (e:ParseException)
            {
               null
            }

        }

        fun convertStrToDate(dateStr:String): Date? {
            return try {
                SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).parse(dateStr)
            } catch (e:ParseException)
            {
               null
            }

        }



        // Create task UI
        Box(modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.custom_white)))
        {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp)
                .background(colorResource(id = R.color.custom_white)),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {

                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color.DarkGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.navigateUp()
                        })
                Spacer(modifier = Modifier.width(16.dp) )
                Text(
                    text = if (taskid == null) "Create Task" else "Update Task",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
            }


            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Task Title",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = taskTitle,
                onValueChange = { taskTitle = it },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                maxLines = 1,
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text(text = "Title") })
            titleError?.let {
                Text(
                    text = it, fontSize = 12.sp, color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Description",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                shape = RoundedCornerShape(18.dp),
                textStyle = TextStyle(fontSize = 18.sp),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Write a note...") },
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = ("Deadlines"),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calender",
                    tint = colorResource(
                        id = R.color.button_color
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = startDate.ifEmpty { "Start Date" },
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        showDatePicker(true)
                    })
                Text(text = "  -  ", fontWeight = FontWeight.Bold)

                Text(text = endDate.ifEmpty { "End Date" },
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        showDatePicker(false)
                    })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.clock),
                        contentDescription = "clock",
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = startTime.ifEmpty { "Start Time" },
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            showTimePicker(true)
                        })
                    Text(text = "  -  ", fontWeight = FontWeight.Bold)

                    Text(text = endTime.ifEmpty { "End Time" },
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            showTimePicker(false)
                        })
                }


            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Priorities",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                priorityButton(text = "High", selectedPriorityState) {
                    selectedPriorityState = it
                }
                priorityButton(
                    text = "Medium", selectedPriorityState
                ) {
                    selectedPriorityState = it
                }
                priorityButton(text = "Low", selectedPriorityState) {
                    selectedPriorityState = it
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    titleError = if (taskTitle.isEmpty()) "Enter the title" else null

                    if (convertStrToTime(endTime)!! < convertStrToTime(startTime)){
                        Toast.makeText(context, "Start Time shouldn't be after end Time",Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (convertStrToDate(endDate)!! < convertStrToDate(startDate)){
                        Toast.makeText(context, "Start Date shouldn't be after end Date",Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (taskTitle.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()) {
                        Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG)
                            .show()
                        return@Button
                    } else {
                        isloading = true

                        if (taskid != null) {


                            if (convertStrToTime(endTime)!! < convertStrToTime(startTime)){
                                Toast.makeText(context, "Start Time shouldn't be after end Time",Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (convertStrToDate(endDate)!! < convertStrToDate(startDate)) {
                                Toast.makeText(
                                    context,
                                    "Start Date shouldn't be after end Date",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }


                            if (taskTitle.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()) {
                                Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG)
                                    .show()
                                return@Button
                            }
                            viewmodel.updateTask(
                                taskid = taskid,
                                taskTitle = taskTitle,
                                taskDescription = taskDescription,
                                startDate = startDate,
                                endDate = endDate,
                                startTime = startTime,
                                endTime = endTime,
                                taskPriority = selectedPriorityState,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Task updated successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                    isloading = false

                                },
                                onFailure = {
                                    isloading = false
                                    Toast.makeText(
                                        context,
                                        "Error updating task",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else
                            viewmodel.saveTask(
                                taskid = UUID.randomUUID().toString(),
                                taskTitle = taskTitle,
                                taskDescription = taskDescription,
                                startDate = startDate,
                                endDate = endDate,
                                startTime = startTime,
                                endTime = endTime,
                                taskPriority = selectedPriorityState,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Task added successfully",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    navController.navigate("home") {
                                        popUpTo("home") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onFailure = {
                                    isloading = false
                                    Toast.makeText(context, "Error saving task", Toast.LENGTH_LONG)
                                        .show()
                                },
                                status
                            )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color))
            ) {
                Text(
                    if (taskid != null) "Update Task" else "Create Task",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {

            }
        }}
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            if (isloading) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            }
        }
    }
}