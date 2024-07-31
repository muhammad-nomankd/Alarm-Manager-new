package com.example.alarmmanager.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import coil.compose.rememberAsyncImagePainter
import com.example.alarmmanager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileContent(navControler = NavController(LocalContext.current))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileContent(navControler: NavController) {

        var userName by rememberSaveable { mutableStateOf("") }
        var userEmail by rememberSaveable { mutableStateOf("") }
        var profileImageUrl by rememberSaveable { mutableStateOf("") }
        val context = LocalContext.current
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val coroutinescope = rememberCoroutineScope()
        var isLoading by rememberSaveable { mutableStateOf(false) }
        var name by rememberSaveable { mutableStateOf("") }
        var isNameUpdated by rememberSaveable { mutableStateOf(false) }


        LaunchedEffect(Unit,isNameUpdated) {
            isLoading = true
            firestore.collection("User").document(userId)
                .get().addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    profileImageUrl = document.getString("imageUrl") ?: ""
                    userEmail = document.getString("email") ?: ""
                }.addOnSuccessListener {
                    isLoading = false
                    Log.d("user mail", userEmail)
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to get user details", Toast.LENGTH_SHORT).show()
                    isLoading = false

                }


        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Gray, strokeWidth = 1.dp)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.Top, modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {


                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back arrow",
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 32.dp, start = 18.dp)
                        .clickable { navControler.navigateUp() })

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = rememberAsyncImagePainter(model = profileImageUrl.ifEmpty { R.drawable.person }),
                    contentDescription = "profile image",
                    modifier = Modifier
                        .padding(start = 32.dp)
                        .shadow(0.5.dp, CircleShape)
                        .clip(CircleShape)
                        .size(75.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (userName.isEmpty() && isNameUpdated.not()) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(
                                text = "Enter your name",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = TextStyle(color = Color.DarkGray, fontSize = 16.sp),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "person icon",
                                tint = Color.Gray
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.Gray,
                            focusedBorderColor = colorResource(id = R.color.light_pink),
                            unfocusedBorderColor = colorResource(id = R.color.light_pink),
                            cursorColor = colorResource(id = R.color.button_color),
                            focusedLabelColor = colorResource(id = R.color.light_pink),
                            unfocusedLabelColor = colorResource(id = R.color.light_pink)
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                        onClick = {

                            if (name.isNotEmpty()) {
                                isLoading = true
                                firestore.collection("User").document(userId)
                                    .update("name", name)
                                    .addOnSuccessListener {
                                        isNameUpdated = true
                                        Toast.makeText(
                                            context,
                                            "Name updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isLoading = false
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to update name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isLoading = false

                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter your name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                        Text(text = "Add Name", fontSize = 12.sp, color = Color.White)
                    }


                } else {
                    Text(
                        userName, fontSize = 16.sp, color = Color.Black, modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    userEmail, fontSize = 16.sp, color = Color.Black, modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        coroutinescope.launch {
                            isLoading = true
                            delay(1000)
                            FirebaseAuth.getInstance().signOut()
                            withContext(Dispatchers.Main) {
                                if (FirebaseAuth.getInstance().currentUser == null) {
                                    navControler.navigate("signup")
                                    isLoading = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Sig out failed. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isLoading = false
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color)),
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Sign Out", fontSize = 16.sp
                    )
                }
            }
        }
    }
}
