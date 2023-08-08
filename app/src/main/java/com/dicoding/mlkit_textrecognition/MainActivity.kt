package com.dicoding.mlkit_textrecognition

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dicoding.mlkit_textrecognition.ui.theme.MLKitTextRecognitionTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitTextRecognitionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        CameraScreen()
    } else {
        Column {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Composable
fun CameraScreen() {

    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }

    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.BottomCenter
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
//                    setBackgroundColor(Color.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also { previewView ->
                    startTextRecognition(
                        context = context,
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onDetectedTextUpdated = ::onTextUpdated
                    )
                }
            }
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White)
                .padding(16.dp),
            text = detectedText,
        )
    }
}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MLKitTextRecognitionTheme {
        MainScreen()
    }
}