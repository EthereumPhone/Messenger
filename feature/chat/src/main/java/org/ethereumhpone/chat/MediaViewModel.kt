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
    val exoPlayer: ExoPlayer
): ViewModel()  {

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
