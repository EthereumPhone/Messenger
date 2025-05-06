package org.ethereumhpone.chat

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.util.GalleryMedia
import org.ethereumhpone.chat.util.MediaType
import javax.inject.Inject

internal const val VideoUriArg = "videoUris"


@HiltViewModel
class MediaViewModel @Inject constructor(
    val exoPlayer: ExoPlayer
): ViewModel()  {

    private val _mediaItems = mutableStateOf<List<GalleryMedia>>(emptyList())
    val mediaItems: State<List<GalleryMedia>> = _mediaItems

    private val _selectedMedia = mutableStateOf<GalleryMedia?>(null)
    val selectedMedia: State<GalleryMedia?> = _selectedMedia

    fun loadMediaFromDevice(contentResolver: android.content.ContentResolver) {
        viewModelScope.launch {
            _mediaItems.value = loadMediaItems(contentResolver)
        }
    }

    fun selectMedia(item: GalleryMedia) {
        _selectedMedia.value = item
    }

    fun clearSelectedMedia() {
        _selectedMedia.value = null
    }

    private suspend fun loadMediaItems(contentResolver: android.content.ContentResolver): List<GalleryMedia> {
        return withContext(Dispatchers.IO) {
            val mediaItems = mutableListOf<GalleryMedia>()

            // Query for images
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            val imageSelection = "${MediaStore.Images.Media.SIZE} > 0"

            val imageQueryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            contentResolver.query(
                imageQueryUri,
                imageProjection,
                imageSelection,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val item = GalleryMedia(id, contentUri, name, MediaType.IMAGE, dateAdded)
                    mediaItems.add(item)
                }
            }

            // Query for videos
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
            )

            val videoSelection = "${MediaStore.Video.Media.SIZE} > 0"

            val videoQueryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            contentResolver.query(
                videoQueryUri,
                videoProjection,
                videoSelection,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val item = GalleryMedia(id, contentUri, name, MediaType.VIDEO, dateAdded)
                    mediaItems.add(item)
                }
            }

            // Sort all items by date added
            mediaItems.sortByDescending { it.dateAdded }
            mediaItems
        }
    }

    init {
        exoPlayer.prepare()
    }

    fun addVideoUri(uri: Uri) {
        exoPlayer.addMediaItem(MediaItem.fromUri(uri))
    }


    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
