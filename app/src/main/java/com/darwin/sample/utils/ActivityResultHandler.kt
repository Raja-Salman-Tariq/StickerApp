package com.darwin.sample.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.darwin.sample.Util
import com.darwin.sample.ViolaSampleActivity
import com.github.gabrielbb.cutout.CutOut
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_viola_sample_edited.*

object ActivityResultHandler {

    fun handleResult(activity : ViolaSampleActivity, requestCode : Int, resultCode : Int, data : Intent?, iv_input_image : ImageView) {

        activity.run {

            // set image state to newly selected image
            if (requestCode == imagePickerIntentId && resultCode == Activity.RESULT_OK) {
                pickedImage = data?.data!!
                Log.d("ImgPathForCrop", "handleResult: $pickedImage;  ${pickedImage.path}")
                val imagePath = Util.getPath(this, pickedImage)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                bitmap = BitmapFactory.decodeFile(imagePath, options)
                bitmap = Util.modifyOrientation(bitmap, imagePath!!)
                iv_input_image.setImageURI(pickedImage)
                iv_input_image.setImageBitmap(bitmap)
            }

            // set image state to processed image
            if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE.toInt()) {
                when (resultCode) {
                    // successful processing
                    Activity.RESULT_OK -> {// set new image state
                        bitmap = ImageHandlingHelper.getBitmapFromUri(activity, CutOut.getUri(data))
                        pickedImage = ImageHandlingHelper.cachePersistingBitmapAndGetUri(activity, bitmap)
                        iv_input_image.setImageBitmap(bitmap)
                        iv_input_image.setImageURI(pickedImage)
//                        showGenericSnackbar("Successfully created bmp and updated", root_view)
                    }
                    // unsuccessful processing
                    Activity.RESULT_CANCELED -> {
//                        showGenericSnackbar("Result CANCELLED ?", root_view)
                    }
                    // error in processing
                    CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE.toInt() -> {
                        CutOut.getError(data).run {
                            Log.d("CutOutBtnClick", " $message ")
//                            showGenericSnackbar(message.toString(), root_view)
                        }
                    }
                    else -> {/* no-op */}
                }
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
                (data?.extras?.get("data") as Bitmap).run {
                    bitmap = this
                    iv_input_image.setImageBitmap(this)
                    pickedImage = ImageHandlingHelper.cachePersistingBitmapAndGetUri(
                        activity,
                        this
                    )
                    iv_input_image.setImageURI(pickedImage)
                }
            }

            // unknown result code
            else {/*showGenericSnackbar(
                "Result codes not match;\n" +
                    " recvd=$requestCode;\n" +
                    "cutout=${CutOut.CUTOUT_ACTIVITY_REQUEST_CODE}",
                root_view
            )*/}
        }
    }

    public fun showGenericSnackbar(msg: String, root_view : View) {
        Snackbar.make(
            root_view,
            "$msg",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("CLOSE") {/*Define your callback here*/ }
            show()
        }
    }
}