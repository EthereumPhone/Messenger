package org.ethereumhpone.common.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.bumptech.glide.Glide

object ImageUtils {

    fun getScaledGif(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 90): ByteArray {
        val gif = Glide
            .with(context)
            .asGif()
            .load(uri)
            .centerInside()
            .encodeQuality(quality)
            .submit(maxWidth, maxHeight)
            .get()

        val outputStream = ByteArrayOutputStream()
        GifEncoder(context, Glide.get(context).bitmapPool).encodeTransformedToStream(gif, outputStream)
        return outputStream.toByteArray()
    }

    suspend fun getScaledImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 90): ByteArray {
        return withContext(Dispatchers.IO) {
            Glide
                .with(context)
                .`as`(ByteArray::class.java)
                .load(uri)
                .centerInside()
                .encodeQuality(quality)
                .submit(maxWidth, maxHeight)
                .get()
        }
    }

}