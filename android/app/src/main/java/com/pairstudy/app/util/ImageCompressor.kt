package com.pairstudy.app.util
import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
object ImageCompressor {
    suspend fun compress(context: Context, uri: Uri): MultipartBody.Part = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val ratio = minOf(1f, 1600f / maxOf(info.size.width, info.size.height))
                decoder.setTargetSize(maxOf(1, (info.size.width * ratio).toInt()), maxOf(1, (info.size.height * ratio).toInt()))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "无法读取该图片，请重新选择" }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) } ?: error("无法读取图片")
        }
        try {
            val out = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            MultipartBody.Part.createFormData("file", "checkin.jpg", out.toByteArray().toRequestBody("image/jpeg".toMediaType()))
        } finally { bitmap.recycle() }
    }
}
