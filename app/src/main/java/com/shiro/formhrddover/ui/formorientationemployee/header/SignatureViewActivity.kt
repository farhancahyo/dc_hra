package com.shiro.formhrddover.ui.formorientationemployee.header

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivitySignatureViewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class SignatureViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignatureViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClear.setOnClickListener {
            binding.signatureView.clearCanvas()
        }

        binding.btnSave.setOnClickListener {
            val bitmap = binding.signatureView.signatureBitmap
//            var path = Integer.parseInt(saveImage(bitmap))
            saveImage(bitmap)
        }
    }

    private fun saveImage(bitmap: Bitmap?) {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val uuid = UUID.randomUUID().toString()
        val timeStamp: String = java.lang.String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
        val string = "Img$timeStamp$uuid"
        if (storageDirectory != null) {
            storageDirectory.mkdirs()
            if(!storageDirectory.exists()){
                storageDirectory.mkdirs()
            }
        }
        val file = File.createTempFile(string, ".jpg", storageDirectory)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, out)
        } catch (ex: Exception) {
            Log.d(
                    "UPIL",
                    java.lang.String.format(
                            "Error writing bitmap to %s: %s",
                            file,
                            ex.message
                    )
            )
        } finally {
            try {
                out?.close()
            } catch (ex: IOException) {
            }
        }

        val data = Intent()
        data.putExtra("pathsignature", file.absolutePath.toString())
        setResult(RESULT_OK, data)
        finish()
    }
}