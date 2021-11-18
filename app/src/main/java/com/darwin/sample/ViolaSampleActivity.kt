package com.darwin.sample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.darwin.sample.PermissionHelper.PermissionsListener
import com.darwin.viola.still.FaceDetectionListener
import com.darwin.viola.still.Viola
import com.darwin.viola.still.model.CropAlgorithm
import com.darwin.viola.still.model.FaceDetectionError
import com.darwin.viola.still.model.FaceOptions
import com.darwin.viola.still.model.Result
import kotlinx.android.synthetic.main.activity_face_crop_sample.btCrop
import kotlinx.android.synthetic.main.activity_face_crop_sample.iv_input_image
import kotlinx.android.synthetic.main.activity_face_crop_sample.radio_algorithm
import kotlinx.android.synthetic.main.activity_face_crop_sample.rvCroppedImages
import kotlinx.android.synthetic.main.activity_face_crop_sample.tvErrorMessage
import kotlinx.android.synthetic.main.activity_viola_sample.*
import kotlinx.android.synthetic.main.activity_viola_sample_edited.*


/**
 * The class VoilaSampleActivity
 *
 * @author Darwin Francis
 * @version 1.0
 * @since 15 Jul 2020
 */
class ViolaSampleActivity : AppCompatActivity() {

    private lateinit var viola: Viola
//    private lateinit var staggeredLayoutManager: StaggeredGridLayoutManager
//    private val faceListAdapter = FacePhotoAdapter()
    private var bitmap: Bitmap? = null
//    private var cropAlgorithm = CropAlgorithm.THREE_BY_FOUR
    private lateinit var permissionHelper: PermissionHelper

    private val imagePickerIntentId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viola_sample_edited)
        permissionHelper = PermissionHelper(this)
//        initializeUI()
        setEventListeners()
        prepareFaceCropper()
    }

    override fun onResume() {
        super.onResume()
        permissionHelper.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHelper.onDestroy()
    }

//    private fun initializeUI() {
//        staggeredLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
//        staggeredLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
//        rvCroppedImages.layoutManager = staggeredLayoutManager
//        rvCroppedImages.adapter = faceListAdapter
//    }

    private fun setEventListeners() {
        btImage.setOnClickListener {
            requestStoragePermission()
        }
        btCrop.setOnClickListener {
//            val radioButtonID: Int = radio_algorithm.checkedRadioButtonId
//            val radioButton: View = radio_algorithm.findViewById(radioButtonID)
//            val algorithmIndex: Int = radio_algorithm.indexOfChild(radioButton)
//            cropAlgorithm = CropAlgorithm.SQUARE//getAlgorithmByIndex(algorithmIndex)
            crop()
        }
    }

    private fun requestStoragePermission() {
        permissionHelper.setListener(permissionsListener)
        val requiredPermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionHelper.requestPermission(requiredPermissions, 100)
    }

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

//    private fun getAlgorithmByIndex(index: Int): CropAlgorithm = when (index) {
//        0 -> CropAlgorithm.THREE_BY_FOUR
//        1 -> CropAlgorithm.SQUARE
//        2 -> CropAlgorithm.LEAST
//        else -> CropAlgorithm.THREE_BY_FOUR
//    }

    private fun prepareFaceCropper() {
        viola = Viola(listener)
//        viola.addAgeClassificationPlugin(this)

        val options = BitmapFactory.Options()
        options.inScaled = false
        bitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.po_single, options
        )
        iv_input_image.setImageBitmap(bitmap)
    }

    private fun crop() {
        val faceOption =
            FaceOptions.Builder()
                .cropAlgorithm(/*cropAlgorithm*/CropAlgorithm.SQUARE)
                .setMinimumFaceSize(6)
//                .enableAgeClassification()
                .enableDebug()
                .build()
        viola.detectFace(bitmap!!, faceOption)
    }

    // call back for face detect op
    private val listener: FaceDetectionListener = object : FaceDetectionListener {

        // when successful, show faces
        override fun onFaceDetected(result: Result) {
//            faceListAdapter.bindData(result.facePortraits)
            iv_input_image.setImageBitmap(result.facePortraits[0].face)
            tvErrorMessage.visibility = View.GONE
        }

        // when unsucessful op, show njothing except error; eg image size unsupported, etc.
        override fun onFaceDetectionFailed(error: FaceDetectionError, message: String) {
            tvErrorMessage.text = message
            tvErrorMessage.visibility = View.VISIBLE
//            faceListAdapter.bindData(emptyList())
        }
    }


    // get image to work on and clear any previous views
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickerIntentId && resultCode == Activity.RESULT_OK) {
            val pickedImage: Uri = data?.data!!
            val imagePath = Util.getPath(this, pickedImage)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeFile(imagePath, options)
            bitmap = Util.modifyOrientation(bitmap!!, imagePath!!)
            iv_input_image.setImageBitmap(bitmap)
//            faceListAdapter.bindData(emptyList())
        }
    }

    // callback for getting permissions
    private val permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onPermissionGranted(request_code: Int) {
            tvErrorMessage.visibility = View.GONE
            pickImageFromGallery()
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}