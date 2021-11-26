package com.darwin.sample.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import androidx.appcompat.app.AppCompatActivity
import com.darwin.sample.stickerscode.model.Sticker
import com.darwin.sample.stickerscode.model.StickerPack
import com.darwin.sample.utils.ImageHandlingHelper.format
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_viola_sample_edited.*
import java.io.*
import java.lang.IllegalStateException
import java.util.ArrayList

class StickerAndPackHandler {

    var latestPack : StickerPack? = null

    fun createAndSaveBitmapFromUri(ctxt: Context, img: Uri, activity: AppCompatActivity?) {
        try {
            bmp = ImageHandlingHelper.getBitmapFromUri(ctxt, img)
            val tag = "abc"
            saveBitmap(ctxt, bmp!!)
        } catch (e: Exception) {
            Log.d("StickerAdder", "add failed: " + e.message)
        }
    }

    fun saveSingleSticker(ctxt: Context, bitmap: Bitmap, pack: StickerPack): File {
        val bmp = getResizedBitmap(bitmap, 512, 512)
        val dir = ctxt.getExternalFilesDir("custom_stickers" + File.separator + pack.identifier)

        val n = File(dir,"stickers").listFiles()!!.size +1

        val file = File(
            ctxt.getExternalFilesDir("custom_stickers" + File.separator + pack.identifier + File.separator + "stickers"),
            "f$n.webp"
        )
        val outputStream = FileOutputStream(file)

        bmp.compress(format, 100, outputStream)

        outputStream.flush()
        outputStream.close()


        MediaStore.Images.Media.insertImage(ctxt.contentResolver, file.absolutePath, file.name, file.name)
        Log.d("StickerAdder", "check " + dir!!.absolutePath)

        Snackbar.make(
            (ctxt as Activity).root_view,
            "File saved !",
            Snackbar.LENGTH_INDEFINITE
        ).apply{
            setAction("CLOSE"){/*Define your callback here*/}
            show()
        }

        latestPack = fetchStickerPack(dir).first

        return file
    }

    fun saveBitmap(ctxt: Context, bitmap: Bitmap) : File{
        var bmp = getResizedBitmap(bitmap, 512, 512)
//        bmp?.setHasAlpha(false);
        val n = ctxt.getExternalFilesDir("custom_stickers")!!.listFiles().size
        val dir = ctxt.getExternalFilesDir("custom_stickers" + File.separator + "stickerPack" + n)

        val pack = FileOutputStream(File(dir, "pack_meta.txt"))

        val tray = File(dir, "tray.webp")
        val o0 = FileOutputStream(tray)

        val f1 = File(
            ctxt.getExternalFilesDir("custom_stickers" + File.separator + "stickerPack" + n + File.separator + "stickers"),
            "f1.webp"
        )
        val o1 = FileOutputStream(f1)

        bmp!!.compress(format, 100, o1)
        getResizedBitmap(bmp, 96, 96)!!.compress(format, 100, o0)

        pack.write(
            ("stickerPack$n" +
                    "\nstickerPack$n" +
                    "\nRST" +
                    "\ntray.webp" +
                    "\n1" +
                    "\nfalse" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n❤")
                .toByteArray()
        )

        pack.flush()
        pack.close()

        o0.flush()
        o0.close()

        o1.flush()
        o1.close()


        MediaStore.Images.Media.insertImage(ctxt.contentResolver,tray.absolutePath,tray.name,tray.name)
        MediaStore.Images.Media.insertImage(ctxt.contentResolver, f1.absolutePath, f1.name, f1.name)
        Log.d("StickerAdder", "check " + dir!!.absolutePath)

        Snackbar.make(
            (ctxt as Activity).root_view,
            "File saved !",
            Snackbar.LENGTH_INDEFINITE
        ).apply{
            setAction("CLOSE"){/*Define your callback here*/}
            show()
        }

            latestPack = fetchStickerPack(dir).first

        return f1
    }


    var bmp: Bitmap? = null


//    fun add(ctxt: Context, img: Uri?, activity: AppCompatActivity?) {
//        try {
////            Bitmap bmp;
//            if (Build.VERSION.SDK_INT < 28) {
//                bmp = MediaStore.Images.Media.getBitmap(
//                    ctxt.contentResolver,
//                    img
//                )
//            } else {
//                bmp = ImageDecoder.decodeBitmap(
//                    ImageDecoder.createSource(
//                        ctxt.contentResolver,
//                        img!!
//                    )
//                )
//            }
//            addBitmap(ctxt, bmp!!)
//        } catch (e: java.lang.Exception) {
//            Log.d("StickerAdder", "add failed: " + e.message)
//        }
//    }

//    @Throws(IOException::class)
//    fun addBitmap(ctxt: Context, bmp: Bitmap /*, String packname*/) {
//        var bmp = bmp
//        bmp = getResizedBitmap(bmp, 512, 512)
//        val n = ctxt.getExternalFilesDir("custom_stickers")!!.listFiles().size
//        val dir =
//            ctxt.getExternalFilesDir("custom_stickers" + File.separator + "stickerPack" + n) //packname);
//        val pack = FileOutputStream(File(dir, "pack_meta.txt"))
//        val tray = File(dir, "tray.webp")
//        val f1 = File(
//            ctxt.getExternalFilesDir("custom_stickers" + File.separator + "stickerPack" + n + File.separator + "stickers"),
//            "f1.webp"
//        )
//        val o0 = FileOutputStream(tray)
//        val o1 = FileOutputStream(f1)
//        val format: CompressFormat
//        format =
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) CompressFormat.WEBP else CompressFormat.WEBP_LOSSLESS
//        bmp.compress(format, 100, o1)
//        getResizedBitmap(bmp, 96, 96).compress(format, 100, o0)
//        pack.write(
//            """stickerPack$n
//    stickerPack$n
//    RSTxyz
//    tray.webp
//    1
//    false
//
//
//
//
//
//    ❤""".toByteArray()
//        )
//        pack.flush()
//        pack.close()
//        o0.flush()
//        o0.close()
//        o1.flush()
//        o1.close()
//        MediaStore.Images.Media.insertImage(
//            ctxt.contentResolver,
//            tray.absolutePath,
//            tray.name,
//            tray.name
//        )
//        MediaStore.Images.Media.insertImage(ctxt.contentResolver, f1.absolutePath, f1.name, f1.name)
//        Log.d("StickerAdder", "check " + dir!!.absolutePath)
//    }

    fun get(ctxt: Context): ArrayList<StickerPack?>? {
        val stickerPacks: ArrayList<StickerPack?> = ArrayList()
        try {
            Log.d("StickerAdder", "add failed: in get:     -1-")
            val customPacks = ctxt.getExternalFilesDir("custom_stickers")!!
                .listFiles()
            var readData: Pair<StickerPack?, Pair<String?, String?>?>
            var mPack: StickerPack?
            var emoji: String?
            Log.d("StickerAdder", "add failed: in get:     -2-")
            for (folder in customPacks) {
                Log.d("StickerAdder", "add failed: in get:     -3/ $folder")
                readData = fetchStickerPack(folder)
                Log.d("StickerAdder", "add failed: in get:     -3/ while fetching")
                mPack = readData.first
                emoji = readData.second!!.first
                //                stickers = Integer.parseInt(readData.second.second);
                Log.d("StickerAdder", "add failed: in get:     -3/ yes")
                val stickers: MutableList<Sticker?> = fetchStickers(0, emoji, File(folder, "stickers"))

                latestPack = mPack

                mPack?.stickers = stickers
                mPack?.custom = true
                Log.d("StickerAdder", "add failed: in get:     -3/ almost")

                if (stickers.size<3)
                    continue;
                stickerPacks.add(mPack)

                Log.d("StickerAdder", "add failed: in get:     -3/ done")
            }
        } catch (e: java.lang.Exception) {
            Log.d("StickerAdder", "add failed: in get:" + e.message)
            return null
        }
        return stickerPacks
    }

    internal fun fetchStickers(stickers: Int, emoji: String?, folder: File): MutableList<Sticker?> {
//        File f;
        val files = folder.listFiles()
        val tmp: ArrayList<Sticker?> = ArrayList()
        var s: Sticker
        for (f in files) {
//            f = new File(folder, "f"+i+".webp");
            s = Sticker(f.name, object : ArrayList<String?>() {
                init {
                    add(emoji)
                    add(emoji)
                    add(emoji)
                }
            })
            s.setSize(f.length())
            s.uri = Uri.fromFile(f)
            s.path = f.absolutePath
            Log.d("StickerAdder", "fetchStickers: size=" + f.length())
            tmp.add(s)
        }
        return tmp
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun fetchStickerPack(stickerPack: File): Pair<StickerPack?, Pair<String?, String?>?> {
        Log.d("StickerAdder", "add failed: in get:     -3/ started fetch")
        val f = File(stickerPack.toString() + File.separator + "pack_meta.txt")
        val input = BufferedReader(InputStreamReader(FileInputStream(f)))
        Log.d("StickerAdder", "is open : ${input.ready()}")
        val identifier: String
        val name: String
        val publisher: String
        val tray_img: String
        val version: String
        val email: String
        val website: String
        val privacy: String
        val licence: String
        val stickers: String
        val emoji: String
        var avoid_cache = false
        var ln: String
        Log.d("StickerAdder", "read the following")
        if (input.readLine().also { ln = it } != null) {
            identifier = ln
            Log.d("StickerAdder", "id = $identifier")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            name = ln
            Log.d("StickerAdder", "name = $name")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            publisher = ln
            Log.d("StickerAdder", "pub = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            tray_img = ln
            Log.d("StickerAdder", "tray = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            version = ln
            Log.d("StickerAdder", "vers = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            if (ln == "true") avoid_cache = true
            Log.d("StickerAdder", "cach = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            email = ln
            Log.d("StickerAdder", "email = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            website = ln
            Log.d("StickerAdder", "site = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            privacy = ln
            Log.d("StickerAdder", "priva = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            licence = ln
            Log.d("StickerAdder", "lis = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            stickers = ln
            Log.d("StickerAdder", "stickers = $ln")
        } else throw IllegalStateException("File not readable")
        if (input.readLine().also { ln = it } != null) {
            emoji = ln
            Log.d("StickerAdder", "emojies = $ln")
        } else throw IllegalStateException("File not readable")
        input.close()
        Log.d("StickerAdder", "add failed: in get:     -3/ dealt w all")
        return Pair<StickerPack?, Pair<String?,String?>?>(
            StickerPack(
                identifier,
                name,
                publisher,
                tray_img,
                email,
                website,
                privacy,
                licence,
                version,
                avoid_cache,
                false
            ),
            Pair<String?, String?>(emoji, stickers)
        )
    }


    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }
}