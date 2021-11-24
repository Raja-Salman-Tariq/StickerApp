package com.darwin.sample.utils

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.darwin.sample.R
import java.io.File
import java.io.FileOutputStream

object ImageHandlingHelper {

    private const val DEFAULT_ASSET_IMAGE = R.drawable.po_single
    val format = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP else Bitmap.CompressFormat.WEBP_LOSSLESS


    fun getDefaultAssetImageUri(resources: Resources): Uri = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(
            DEFAULT_ASSET_IMAGE
        ) + '/' + resources.getResourceTypeName(DEFAULT_ASSET_IMAGE) + '/' + resources.getResourceEntryName(
            DEFAULT_ASSET_IMAGE
        )
    )

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    // by persisting bitmap we mean that system will not destroy it as its not in the cach directory
    fun cachePersistingBitmapAndGetUri(activity : AppCompatActivity, bitmap : Bitmap): Uri {
//        File.createTempFile("tmp", null, activity.cacheDir);
        val cacheFile = File(activity.getExternalFilesDir(null), "tmp.webp")
        bitmap.compress(format, 100, FileOutputStream(cacheFile))
        MediaStore.Images.Media.insertImage(activity.contentResolver, cacheFile.absolutePath, cacheFile.name, cacheFile.name)
        return Uri.fromFile(cacheFile)
    }

    fun getBitmapFromUri(ctxt: Context, imageUri : Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                ctxt.contentResolver,
                imageUri
            )
        } else {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    ctxt.contentResolver,
                    imageUri
                )
            )
        }
    }
}