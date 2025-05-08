package org.ethereumhpone.chat.screen

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.dgenBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.MediaViewModel
import org.ethereumhpone.domain.model.Attachment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    //viewModel: MediaViewModel = viewModel(),
    showImages: Boolean = true,
    loadMedia: (ContentResolver) -> Unit,
    toggleSelect: (Attachment) -> Unit,
    selectAttachment: (Int) -> Unit,
    clearSelection: () -> Unit,
    toggleSelectionMode: () -> Unit,
    selectAll: () -> Unit,
    next: () -> Unit,
    prev: () -> Unit,
    mediaItems: List<Attachment>,
    selectedIndex: Int,
    selectedSet: Set<Attachment>,
    isInSelectionMode: Boolean,

    onBack: () -> Unit
) {
    val context = LocalContext.current
    //val mediaItems by viewModel.mediaItems.collectAsState() //TODO: Replace with attachments
    //val selectedIndex by viewModel.selectedIndex.collectAsState()
    //val selectedAttachment by viewModel.selectedAttachment.collectAsState()
    //val isInSelectionMode by viewModel.isInSelectionMode.collectAsState()
    //val selectedSet by viewModel.selectedSet.collectAsState()

    // Permission state
    var hasPermission by remember { mutableStateOf(false) }
    var hasDetailBeenOpened by remember { mutableStateOf(false) }

    var coroutinescope = rememberCoroutineScope()

    val filtered = if (showImages) {
        mediaItems.filterIsInstance<Attachment.Image>()
    } else {
        mediaItems.filterIsInstance<Attachment.Video>()
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermission) loadMedia(context.contentResolver)
    }

    // Launcher for permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.all { it }
        if (hasPermission) loadMedia(context.contentResolver)
    }

    Box(modifier = Modifier.fillMaxSize().imePadding().background(dgenBlack)) {
        if (!hasPermission) {
            PermissionScreen {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }
        } else if (filtered.isEmpty()) { //TODO: Replace with attachments
            // Loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Crossfade(
                hasDetailBeenOpened
            ) { openDetail ->
                if(!openDetail){
                    MediaGridScreen(
                        mediaItems = filtered, //TODO: Replace with attachments
                        onMediaClick = { att ->
                            if (isInSelectionMode) {
                                // in “Select” mode, tapping toggles the item in the set
                                //viewModel.toggleSelect(att) //TODO: Remove
                                toggleSelect(att)
                            } else {
                                // otherwise open detail view
                                //viewModel.select(filtered.indexOf(att)) //TODO: Remove
                                selectAttachment(filtered.indexOf(att))
                                hasDetailBeenOpened = true
                            }
                            //viewModel.select(mediaItems.indexOf(att))
                        },
                        isInSelectionMode = isInSelectionMode,
                        selectedItems = selectedSet,
                        toggleSelectionMode = toggleSelectionMode,
                        selectAllMedia = selectAll,
                        clearSelections = clearSelection,
                        onBack = onBack,
                        onSelectionDone = {}
                    )
                }
                else {
                    MediaDetailScreen(
                        onBack = {
                            hasDetailBeenOpened = false
                            coroutinescope.launch {
                                delay(500)
                                selectAttachment(-1)
                            }
                        },
                        onNext = next,
                        onPrevious = prev,
                        allMedia = filtered,
                        currentIndex = selectedIndex
                    )
                }
            }
        }
    }
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
                        Modifier
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

