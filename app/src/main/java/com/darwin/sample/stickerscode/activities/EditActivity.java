package com.darwin.sample.stickerscode.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.darwin.sample.R;

import java.io.ByteArrayOutputStream;
import java.io.File;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class EditActivity extends AppCompatActivity {

    Uri imgUri;

    PhotoEditorView mPhotoEditorView;
    PhotoEditor mEditor;

    public String TAG = "editact";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.action_info).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_done).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            saveImg();
//            getImgAndFinishAct();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImg() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "saveImg: saving started");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mEditor.saveAsFile(
                    getExternalFilesDir(null) + File.separator + "tmp.bmp",
                    new PhotoEditor.OnSaveListener() {
                        @Override
                        public void onSuccess(@NonNull String imagePath) {
                            Log.d(TAG, "onSuccess: "+imagePath);
                            finish();
                        }

                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "onFailure: FAILED ! ;-;");
                        }
                    }
            );
        }
    }


    private void getImgAndFinishAct() {
        Bitmap.CompressFormat format ;
        if (Build.VERSION.SDK_INT< Build.VERSION_CODES.R)
            format = Bitmap.CompressFormat.WEBP;
        else
            format = Bitmap.CompressFormat.WEBP_LOSSLESS;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bmp =((BitmapDrawable)mPhotoEditorView.getSource().getDrawable()).getBitmap();
        bmp.compress(format, 100, stream);

        byte[] byteArray = stream.toByteArray();

        Intent i = new Intent();
        i.putExtra("bmpresult", byteArray);
        setResult(RESULT_OK, i);
        finish();
    }

    private void setView() {
        imgUri = getIntent().getData();
        Log.d(TAG, "onCreate: "+imgUri);

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        mPhotoEditorView.getSource().setImageURI(imgUri);

        mEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .build();

        mEditor.setBrushDrawingMode(true);
    }


}
