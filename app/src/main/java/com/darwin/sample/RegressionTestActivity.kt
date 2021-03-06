package com.darwin.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.darwin.viola.still.FaceDetectionListener
import com.darwin.viola.still.Viola
import com.darwin.viola.still.model.CropAlgorithm
import com.darwin.viola.still.model.FaceDetectionError
import com.darwin.viola.still.model.FaceOptions
import com.darwin.viola.still.model.Result
import kotlinx.android.synthetic.main.activity_face_crop_sample.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * The class StillImageSampleActivity
 *
 * @author Darwin Francis
 * @version 1.0
 * @since 13 Jul 2020
 */
class RegressionTestActivity : AppCompatActivity() {

    private lateinit var viola: Viola
    private lateinit var staggeredLayoutManager: StaggeredGridLayoutManager
    private val faceListAdapter = FacePhotoAdapter()
    private var bitmap: Bitmap? = null
    private var cropAlgorithm = CropAlgorithm.THREE_BY_FOUR
    private var imageList: MutableList<Bitmap> = mutableListOf()
    private var imageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_crop_sample)
        initializeUI()
        setEventListeners()
        listImagesFromFolder()
        prepareFaceCropper()
    }

    private fun initializeUI() {
        staggeredLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        staggeredLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        rvCroppedImages.layoutManager = staggeredLayoutManager
        rvCroppedImages.adapter = faceListAdapter
    }

    private fun setEventListeners() {
        btCrop.setOnClickListener {
            val radioButtonID: Int = radio_algorithm.checkedRadioButtonId
            val radioButton: View = radio_algorithm.findViewById(radioButtonID)
            val algorithmIndex: Int = radio_algorithm.indexOfChild(radioButton)
            cropAlgorithm = getAlgorithmByIndex(algorithmIndex)
            crop()
        }
    }

    private fun getAlgorithmByIndex(index: Int): CropAlgorithm = when (index) {
        0 -> CropAlgorithm.THREE_BY_FOUR
        1 -> CropAlgorithm.SQUARE
        2 -> CropAlgorithm.LEAST
        else -> CropAlgorithm.THREE_BY_FOUR
    }

    private fun listImagesFromFolder() {
        val path: String =
            Environment.getExternalStorageDirectory().toString() + "/FImages"
        val directory = File(path)
        val files: Array<File> = directory.listFiles()
        for (i in files.indices) {
            try {
                val f = File(files[i].name)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap =
                    BitmapFactory.decodeStream(FileInputStream("$path/$f"), null, options)
                val rotBitmap = Util.modifyOrientation(bitmap!!, "$path/$f")
                imageList.add(rotBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun prepareFaceCropper() {
        viola = Viola(listener)
        val options = BitmapFactory.Options()
        options.inScaled = false
        bitmap = imageList[0]
        iv_input_image.setImageBitmap(bitmap)
    }

    private fun crop() {
        val faceOption =
            FaceOptions.Builder()
                .cropAlgorithm(cropAlgorithm)
                .setMinimumFaceSize(4)
                .enableDebug()
                .build()

        if (imageIndex < imageList.size) {
            bitmap = imageList[imageIndex]

            //bitmap = rotateBitmap(bitmap!!)

            iv_input_image.setImageBitmap(bitmap)
            viola.detectFace(bitmap!!, faceOption)
            imageIndex++
        } else {
            imageIndex = 0
        }
        viola.detectFace(bitmap!!, faceOption)
    }


    private val listener: FaceDetectionListener = object : FaceDetectionListener {

        override fun onFaceDetected(result: Result) {
            tvErrorMessage.text = ""
            faceListAdapter.bindData(result.facePortraits)
        }

        override fun onFaceDetectionFailed(error: FaceDetectionError, message: String) {
            tvErrorMessage.text = message
            faceListAdapter.bindData(listOf())
        }
    }
}