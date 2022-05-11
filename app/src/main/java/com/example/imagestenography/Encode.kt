package com.example.imagestenography

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.example.imagestenography.databinding.ActivityEncodeBinding
import java.io.*


class Encode : AppCompatActivity(), TextEncodingCallback {

    //encoding
    private var filepath: Uri? = null
    private lateinit var imageSteganography: ImageSteganography
    private lateinit var textEncoding: TextEncoding
    //Bitmaps
    private var originalImage: Bitmap? = null
    private var encodedImage: Bitmap? = null

    private lateinit var binding: ActivityEncodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater)
        checkAndRequestPermissions()
        binding.btnChooseImage.setOnClickListener{
            imageChooser()
        }

        //Encode Button
        binding.btnEncode.setOnClickListener{
            if (filepath == null ){
                Toast.makeText(applicationContext, "Choose file!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (binding.editTextMessage.text.matches(Regex(""))) {
                Toast.makeText(applicationContext, "Type message!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (binding.editTextSecretKey.text.matches(Regex(""))){
                Toast.makeText(applicationContext, "Type secret key!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (filepath != null){
                if(binding.editTextMessage.text != null){
                    //ImageSteganography Object instantiation
                    imageSteganography = ImageSteganography(binding.editTextMessage.text.toString(),
                        binding.editTextSecretKey.text.toString(),
                        originalImage
                    )
                    //TextEncoding object Instantiation
                    textEncoding = TextEncoding(this, this)
                    //Executing the encoding
                    textEncoding.execute(imageSteganography);
                }
            }
        }
        binding.btnSaveImage.setOnClickListener{
            if (filepath == null ){
                Toast.makeText(applicationContext, "Choose file!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (binding.editTextMessage.text.matches(Regex(""))) {
                Toast.makeText(applicationContext, "Type message!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (binding.editTextSecretKey.text.matches(Regex(""))){
                Toast.makeText(applicationContext, "Type secret key!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            if (filepath == null ){
                Toast.makeText(applicationContext, "Choose file!", Toast.LENGTH_LONG).show()
                return@setOnClickListener;
            }
            val imgToSave: Bitmap? = encodedImage
            binding.progressbar.visibility = View.VISIBLE
            val performEncoding = Thread { saveToInternalStorage(imgToSave!!) }
            performEncoding.start()
            finish()
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
                        binding.imageViewEncode.setImageBitmap(bitmap)
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageViewEncode.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun saveToInternalStorage(bitmapImage: Bitmap) {

        val fOut: OutputStream
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), "Encoded" + ".PNG"
        ) // the File to save ,
        try {
            fOut = FileOutputStream(file)
            bitmapImage.compress(
                Bitmap.CompressFormat.PNG,
                100,
                fOut
            ) // saving the Bitmap to a file
            fOut.flush() // Not really required
            fOut.close() // do not forget to close the stream
            binding.progressbar.visibility = View.INVISIBLE
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun checkAndRequestPermissions() {
        val permissionWriteStorage = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 1)
        }
    }
    override fun onStartTextEncoding() {
        //Whatever you want to do at the start of text encoding
    }
    @SuppressLint("SetTextI18n")
    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        //By the end of textEncoding

        if (result != null && result.isEncoded) {
            encodedImage = result.encoded_image;
            binding.btnEncode.isEnabled = false
            binding.btnChooseImage.isEnabled = false
            binding.editTextMessage.isEnabled = false
            binding.editTextSecretKey.isEnabled = false
            binding.btnSaveImage.visibility = View.VISIBLE
            Toast.makeText(applicationContext,"image encoded!", Toast.LENGTH_LONG).show()
        }
    }
}