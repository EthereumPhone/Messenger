package org.ethereumhpone.chat

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.chat.util.GalleryMedia
import org.ethereumhpone.chat.util.MediaType
import javax.inject.Inject
import kotlin.collections.map
import kotlin.collections.toMutableSet
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*


internal const val VideoUriArg = "videoUris"


@HiltViewModel
class MediaViewModel @Inject constructor(
    val exoPlayer: ExoPlayer
): ViewModel()  {

    private val _selectedUris = mutableStateListOf<Uri>()
    val selectedUris: List<Uri> get() = _selectedUris

    // Media items as StateFlow
    private val _mediaItems = MutableStateFlow<List<GalleryMedia>>(emptyList())
    val mediaItems: StateFlow<List<GalleryMedia>> = _mediaItems

    // Currently selected media index
    private val _selectedMediaIndex = MutableStateFlow(-1)
    val selectedMediaIndex: StateFlow<Int> = _selectedMediaIndex

    // Derived currently selected media (or null)
    val selectedMedia: StateFlow<GalleryMedia?> = combine(
        _mediaItems,
        _selectedMediaIndex
    ) { items, index ->
        items.getOrNull(index)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    // Multi-selection set
    private val _selectedMediaItems = MutableStateFlow<Set<GalleryMedia>>(emptySet())
    val selectedMediaItems: StateFlow<Set<GalleryMedia>> = _selectedMediaItems

    // Selection mode flag
    private val _isInSelectionMode = MutableStateFlow(false)
    val isInSelectionMode: StateFlow<Boolean> = _isInSelectionMode

    // Load media asynchronously
    fun loadMediaFromDevice(contentResolver: ContentResolver) {
        viewModelScope.launch {
            _mediaItems.value = loadMediaItems(contentResolver)
        }
    }

    fun selectMedia(item: GalleryMedia) {
        if (_isInSelectionMode.value) {
            toggleItemSelection(item)
        } else {
            val index = _mediaItems.value.indexOf(item)
            if (index != -1) _selectedMediaIndex.value = index
        }
    }

    fun navigateToNextMedia() {
        _mediaItems.value.takeIf { it.isNotEmpty() }?.let {
            _selectedMediaIndex.value = (_selectedMediaIndex.value + 1) % it.size
        }
    }

    fun navigateToPreviousMedia() {
        _mediaItems.value.takeIf { it.isNotEmpty() }?.let { items ->
            _selectedMediaIndex.value = if (_selectedMediaIndex.value <= 0)
                items.lastIndex
            else
                _selectedMediaIndex.value - 1
        }
    }

    fun toggleSelectionMode() {
        _isInSelectionMode.value = !_isInSelectionMode.value
        if (!_isInSelectionMode.value) _selectedMediaItems.value = emptySet()
    }

    fun toggleItemSelection(item: GalleryMedia) {
        val current = _selectedMediaItems.value.toMutableSet()
        if (current.remove(item).not()) current.add(item)
        _selectedMediaItems.value = current
        if (current.isEmpty()) _isInSelectionMode.value = false
    }

    fun selectAllMedia() {
        _selectedMediaItems.value = _mediaItems.value.toSet()
    }

    fun clearSelections() {
        _selectedMediaItems.value = emptySet()
    }

    fun clearSelectedMedia() {
        _selectedMediaIndex.value = -1
    }

    fun refreshSelection() {
        _selectedUris.apply {
            clear()
            addAll(_selectedMediaItems.value.map { it.uri })
        }
    }

    // Get URIs from selected items
    fun getSelectedMediaUris(): List<Uri> =
        _selectedMediaItems.value.map { it.uri }

    private suspend fun loadMediaItems(contentResolver: ContentResolver): List<GalleryMedia> {
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
