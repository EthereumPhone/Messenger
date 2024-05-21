package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumhpone.domain.model.Attachment

interface MediaRepository {
    fun getImages(): Flow<List<Attachment.Image>>
}