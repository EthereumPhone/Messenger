package org.ethereumhpone.chat.components

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    selectedUris: List<Uri>,
    onToggleSelection: (Uri) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current

    // 1. Pick the correct permission constant for your API level
    val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    // 2. Track whether we have it
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, readPerm) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // 3. Create a launcher that updates `hasPermission`
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // 4. If we don’t have it, show a prompt; otherwise show the grid
    if (!hasPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Permission required to load images")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { permLauncher.launch(readPerm) }) {
                Text("Grant Permission")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Select Images") },
                actions = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Done")
                    }
                }
            )
            CustomImagePicker(
                selectedUris = selectedUris,
                onToggleSelection = onToggleSelection
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomImagePicker(
    selectedUris: List<Uri>,
    onToggleSelection: (Uri) -> Unit
) {
    val context = LocalContext.current
    val allImages by produceState(initialValue = emptyList<Uri>(), context) {
        val uris = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                uris += ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }
        }
        value = uris
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
       items(
           allImages
       ) {  uri ->
           Box(
               modifier = Modifier
                   .aspectRatio(1f)
                   .clip(RoundedCornerShape(8.dp))
                   .clickable { onToggleSelection(uri) }
           ) {
               AsyncImage(
                   model = uri,
                   contentDescription = null,
                   modifier = Modifier.fillMaxSize(),
                   contentScale = androidx.compose.ui.layout.ContentScale.Crop
               )
               if (uri in selectedUris) {
                   Box(
                       modifier = Modifier
                           .matchParentSize()
                           .background(Color.Black.copy(alpha = 0.4f))
                   )
                   Icon(
                       Icons.Default.CheckCircle,
                       contentDescription = "Selected",
                       tint = Color.White,
                       modifier = Modifier
                           .align(Alignment.TopEnd)
                           .padding(6.dp)
                   )
               }
           }
       }
    }
}