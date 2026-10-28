package com.diego_ramos.croche_api.modules.product.data.local.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProductImageStorage(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "product_photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    fun saveProductPhoto(sourceUri: Uri): String {
        val fileName = "photo_${Uuid.random()}.jpg"
        val targetFile = File(photosDir, fileName)

        val rotation = getExifRotation(sourceUri)
        val originalBitmap = decodeRawBitmapFromUri(sourceUri)
            ?: throw IllegalArgumentException("Não foi possível carregar a imagem selecionada.")

        val rotatedBitmap = rotateBitmap(originalBitmap, rotation)
        val resizedBitmap = resizeIfNeeded(rotatedBitmap, MAX_DIMENSION)
        val finalBitmap = removeTransparency(resizedBitmap)

        FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            out.flush()
        }

        if (originalBitmap != resizedBitmap && !originalBitmap.isRecycled) {
            originalBitmap.recycle()
        }
        if (rotatedBitmap != resizedBitmap && !rotatedBitmap.isRecycled) {
            rotatedBitmap.recycle()
        }
        if (finalBitmap != resizedBitmap && !finalBitmap.isRecycled) {
            finalBitmap.recycle()
        }
        if (!resizedBitmap.isRecycled) {
            resizedBitmap.recycle()
        }

        return "product_photos/$fileName"
    }

    fun saveBitmapPhoto(bitmap: Bitmap): String {
        val fileName = "photo_${Uuid.random()}.jpg"
        val targetFile = File(photosDir, fileName)

        val resizedBitmap = resizeIfNeeded(bitmap, MAX_DIMENSION)
        val finalBitmap = removeTransparency(resizedBitmap)

        FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            out.flush()
        }

        if (finalBitmap != resizedBitmap && !finalBitmap.isRecycled) {
            finalBitmap.recycle()
        }
        if (resizedBitmap != bitmap && !resizedBitmap.isRecycled) {
            resizedBitmap.recycle()
        }

        return "product_photos/$fileName"
    }

    fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val rotation = getExifRotation(uri)
                val original = BitmapFactory.decodeStream(inputStream) ?: return null
                rotateBitmap(original, rotation)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun deletePhoto(relativePath: String?): Boolean {
        if (relativePath.isNullOrBlank()) return false
        val file = getFileFromRelativePath(relativePath)
        return if (file.exists()) file.delete() else false
    }

    fun getFileFromRelativePath(relativePath: String): File {
        return if (relativePath.startsWith("/")) {
            File(relativePath)
        } else {
            File(context.filesDir, relativePath)
        }
    }

    private fun decodeRawBitmapFromUri(uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun getExifRotation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun removeTransparency(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) return bitmap
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return resultBitmap
    }

    companion object {
        const val MAX_DIMENSION = 1280
        const val COMPRESSION_QUALITY = 85
    }
}
