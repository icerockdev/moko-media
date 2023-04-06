/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import dev.icerock.moko.media.BitmapUtils.getBitmapOrientation
import dev.icerock.moko.media.BitmapUtils.getNormalizedBitmap
import java.io.IOException

object MediaFactory {

    fun create(context: Context, uriString: String, type: MediaType): Media {
        val contentResolver = context.contentResolver
        val uri: Uri = Uri.parse(uriString)
        return when (type) {
            MediaType.PHOTO -> createPhotoMedia(contentResolver, uri)
            MediaType.VIDEO -> createVideoMedia(contentResolver, uri)
        }
    }

    @Suppress("ThrowsCount")
    fun create(context: Context, uri: Uri): Media {
        val projection = arrayOf(
            MediaStore.MediaColumns.MIME_TYPE
        )

        val contentResolver = context.contentResolver
        val cursorRef = contentResolver
            .query(uri, projection, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        return cursorRef.use { cursor ->
            if (!cursor.moveToFirst()) {
                throw IllegalStateException("cursor should have one element")
            }

            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val mimeType = cursor.getString(mimeTypeIndex)

            when {
                mimeType.startsWith("image") -> createPhotoMedia(contentResolver, uri)
                mimeType.startsWith("video") -> createVideoMedia(contentResolver, uri)
                else -> throw IllegalArgumentException("unsupported type $mimeType")
            }
        }
    }

    @SuppressLint("Range")
    private fun createPhotoMedia(
        contentResolver: ContentResolver,
        uri: Uri
    ): Media {

        val cursorRef = contentResolver
            .query(uri, null, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        return cursorRef.use { cursor ->
            if (!cursor.moveToFirst()) {
                throw IllegalStateException("not found resource")
            }

            val orientation = contentResolver.openInputStream(uri)?.use {
                getBitmapOrientation(it)
            } ?: ExifInterface.ORIENTATION_UNDEFINED

            val title = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            val normalizedBitmap = contentResolver.openInputStream(uri)?.use {
                getNormalizedBitmap(it, orientation, sampleSize = null)
            } ?: throw IOException("can't open stream")

            Media(
                name = title.orEmpty(),
                path = uri.path ?: uri.toString(),
                type = MediaType.PHOTO,
                preview = Bitmap(normalizedBitmap)
            )
        }
    }

    @SuppressLint("Range")
    private fun createVideoMedia(
        contentResolver: ContentResolver,
        uri: Uri
    ): Media {
        val cursorRef = contentResolver
            .query(uri, null, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        return cursorRef.use { cursor ->
            if (!cursor.moveToFirst()) {
                throw IllegalStateException("cursor should have one element")
            }

            val title = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            val idColumn = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)
            val thumbnail: android.graphics.Bitmap = if (idColumn != -1) {
                val id = cursor.getLong(idColumn)
                MediaStore.Video.Thumbnails.getThumbnail(
                    contentResolver,
                    id,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
                )
            } else {
                null
            } ?: contentResolver.openFileDescriptor(uri, "r")?.use {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(it.fileDescriptor)
                retriever.getFrameAtTime(0)
            } ?: throw IOException("can't read thumbnail")

            Media(
                name = title.orEmpty(),
                path = uri.path ?: uri.toString(),
                type = MediaType.VIDEO,
                preview = Bitmap(thumbnail)
            )
        }
    }
}
