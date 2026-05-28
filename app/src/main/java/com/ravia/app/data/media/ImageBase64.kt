package com.ravia.app.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val MAX_DIMENSION = 960
private const val TARGET_BYTES = 480_000

suspend fun imageUriToJpegDataUri(context: Context, uriString: String?): String? = withContext(Dispatchers.IO) {
    if (uriString.isNullOrBlank()) return@withContext null
    if (uriString.startsWith("data:image/") || uriString.startsWith("http://") || uriString.startsWith("https://")) {
        return@withContext uriString
    }

    val bitmap = context.contentResolver.openInputStream(Uri.parse(uriString)).use { input ->
        BitmapFactory.decodeStream(input)
    } ?: return@withContext null

    val scaled = bitmap.scaleToMaxDimension(MAX_DIMENSION)
    if (scaled !== bitmap) bitmap.recycle()

    var quality = 82
    var bytes = ByteArray(0)
    do {
        ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
            bytes = output.toByteArray()
        }
        quality -= 8
    } while (bytes.size > TARGET_BYTES && quality >= 50)

    if (!scaled.isRecycled) scaled.recycle()
    "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun Bitmap.scaleToMaxDimension(maxDimension: Int): Bitmap {
    val largest = maxOf(width, height)
    if (largest <= maxDimension) return this

    val ratio = maxDimension.toFloat() / largest.toFloat()
    val newWidth = (width * ratio).toInt().coerceAtLeast(1)
    val newHeight = (height * ratio).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}
