package com.rgbc.cloudbackup.file

import android.content.Context
import android.provider.MediaStore
import java.io.File

object FileUtils {
    fun getMediaFiles(context: Context): List<File> {
        val files = mutableListOf<File>()
        val contentResolver = context.contentResolver

        // Query images
        val imageCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA),
            null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        imageCursor?.use { cursor ->
            val pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                cursor.getString(pathIndex)?.let { path ->
                    files.add(File(path))
                }
            }
        }

        // Query videos
        val videoCursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media.DATA),
            null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        videoCursor?.use { cursor ->
            val pathIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                cursor.getString(pathIndex)?.let { path ->
                    files.add(File(path))
                }
            }
        }

        return files
    }
}