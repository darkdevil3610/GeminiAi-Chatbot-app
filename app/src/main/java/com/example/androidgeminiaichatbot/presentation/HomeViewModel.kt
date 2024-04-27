import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidgeminiaichatbot.BuildConfig
import com.example.androidgeminiaichatbot.presentation.HomeScreenState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.initial)
    val uiState = _uiState.asStateFlow()

    private var generativeModel: GenerativeModel

    init {
        val config = generationConfig {
            temperature = 0.7f  //0 to 1
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = BuildConfig.apiKey,
            generationConfig = config
        )
    }

    fun sendQuery(
        userInputText: String,
        selectedImages: List<Bitmap>
    ) {
        _uiState.value = HomeScreenState.loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = "Take a look at pictures and answer the following questions $userInputText"
//                val prompt = "$userInputText"
                val inputContent = content() {
                    selectedImages.forEach {
                        image(it) //images
                    }
                    text(prompt) //questionText
                }

                var outputContent = ""
                generativeModel.generateContentStream(inputContent).collect {
                    print("outputContent : $outputContent")
                    outputContent += it.text
                    _uiState.value = HomeScreenState.Success(outputContent)
                }

            } catch (e: Exception) {
                _uiState.value =
                    HomeScreenState.Error(e.localizedMessage ?: "Error generating the content")
            }

        }

    }
}