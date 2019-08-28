/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

object MediaFactory {
    fun create(
        context: Context,
        uriString: String,
        type: MediaType
    ): Media {
        val contentResolver = context.contentResolver
        val uri: Uri = Uri.parse(uriString)
        return when (type) {
            MediaType.PHOTO -> createPhotoMedia(
                contentResolver,
                uri
            )
            MediaType.VIDEO -> createVideoMedia(
                contentResolver,
                uri
            )
        }
    }

    fun create(context: Context, uri: Uri): Media {
        val projection = arrayOf(
            MediaStore.MediaColumns.MIME_TYPE
        )

        val contentResolver = context.contentResolver
        val cursor = contentResolver
            .query(uri, projection, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        try {
            with(cursor) {
                if (!moveToFirst()) {
                    throw IllegalStateException()
                }

                val mimeType = getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                    .let { getString(it) }

                return when {
                    mimeType.startsWith("image") -> createPhotoMedia(
                        contentResolver,
                        uri
                    )
                    mimeType.startsWith("video") -> createVideoMedia(
                        contentResolver,
                        uri
                    )
                    else -> throw IllegalArgumentException("unsupported type $mimeType")
                }
            }
        } finally {
            cursor.close()
        }
    }

    private fun createPhotoMedia(
        contentResolver: ContentResolver,
        uri: Uri
    ): Media {
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.TITLE
        )

        val cursor = contentResolver
            .query(uri, projection, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        try {
            return with(cursor) {
                if (!moveToFirst()) {
                    throw IllegalStateException("not found resource")
                }

                val orientation = contentResolver.openInputStream(uri)?.use {
                    val exif = ExifInterface(it)
                    exif.rotationDegrees
                } ?: 0

                val title =
                    getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE)
                        .let { getString(it) } ?: uri.lastPathSegment

                val sourceBitmap = contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                } ?: throw IOException("can't open stream")

                val matrix = Matrix()
                matrix.postRotate(orientation.toFloat())

                val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                    sourceBitmap,
                    0, 0, sourceBitmap.width, sourceBitmap.height,
                    matrix,
                    true
                )

                Media(
                    name = title,
                    path = uri.toString(),
                    type = MediaType.PHOTO,
                    preview = Bitmap(rotatedBitmap)
                )
            }
        } finally {
            cursor.close()
        }
    }

    private fun createVideoMedia(
        contentResolver: ContentResolver,
        uri: Uri
    ): Media {
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.TITLE
        )

        val cursor = contentResolver
            .query(uri, projection, null, null, null)
            ?: throw IllegalArgumentException("can't open cursor")

        try {
            return with(cursor) {
                if (!moveToFirst()) {
                    throw IllegalStateException()
                }

                val titleColumn = getColumnIndex(MediaStore.Video.VideoColumns.TITLE)
                val title = if (titleColumn != -1) {
                    getString(titleColumn)
                } else {
                    null
                } ?: uri.lastPathSegment ?: "file"

                val idColumn = getColumnIndex(MediaStore.Video.VideoColumns._ID)
                val thumbnail: android.graphics.Bitmap? = if (idColumn != -1) {
                    val id = getLong(idColumn)
                    MediaStore.Video.Thumbnails.getThumbnail(
                        contentResolver,
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                } else {
                    null
                } ?: contentResolver.openFileDescriptor(uri, "r")?.use {
                    val retriver = MediaMetadataRetriever()
                    retriver.setDataSource(it.fileDescriptor)
                    retriver.getFrameAtTime(0)
                }

                if (thumbnail == null) {
                    throw IOException("can't read thumbnail")
                }

                Media(
                    name = title,
                    path = uri.toString(),
                    type = MediaType.VIDEO,
                    preview = Bitmap(thumbnail)
                )
            }
        } finally {
            cursor.close()
        }
    }
}
