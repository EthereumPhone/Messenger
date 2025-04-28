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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    attachments: List<Attachment>,
    onToggleAttachment: (Attachment) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, readPerm) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .background(dgenBlack)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = onDone) {
                Icon(
                    painter = painterResource(R.drawable.backicon),
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }
            Text(
                text = "SELECT MEDIA",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )
            Spacer(Modifier.size(24.dp))

        }
        CustomImagePicker(
            attachments = attachments,
            onToggleAttachment = onToggleAttachment
        )
    }

    /*
    if (!hasPermission) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Permission required to load attachments")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { permLauncher.launch(readPerm) }) {
                Text("Grant Permission")
            }
        }
    }
    else {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Select Media") },
                actions = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Done")
                    }
                }
            )
            CustomImagePicker(
                attachments = attachments,
                onToggleAttachment = onToggleAttachment
            )
        }
    }
     */

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomImagePicker(
    attachments: List<Attachment>,
    onToggleAttachment: (Attachment) -> Unit
) {
    val context = LocalContext.current
    val allAttachments by produceState(initialValue = emptyList<Attachment>(), context) {
        val list = mutableListOf<Attachment>()
        // Load images
        val imgProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imgProjection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val date = cursor.getLong(dateCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                list += Attachment.Image(uri = uri, date = date)
            }
        }
        // Load videos
        val vidProj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED)
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            vidProj, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val duration = cursor.getLong(durCol)
                val date = cursor.getLong(dateCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                list += Attachment.Video(uri = uri, duration = duration, date = date)
            }
        }
        value = list
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(allAttachments) { attachment ->
            val uri = when (attachment) {
                is Attachment.Image -> attachment.getUri()
                is Attachment.Video -> attachment.getUri()
                else -> null
            } ?: return@items
            Box(
                Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleAttachment(attachment) }
            ) {
                if (attachment is Attachment.Video) {
                    // Show video thumbnail
                    attachment.getThumbnail(context)?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Icon(
                        Icons.Default.PlayCircleOutline,
                        contentDescription = "Video",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (attachment in attachments) {
                    Box(
                        Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f))
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


/*@OptIn(ExperimentalFoundationApi::class)
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
}*/

