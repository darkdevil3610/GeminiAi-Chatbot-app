package com.example.androidgeminiaichatbot.presentation

import HomeViewModel
import android.annotation.SuppressLint
import android.app.Instrumentation.ActivityResult
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.androidgeminiaichatbot.Utils.UriCustomSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.contracts.contract

@Composable
fun AppContent(homeViewModel: HomeViewModel = viewModel()) {

    val coroutineScope = rememberCoroutineScope()
    val uiState = homeViewModel.uiState.collectAsState()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    HomeScreen(
        uiState.value
    ) { inputText, selectedImages ->

        coroutineScope.launch {

            //convert uri to Bitmap
            val selectedImageBitmaps = selectedImages.mapNotNull {
                val imageRequest = imageRequestBuilder
                    .data(it)
                    .size(size = 786)
                    .build()

                val imageResult = imageLoader.execute(imageRequest)
                if (imageResult is SuccessResult) {
                    return@mapNotNull (imageResult.drawable as BitmapDrawable).bitmap
                } else {
                    return@mapNotNull null
                }
            }
            print("inputText $inputText")
            homeViewModel.sendQuery(inputText, selectedImageBitmaps)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeScreenState = HomeScreenState.initial,
    onSendClicked: (String, List<Uri>) -> Unit
) {

    var userQuestion by rememberSaveable() {
        mutableStateOf("")
    }

    val imageUris = rememberSaveable(saver = UriCustomSaver()) {
        mutableStateListOf()
    }


    val pictureMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                imageUris.add(it)
            }
        }
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gemini AI ChatBot") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },
        bottomBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            //Launch Picker
                            pictureMediaLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Image"
                        )
                    }


                    //Question TextField
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = {
                            userQuestion = it
                        },
                        label = {
                            Text(text = "Enter Search text")
                        },
                        placeholder = {
                            Text(text = "Upload image & Ask Question.")
                        },
                        modifier = Modifier.fillMaxWidth(0.82f),
                    )

                    //Send Button
                    IconButton(
                        onClick = {
                            if (userQuestion.isNotEmpty()) {
                                onSendClicked(userQuestion, imageUris)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }

                AnimatedVisibility(visible = true) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(modifier = Modifier.padding(8.dp)) {
                            items(imageUris) { imageUri ->
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(4.dp)
//                                            .requiredSize(50.dp)
                                    )
                                    TextButton(
                                        onClick = { imageUris.remove(imageUri) }
                                    ) {
                                        Text(text = "Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            when (uiState) {
                is HomeScreenState.initial -> {
                }

                is HomeScreenState.loading -> {
                    Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center, content = {
                        CircularProgressIndicator()
                    })
                }

                is HomeScreenState.Success -> {
                    Card(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = uiState.successMessage,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is HomeScreenState.Error -> {
                    Card(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {

                }
            }
        }
    }

}