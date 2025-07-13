package com.rgbc.cloudbackup.utils

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object ChecksumUtils{
    fun createChecksum(file: File): String{
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var read: Int
            inputStream.use { stream ->
                while (stream.read(buffer).also { read =  it }>0){
                    digest.update(buffer,0,read)

                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception){
            Log.w("ChecksumUtils.createChecksum",e)
            ""
        }
    }
}