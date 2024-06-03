package org.ethereumhpone.chat.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalZeroShutterLag::class) @Composable
fun CameraPreview(
    modifier: Modifier,
    onPhotoCaptured: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
            .build()
    }


    DisposableEffect(key1 = cameraSelector) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture,
                    preview
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.get().unbindAll()
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        AndroidView(
            modifier = Modifier
                .matchParentSize()
                .align(Alignment.Center),
            factory = {
                previewView
                    .apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setBackgroundColor(Color.BLACK) // while loading
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
            }
        )
        
        IconButton(
            modifier = Modifier
                .offset(10.dp, 10.dp)
                .align(Alignment.TopStart),
            onClick = {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } }
        ) {
            Icon(
                imageVector = Icons.Outlined.FlipCameraAndroid,
                contentDescription = "",
                modifier = Modifier
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f))
                    .padding(5.dp)
                    .fillMaxSize(),
                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        // Take picture
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .offset(0.dp, (-10).dp)
                .clip(CircleShape)
                .size(52.dp)
                .background(androidx.compose.ui.graphics.Color.White)
                .clickable { takePicture(context, imageCapture, onPhotoCaptured) }
        )

    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (Uri) -> Unit,
) {


    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        //put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(error: ImageCaptureException)
            {
                error.printStackTrace()

            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri

                savedUri?.let {
                    onPhotoCaptured(it)
                }
            }
        })
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun previewButton() {
    Box(Modifier.size(400.dp)) {
        IconButton(
            modifier = Modifier
                .offset(15.dp, 15.dp)
                .align(Alignment.TopStart),
            onClick = {}
        ) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f))
                    .padding(5.dp)
                    .fillMaxSize(),
                imageVector = Icons.Outlined.FlipCameraAndroid,
                contentDescription = "",

                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        // Take picture
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .offset(0.dp, -10.dp)
                .clip(CircleShape)
                .size(80.dp)
                .background(androidx.compose.ui.graphics.Color.White)
                .clickable {  }
        )
    }
}

