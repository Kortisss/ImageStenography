package com.example.imagestenography

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.example.imagestenography.databinding.ActivityDecodeBinding
import com.google.android.material.snackbar.Snackbar


class Decode : AppCompatActivity(), TextDecodingCallback {
    //encoding
    private var filepath: Uri? = null
    private lateinit var imageSteganography: ImageSteganography
    private lateinit var textEncoding: TextDecoding
    //Bitmaps
    private var originalImage: Bitmap? = null

    private lateinit var binding: ActivityDecodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecodeBinding.inflate(layoutInflater)

        binding.btnSelectImage.setOnClickListener{
            imageChooser()
        }
        binding.btnDecodeImage.setOnClickListener{
            if (filepath != null) {
                //Making the ImageSteganography object
                val imageSteganography = ImageSteganography(
                    binding.editTextSecretKeyDecode.text.toString(),
                    originalImage
                )
                //Making the TextDecoding object
                val textDecoding = TextDecoding(this, this)
                //Execute Task
                textDecoding.execute(imageSteganography)
            }
        }
        setContentView(binding.root)
    }

    private fun imageChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        resultLauncher.launch(intent)
    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Intent.createChooser(intent,"Select picture")
            filepath = data?.data
            val selectedPhotoUri = data?.data
            try {
                selectedPhotoUri?.let {
                    if(Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            selectedPhotoUri
                        )
                        originalImage = bitmap
                        binding.imageView.setImageBitmap(bitmap)
                        binding.btnDecodeImage.visibility = View.VISIBLE
                        binding.editTextSecretKeyDecode.isEnabled = true
                        binding.btnDecodeImage.isEnabled = true

                        binding.textViewMessage.text = ""
                        binding.editTextSecretKeyDecode.text = Editable.Factory.getInstance().newEditable("")
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(bitmap)
                        binding.btnDecodeImage.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onStartTextEncoding() {
        //Whatever you want to do by the start of textDecoding
    }
    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        //By the end of textDecoding

        if (result != null) {
            if (!result.isDecoded)
                Toast.makeText(applicationContext,"No message found",Toast.LENGTH_SHORT).show()
            else {
                if (!result.isSecretKeyWrong) {
                    binding.textViewMessage.text = result.message
                    binding.btnDecodeImage.isEnabled = false
                    binding.editTextSecretKeyDecode.isEnabled = false
                    Toast.makeText(applicationContext,"Image decoded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext,"Wrong secret key",Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(applicationContext,"Select Image First",Toast.LENGTH_SHORT).show()
        }
    }
}