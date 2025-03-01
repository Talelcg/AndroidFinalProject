package com.idz.colman24class2.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import com.project.easytravel.base.MyApplication
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CloudinaryModel {

    companion object {
        private var isInitialized = false

        fun initCloudinary(context: Context) {
            if (!isInitialized) {
                val config = mapOf(
                    "cloud_name" to com.project.easytravel.BuildConfig.CLOUD_NAME,
                    "api_key" to com.project.easytravel.BuildConfig.API_KEY,
                    "api_secret" to com.project.easytravel.BuildConfig.API_SECRET
                )
                MediaManager.init(context, config)
                MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                    .maxConcurrentRequests(3)
                    .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                    .build()
                isInitialized = true
            }
        }
    }

    fun uploadImage(imageUri: Uri, callback: (String?, String?) -> Unit) {
        MediaManager.get().upload(imageUri)
            .option("public_id", UUID.randomUUID().toString())
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    callback(resultData?.get("secure_url") as? String, null)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    callback(null, error?.description)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    fun uploadBitmap(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val context = MyApplication.Globals.context ?: return
        val file = bitmapToFile(bitmap, context)

        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val publicUrl = resultData["secure_url"] as? String ?: ""
                    onSuccess(publicUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }
}