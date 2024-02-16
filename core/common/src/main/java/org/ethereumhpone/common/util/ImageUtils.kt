package org.ethereumhpone.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException

object ImageUtils {

    suspend fun getScaledDrawable(
        context: Context,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int = 90,
    ): ByteArray {
        return withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }.build()

            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(maxWidth, maxHeight)
                .build()

            try {
                val result = imageLoader.execute(request).drawable
                result?.let { encodeDrawableToByteArray(it, quality) } ?: byteArrayOf()
            } catch (e: Exception) {
                e.printStackTrace() // Handle error as needed
                byteArrayOf()
            }
        }
    }

    private fun encodeDrawableToByteArray(drawable: Drawable?, quality: Int): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            drawable?.toBitmap()?.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }
}