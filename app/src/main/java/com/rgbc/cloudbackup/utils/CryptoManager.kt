package com.rgbc.cloudbackup.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class CryptoManager {
    private val key = "YourSuperSecretKey1234567890".toByteArray()
    private val iv = "YourSuperSecretIV".toByteArray().copyOf(16)

    private val secretKeySpec = SecretKeySpec(key, "AES")
    private val ivParameterSpec = IvParameterSpec(iv)

    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    val encryptedBlock = cipher.update(buffer, 0, read)
                    fos.write(encryptedBlock)
                }
                val finalBlock = cipher.doFinal()
                fos.write(finalBlock)
            }
        }

    }

    fun encryptFileName(fileName: String): String{
        return Base64.encodeToString(fileName.toByteArray(),Base64.NO_WRAP)
    }
    fun decryptFileName(encryptedName: String): String{
        return String(Base64.decode(encryptedName, Base64.NO_WRAP))

    }
}