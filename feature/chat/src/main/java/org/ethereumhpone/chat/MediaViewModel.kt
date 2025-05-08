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
import org.ethereumhpone.domain.model.Attachment


internal const val VideoUriArg = "videoUris"


@HiltViewModel
class MediaViewModel @Inject constructor(
    val exoPlayer: ExoPlayer
): ViewModel()  {

    private val _items = MutableStateFlow<List<Attachment>>(emptyList())
    val mediaItems: StateFlow<List<Attachment>> = _items

    private val _selectedIndex = MutableStateFlow(-1)
    val selectedIndex: StateFlow<Int> = _selectedIndex

    val selectedAttachment: StateFlow<Attachment?> = combine(_items, _selectedIndex) { list, idx ->
        list.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _selectionMode = MutableStateFlow(false)
    val isInSelectionMode: StateFlow<Boolean> = _selectionMode

    private val _selectedSet = MutableStateFlow<Set<Attachment>>(emptySet())
    val selectedSet: StateFlow<Set<Attachment>> = _selectedSet

    fun loadMedia(resolver: ContentResolver) {
        viewModelScope.launch {
            _items.value = fetchAttachments(resolver)
        }
    }

    private suspend fun fetchAttachments(resolver: ContentResolver): List<Attachment> =
        withContext(Dispatchers.IO) {
            val output = mutableListOf<Attachment>()
            // Images
            val imgProj = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_ADDED)
            val imgUri = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            resolver.query(imgUri, imgProj, "${MediaStore.Images.Media.SIZE}>0", null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while(cursor.moveToNext()){
                    val id = cursor.getLong(idCol)
                    val mime = cursor.getString(mimeCol)
                    val date = cursor.getLong(dateCol)
                    val contentUri = ContentUris.withAppendedId(imgUri, id)
                    output += Attachment.Image(contentUri, null, date) //TODO: Add MIME Detection
                }
            }
            // Videos
            val vidProj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED)
            val vidUri = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            resolver.query(vidUri, vidProj, "${MediaStore.Video.Media.SIZE}>0", null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { cursor->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                while(cursor.moveToNext()){
                    val id = cursor.getLong(idCol)
                    val dur = cursor.getLong(durCol)
                    val date = cursor.getLong(dateCol)
                    val contentUri = ContentUris.withAppendedId(vidUri, id)
                    output += Attachment.Video(contentUri, dur, date)
                }
            }
            output
        }

    fun select(idx: Int) { _selectedIndex.value = idx }
    fun next() { _items.value.takeIf { it.isNotEmpty() }?.let { _selectedIndex.value = (_selectedIndex.value+1)%it.size } }
    fun prev() { _items.value.takeIf{it.isNotEmpty()}?.let{ _selectedIndex.value = if(_selectedIndex.value<=0) it.lastIndex else _selectedIndex.value-1 } }
    fun toggleSelectionMode() { _selectionMode.value = !_selectionMode.value; if(!_selectionMode.value) _selectedSet.value=emptySet() }
    fun toggleSelect(att: Attachment) { val s=_selectedSet.value.toMutableSet(); if(!s.remove(att)) s+=att; _selectedSet.value=s; if(s.isEmpty()) _selectionMode.value=false }
    fun selectAll() { _selectedSet.value=_items.value.toSet() }
    fun clearSelection() { _selectedSet.value=emptySet() }

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
