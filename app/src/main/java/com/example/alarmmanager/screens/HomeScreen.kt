package com.example.alarmmanager.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.alarmmanager.R
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.dataclasses.Weather
import com.example.alarmmanager.viewmodels.GetTaskViewModel
import com.example.alarmmanager.viewmodels.WeatherViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HomeScreen : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreenUi(navController = NavController(LocalContext.current), LocalContext.current)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition")
    @Composable
    fun HomeScreenUi(navController: NavController, context: Context) {
        var profileImageUrl by rememberSaveable { mutableStateOf("") }
        var userName by rememberSaveable { mutableStateOf("") }
        var userEmail by rememberSaveable { mutableStateOf("") }
        var selectedCategoryState by remember { mutableStateOf("All") }
        val firestore = FirebaseFirestore.getInstance()
        val viewmodel: GetTaskViewModel = viewModel()
        val weatherViewModel: WeatherViewModel = viewModel()
        val tasks by viewmodel.filteredTasks.observeAsState(emptyList())
        val nonfilterTasks by viewmodel.tasksForUpCommingCategory.collectAsState()
        var showPopUp by rememberSaveable { mutableStateOf(false) }
        var currenttask by rememberSaveable { mutableStateOf("") }
        var taskStatus by rememberSaveable { mutableStateOf("") }
        var deleteTask by rememberSaveable { mutableStateOf(false) }
        var isLoading by rememberSaveable { mutableStateOf(false) }
        var selectedWeatherLocation by rememberSaveable { mutableStateOf("") }
        val currentTemprature by weatherViewModel.temperature.observeAsState()
        val currentWeatherDescription by weatherViewModel.weatherDescription.observeAsState()
        val currentHumidity by weatherViewModel.weatherHumidity.observeAsState()


        // Getting User Detail from FireStore
        LaunchedEffect(Unit) {
            isLoading = true
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .get().addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    profileImageUrl = document.getString("imageUrl") ?: ""
                    userEmail = document.getString("email") ?: ""
                }
                .addOnSuccessListener {
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to get user details", Toast.LENGTH_SHORT).show()
                    isLoading = false

                }

            // Fetch tasks for UpComming Category
            viewmodel.filterTasksForUpCommingCategory("In Progress and Pending")

            // Fetching Weather Location and data from FireStore
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        selectedWeatherLocation = document.getString("location") ?: ""
                    }
                    if (selectedWeatherLocation.isNotBlank()) {
                        weatherViewModel.fetchWeather(selectedWeatherLocation)
                    } else {
                        Log.e("Weather Error", "Selected weather location is blank")
                    }
                }


        }

        // Getting filtered Tasks based on selected Category Button
        LaunchedEffect(selectedCategoryState) {
            viewmodel.filterTasks(selectedCategoryState)
        }

        LaunchedEffect(Unit) {


        }
        //Delete task logic
        fun deleteTask(taskId: String, status: String, context: Context) {
            val db = FirebaseFirestore.getInstance()
            val uId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val taskRef = db.collection("User").document(uId).collection("tasks")
            taskRef.whereEqualTo("taskId", taskId)
                .get()
                .addOnSuccessListener { querySnapShot ->
                    for (document in querySnapShot.documents) {
                        if (status == "Completed") {
                            taskRef.document(document.id).delete().addOnSuccessListener {
                                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                                deleteTask = true
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT)
                                    .show()

                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Task has not completed yet.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }

        // Refresh tasks Category after deletion
        val coroutineScope = rememberCoroutineScope()
        if (deleteTask) {
            coroutineScope.launch {
                viewmodel.filterTasks(selectedCategoryState)
                deleteTask = false
            }
        }

        // PopUp for Deleting task
        if (showPopUp) {
            AlertDialog(onDismissRequest = { showPopUp = false },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colorResource(
                    id = R.color.task_color
                ),
                confirmButton = {
                    Button(
                        onClick = {
                            showPopUp = false
                            deleteTask(currenttask, taskStatus, context)

                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(
                                id = R.color.button_color
                            )
                        )
                    ) {
                        Text(
                            text = "Delete",
                            color = Color.White, modifier = Modifier
                        )
                    }

                },
                title = {
                    Text(
                        text = "Delete Task",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you  want to delete this task?",
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                dismissButton = {
                    Button(
                        onClick = { showPopUp = false },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            colorResource(id = R.color.darkBlue)
                        )
                    ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                })
        }

        // Category Button for selecting specific category like All, In Progress or Completed
        @Composable
        fun categoryButton(text: String, selectedCategory: String, onClick: (String) -> Unit) {
            val isSelected = selectedCategory == text
            Button(
                onClick = { onClick(text) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) colorResource(id = R.color.button_color) else Color.White,
                    contentColor = if (isSelected) Color.White else colorResource(id = R.color.button_color),
                )
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Adding task Icons to each task
        @Composable
        fun getTaskIcon(title: String): Int {
            return when {
                title.lowercase(Locale.ROOT)
                    .contains("office", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("work", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("job", ignoreCase = true) -> R.drawable.office

                title.lowercase(Locale.ROOT).contains("home", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("house", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("building", ignoreCase = true) -> R.drawable.house

                title.lowercase(Locale.ROOT)
                    .contains("shopping", ignoreCase = true) -> R.drawable.shopping

                title.lowercase(Locale.ROOT).contains("food", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("lunch", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("dinner", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("breakfast", ignoreCase = true) -> R.drawable.lunch

                title.lowercase(Locale.ROOT)
                    .contains("medicine", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("tablets", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("doctor", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("hospital", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("appointment", ignoreCase = true) -> R.drawable.medicine

                title.lowercase(Locale.ROOT).contains("Yoga", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("Gym", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("Exercise", ignoreCase = true) -> R.drawable.house

                else -> R.drawable.taskicon
            }
        }

        // Task Item for LazyRow
        @Composable
        fun taskItem(task: Task, longClick: () -> Unit, onClick: () -> Unit) {
            Card(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .width(160.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { onClick() },
                        onLongClick = { longClick() },
                    ),
                backgroundColor = Color.White,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = getTaskIcon(task.title)),
                        contentDescription = "Task Icon",
                        Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormater(task.startDate),
                        fontSize = 16.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.status,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = when (task.status) {
                                "Completed" -> colorResource(id = R.color.green)
                                "In Progress" -> colorResource(id = R.color.darkYellow)
                                "Pending" -> Color.Black
                                else -> Color.Gray
                            },
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        Card(
                            shape = RoundedCornerShape(4.dp),
                            backgroundColor = when (task.priority) {
                                "High" -> colorResource(id = R.color.light_pink)
                                "Medium" -> colorResource(id = R.color.lightBlue)
                                "Low" -> colorResource(id = R.color.lightYellow)
                                else -> Color.White
                            },
                            contentColor = when (task.priority) {
                                "High" -> colorResource(id = R.color.dark_pink)
                                "Medium" -> colorResource(id = R.color.darkBlue)
                                "Low" -> colorResource(id = R.color.darkYellow)
                                else -> Color.Black
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                text = task.priority,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (task.priority) {
                                    "High" -> colorResource(id = R.color.dark_pink)
                                    "Medium" -> colorResource(id = R.color.darkBlue)
                                    "Low" -> colorResource(id = R.color.darkYellow)
                                    else -> Color.Black
                                },
                                modifier = Modifier.padding(
                                    start = 4.dp, end = 4.dp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Task Item for UpComing tasks
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun upComingTasksItem(task: Task, longClick: () -> Unit, onClick: () -> Unit) {
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .combinedClickable(
                        onClick = { onClick() },
                        onLongClick = { longClick() },
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .height(90.dp)
                            .width(4.dp)
                            .background(
                                colorResource(id = R.color.box_color),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .width(IntrinsicSize.Max)
                                        .widthIn(0.dp, 150.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Card(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .wrapContentWidth(),
                                    backgroundColor = when (task.priority) {
                                        "Low" -> colorResource(id = R.color.lightYellow)
                                        "Medium" -> colorResource(id = R.color.lightBlue)
                                        "High" -> colorResource(id = R.color.light_pink)
                                        else -> Color.White
                                    },
                                    contentColor = when (task.priority) {
                                        "Low" -> colorResource(id = R.color.darkYellow)
                                        "Medium" -> colorResource(id = R.color.darkBlue)
                                        "High" -> colorResource(id = R.color.dark_pink)
                                        else -> Color.White
                                    }
                                ) {
                                    Text(
                                        text = task.priority,
                                        fontSize = 12.sp,
                                        color = when (task.priority) {
                                            "High" -> colorResource(id = R.color.dark_pink)
                                            "Medium" -> colorResource(id = R.color.darkBlue)
                                            "Low" -> colorResource(id = R.color.darkYellow)
                                            else -> Color.Black
                                        },
                                        modifier = Modifier.padding(
                                            start = 6.dp,
                                            end = 6.dp,
                                            top = 3.dp,
                                            bottom = 3.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = task.description,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = timeFormater(task.startTime) + " - " + timeFormater(task.endTime),
                            color = Color.DarkGray,
                            fontSize = 16.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dateFormater(task.startDate),
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .wrapContentWidth(Alignment.End)
                        )
                        Text(
                            text = "To", color = Color.DarkGray,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = dateFormater(task.endDate),
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .wrapContentWidth(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.status,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = when (task.status) {
                                "Completed" -> colorResource(id = R.color.green)
                                "In Progress" -> colorResource(id = R.color.darkYellow)
                                "Pending" -> Color.Black
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
        }


        // MainHome Screen UI
        LazyColumn(
            modifier = Modifier
                .background(colorResource(id = R.color.custom_white))
                .fillMaxSize()

        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Box(modifier = Modifier
                        .clickable {
                            navController.navigate("profile")
                        }) {
                        Image(
                            painter = rememberAsyncImagePainter(model = profileImageUrl.ifEmpty { R.drawable.person }),
                            contentDescription = "Profile Image",
                            Modifier
                                .clip(CircleShape)
                                .size(60.dp)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )

                        // Show badge if user name is empty show a dot on profile icon
                        if (userName.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    if (selectedWeatherLocation.isEmpty()) {
                        Text(
                            text = "Select Location to view weather.",
                            modifier = Modifier.clickable { navController.navigate("locationDetailScreen") })
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                text = selectedWeatherLocation.ifEmpty { "Loading..." },
                                fontSize = 16.sp,
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .clickable { navController.navigate("locationDetailScreen") }

                            )
                            Spacer(modifier = Modifier.height(4.dp))





                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (currentTemprature != null) "${currentTemprature!!.toInt()}°" else "Loading...",
                                fontFamily = if (currentTemprature != null) FontFamily(Font(R.font.roboto_light)) else FontFamily.Default,
                                fontSize = if (currentTemprature != null) 42.sp else 12.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Light
                            )



                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentWeatherDescription ?: "Loading...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Humidity ${currentHumidity ?: ""}%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }


                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
                Text(
                    "Categories",
                    color = Color.DarkGray,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 32.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    categoryButton(text = "All", selectedCategoryState) {
                        selectedCategoryState = it
                    }
                    categoryButton(
                        text = "In Progress", selectedCategoryState
                    ) {
                        selectedCategoryState = it
                    }
                    categoryButton(text = "Completed", selectedCategoryState) {
                        selectedCategoryState = it
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }


            // LazyRow for horizontal scrollable tasks
            item {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp)
                ) {
                    items(tasks, key = { it.taskId }) { task ->
                        taskItem(task, longClick = {
                            currenttask = task.taskId
                            showPopUp = true
                            taskStatus = task.status

                        }, onClick = {
                            navController.navigate("createTask?taskId=${task.taskId}&taskTitle=${task.title}&taskDescription=${task.description}&startDate=${task.startDate}&endDate=${task.endDate}&startTime=${task.startTime}&endTime=${task.endTime}&priority=${task.priority}")
                        })
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // View all tasks
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "View all",
                        color = colorResource(id = R.color.button_color),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = 32.dp)
                            .align(Alignment.CenterEnd)
                            .clickable {
                                navController.navigate("taskListScreen")
                            })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // UpComing tasks List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colorResource(
                        id = R.color.task_color
                    )
                ) {
                    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)) {
                        Text(
                            text = "Upcoming Tasks",
                            fontSize = 22.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        nonfilterTasks.forEach { task ->
                            upComingTasksItem(task,
                                longClick = {
                                    currenttask = task.taskId
                                    showPopUp = true
                                    taskStatus = task.status

                                }, onClick = {
                                    navController.navigate("createTask?taskId=${task.taskId}&taskTitle=${task.title}&taskDescription=${task.description}&startDate=${task.startDate}&endDate=${task.endDate}&startTime=${task.startTime}&endTime=${task.endTime}&priority=${task.priority}")
                                })


                        }
                    }
                }
            }

        }

        Box(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(32.dp))

            // Floating Action Button for Creating task Screen navigation
            FloatingActionButton(
                onClick = { navController.navigate("createTask") },
                containerColor = colorResource(id = R.color.button_color),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(48.dp)

            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.size(32.dp)
                )
            }

        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (isLoading) {
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

    // Formating Date
    private fun dateFormater(dateString: String): String {
        val fetchedDateFormate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return try {
            val date = fetchedDateFormate.parse(dateString)
            val day = date?.let { SimpleDateFormat("d", Locale.getDefault()).format(it).toInt() }
            val suffix = when (day) {
                1, 21, 31 -> "st"
                2, 22 -> "nd"
                3, 23 -> "rd"
                else -> "th"
            }
            date?.let { sdf.format(it) } + suffix
        } catch (e: Exception) {
            e.printStackTrace()
            "Invalid date"
        }
    }

    // Formating Time

    private fun timeFormater(timeString: String): String {

        val fetchedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dDF = SimpleDateFormat("h.mm a", Locale.getDefault())

        return try {
            val time = fetchedTime.parse(timeString)
            dDF.format(time ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            "invalid formated time"
        }
    }


}