package org.ethereumhpone.data.repository

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ethereumhpone.common.extensions.map
import org.ethereumhpone.domain.mapper.ImageCursor
import org.ethereumhpone.domain.model.Attachment
import org.ethereumhpone.domain.repository.MediaRepository
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val imageCursor: ImageCursor
): MediaRepository {
    override fun getImages(): Flow<List<Uri>> = flow {

        val images = imageCursor.getImageCursor()
            ?.map(imageCursor::map)
            .orEmpty()

        emit(images)
    }
}