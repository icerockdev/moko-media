/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

object BitmapUtils {

    // TODO: unused
    @Throws(IOException::class)
    fun getAngle(filename: String): Int {
        val exif = ExifInterface(filename)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    @Throws(IOException::class)
    fun getBitmapOrientation(bitmapStream: InputStream): Int {
        val exif = ExifInterface(bitmapStream)
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    }

    @Throws(IOException::class)
    fun getNormalizedBitmap(
        bitmapStream: InputStream,
        orientation: Int,
        sampleSize: Int? = null
    ): Bitmap {
        val bitmapOptions = if (sampleSize != null) {
            BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
            }
        } else null

        val bitmap = BitmapFactory.decodeStream(bitmapStream, null, bitmapOptions)
            ?: throw IOException("Can't decode bitmap stream")

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return result
    }

    // TODO: unused
    fun cloneRotated(bitmap: Bitmap, angle: Int): Bitmap {
        if (angle != 0) {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }

    fun getResizedBitmap(bitmap: Bitmap, maxSize: Int = DEFAULT_MAX_SIZE): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    fun getBitmapOptionsFromStream(
        inputStream: InputStream
    ): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, this)
        }
    }

    // TODO: unused
    fun getBitmapForStream(
        inputStream: InputStream,
        sampleSize: Int
    ): Bitmap? {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }

        return BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize = 1

        if (height > maxHeight || width > maxWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private const val DEFAULT_MAX_SIZE = 500
}
