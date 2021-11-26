package com.darwin.sample

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.darwin.sample.stickerscode.model.Sticker
import com.darwin.sample.stickerscode.model.StickerPack
import com.darwin.sample.utils.PermissionHelper.PermissionsListener
import com.darwin.sample.utils.ActivityResultHandler
import com.darwin.sample.utils.ImageHandlingHelper
import com.darwin.sample.utils.PermissionHelper
import com.darwin.sample.utils.StickerAndPackHandler
import com.darwin.viola.still.FaceDetectionListener
import com.darwin.viola.still.Viola
import com.darwin.viola.still.model.CropAlgorithm
import com.darwin.viola.still.model.FaceDetectionError
import com.darwin.viola.still.model.FaceOptions
import com.darwin.viola.still.model.Result
import com.github.gabrielbb.cutout.CutOut
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_face_crop_sample.btCrop
import kotlinx.android.synthetic.main.activity_face_crop_sample.iv_input_image
import kotlinx.android.synthetic.main.activity_face_crop_sample.tvErrorMessage
import kotlinx.android.synthetic.main.activity_viola_sample_edited.*
import java.util.ArrayList


/**
 * The class VoilaSampleActivity
 *
 * @author Darwin Francis
 * @version 1.0
 * @since 15 Jul 2020
 */
class ViolaSampleActivity : AppCompatActivity() {

    internal lateinit var pickedImage: Uri
    private lateinit var viola: Viola
    internal lateinit var bitmap: Bitmap
    private lateinit var permissionHelper: PermissionHelper

    internal val imagePickerIntentId = 1

    var stickerPack : StickerPack? = null

//    =============================================================================================
//    -----------------------             ACTIVITY OVERRIDES              -------------------------
//    =============================================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viola_sample_edited)

        permissionHelper = PermissionHelper(this)

        stickerPack = intent.getParcelableExtra("stickerpack")

        Log.d("individSticker", "onCreate: recvd individ sticker = $stickerPack")

        requestStoragePermission()
        setEventListeners()
        prepareFaceCropper()
    }
    //----------------------------------------------------------------------------------------------
    override fun onResume() {
        super.onResume()
        permissionHelper.resume()
    }
    //----------------------------------------------------------------------------------------------
    override fun onDestroy() {
        super.onDestroy()
        permissionHelper.onDestroy()
    }
    //----------------------------------------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //----------------------------------------------------------------------------------------------

    // get image to work on and clear any previous views
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ActivityResultHandler.handleResult(
            this,
            requestCode,
            resultCode,
            data,
            iv_input_image
        )
    }

//    =============================================================================================
//    -----------------------             FUNCTIONALITIES              -------------------------
//    =============================================================================================
    private fun setEventListeners() {
        btImage.setOnClickListener {
            pickImageFromGallery()
        }
        btCam.setOnClickListener {
            dispatchTakePictureIntent()
        }
        btCrop.setOnClickListener {
            crop()
        }
        btCutout.setOnClickListener {
            CutOut.activity().src(pickedImage).start(this);
        }
        btSave.setOnClickListener{
            val image = if (stickerPack != null) {
                StickerAndPackHandler().saveSingleSticker(this, bitmap, stickerPack!!)
            } else StickerAndPackHandler().saveBitmap(this, bitmap)

            val imageUri: Uri = FileProvider.getUriForFile(
                this,
                "com.darwin.sample.fileprovider",  //(use your app signature + ".provider" )
                image
            )

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM,imageUri )
                type = "image/webp"
            }
            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
            val intent = Intent().apply {
                data = imageUri
                if (stickerPack!=null) {
                    putStringArrayListExtra("emojis",
                        stickerPack!!.stickers[0].emojis as ArrayList<String>?
                    )
                    putExtra("name", image.name)
                    putExtra("size", image.length())
                    putExtra("uri", Uri.fromFile(image).toString())
                    putExtra("path", image.absolutePath)
                    Log.d("individSticker", "fetchStickers: size=" + image.length())
                }
            }
            val readData = StickerAndPackHandler().fetchStickerPack(image.parentFile.parentFile)
            Log.d("StickerAdder", "add failed: in get:     -3/ while fetching")
            val mPack = readData.first
            val emoji = readData.second!!.first
            //                stickers = Integer.parseInt(readData.second.second);
            Log.d("StickerAdder", "add failed: in get:     -3/ yes")
            val stickers: MutableList<Sticker?> = StickerAndPackHandler().fetchStickers(0, emoji, image.parentFile)

            mPack?.stickers = stickers
            mPack?.custom = true
            intent.putExtra("pack", mPack)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
    //----------------------------------------------------------------------------------------------
    private fun requestStoragePermission() {
        permissionHelper.setListener(permissionsListener)
        val requiredPermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionHelper.requestPermission(requiredPermissions, 100)
    }
    //----------------------------------------------------------------------------------------------
    private fun pickImageFromGallery() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, imagePickerIntentId)
    }
    //----------------------------------------------------------------------------------------------
    private fun prepareFaceCropper() {
        viola = Viola(listener)

        val options = BitmapFactory.Options()
        options.inScaled = false
        bitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.po_single, options
        )
        pickedImage = ImageHandlingHelper.getDefaultAssetImageUri(resources)
        iv_input_image.setImageURI(pickedImage)
    }
    //----------------------------------------------------------------------------------------------
    private fun crop() {
        val faceOption =
            FaceOptions.Builder()
                .cropAlgorithm(/*cropAlgorithm*/CropAlgorithm.SQUARE)
                .setMinimumFaceSize(6)
//                .enableAgeClassification()
                .enableDebug()
                .build()
        viola.detectFace(ImageHandlingHelper.getBitmapFromUri(this, pickedImage), faceOption)
    }

//    =============================================================================================
//    -----------------------                 CALL BACKS                  -------------------------
//    =============================================================================================

    // call back for face detect op
    private val listener: FaceDetectionListener = object : FaceDetectionListener {

        // when successful, show faces
        override fun onFaceDetected(result: Result) {

            bitmap = result.facePortraits[0].face
            pickedImage = ImageHandlingHelper.cachePersistingBitmapAndGetUri(this@ViolaSampleActivity, bitmap)
            iv_input_image.setImageBitmap(bitmap)
            iv_input_image.setImageURI(pickedImage)
            tvErrorMessage.visibility = View.GONE
        }

        // when unsucessful op, show njothing except error; eg image size unsupported, etc.
        override fun onFaceDetectionFailed(error: FaceDetectionError, message: String) {
            tvErrorMessage.text = message
//            tvErrorMessage.visibility = View.VISIBLE
            ActivityResultHandler.showGenericSnackbar(message, root_view);
        }
    }

    //----------------------------------------------------------------------------------------------

    // callback for getting permissions
    private val permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onPermissionGranted(request_code: Int) {
            tvErrorMessage.visibility = View.GONE
//            pickImageFromGallery()
        }

        override fun onPermissionRejectedManyTimes(
            rejectedPerms: List<String>,
            request_code: Int,
            neverAsk: Boolean
        ) {
            tvErrorMessage.text = "Permission for storage access denied."
            tvErrorMessage.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    // get image from camera
    val REQUEST_IMAGE_CAPTURE = 54321

    private fun dispatchTakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(root_view, "Please grant permission for the camera first and try again.", LENGTH_INDEFINITE).apply {
                setAction("OK"){
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA ),
                        52431
                    )
                }
                show()
            }
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }
    }
}