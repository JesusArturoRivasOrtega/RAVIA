package com.ravia.app.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

suspend fun FirebaseStorage.uploadFile(
    localUri: String?,
    remotePath: String,
    fallbackUrl: String? = localUri
): String? {
    if (localUri.isNullOrBlank()) return null
    if (localUri.startsWith("http://") || localUri.startsWith("https://")) return localUri

    return runCatching {
        val fileName = "${UUID.randomUUID()}_${Uri.parse(localUri).lastPathSegment ?: "archivo"}"
        val ref = reference.child("$remotePath/$fileName")
        ref.putFile(Uri.parse(localUri)).await()
        ref.downloadUrl.await().toString()
    }.getOrElse { fallbackUrl }
}

