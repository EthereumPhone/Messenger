package org.ethereumhpone.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal const val VideoUriArg = "videoUris"


@HiltViewModel
class MediaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val exoPlayer: ExoPlayer
): ViewModel()  {
    private val videoUris = savedStateHandle.getStateFlow(VideoUriArg, emptyList<Uri>())

    val videoItems = videoUris.map { uris ->
        uris.map { uri ->
            VideoItem(uri, MediaItem.fromUri(uri))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        exoPlayer.prepare()
    }

    fun addVideoUri(uri: Uri) {
        savedStateHandle[VideoUriArg] =  videoUris.value + uri
        exoPlayer.addMediaItem(MediaItem.fromUri(uri))
    }

    fun playVideo(uri: Uri) {
        exoPlayer.setMediaItem(
            videoItems.value.find { it.uri == uri }?. mediaItem ?: return
        )
        exoPlayer.play()
    }

    override fun onCleared() {
        super.onCleared()

        exoPlayer.release()
    }

}


data class VideoItem(val uri: Uri, val mediaItem: MediaItem)