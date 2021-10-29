package com.life.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import com.life.android.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScreenShot(private val context: Context) {
     fun getBitmapUri(view: View): Uri? {
        val bmp: Bitmap = getBitmapFromView(view = view)
        var bmpUri: Uri? = null
        try {
            val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap =
                Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}