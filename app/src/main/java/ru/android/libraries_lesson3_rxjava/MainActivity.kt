package ru.android.libraries_lesson3_rxjava

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import ru.android.libraries_lesson3_rxjava.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainView {

    private var _vb: ActivityMainBinding? = null
    private val presenter = MainPresenter(this, ConvertModel())

    private var pathImagePicked: String? = null


    private val vb
        get() = _vb!!

    companion object {
        const val REQUEST_CODE_GET_CONTENT = 100
        const val REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 101

        var converterDisposable: CompositeDisposable? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.imageJpg.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpg"))
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                REQUEST_CODE_GET_CONTENT
            )
        }

        vb.buttonConvert.setOnClickListener {
            if (pathImagePicked == null) return@setOnClickListener

            if (checkPermissionWrite()) {
                presenter.convertJPGtoPNG(
                    (vb.imageJpg.drawable as BitmapDrawable).bitmap,
                    pathImagePicked!!
                )
            } else {
                requestPermissionWrite()
            }
        }
    }

    private fun checkPermissionWrite(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionWrite() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK &&
            requestCode == REQUEST_CODE_GET_CONTENT &&
            data != null && data.data != null
        ) {
            val imagePickedUri = data.data!!
            vb.imageJpg.setImageURI(imagePickedUri)
            pathImagePicked = getPathFromUri(imagePickedUri)
            vb.textUriJpgImage.text  = pathImagePicked
        }

    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var res: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(projection[0])
            columnIndex.let {
                res = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return res
    }

    override fun onDestroy() {
        super.onDestroy()
        _vb = null
        converterDisposable?.dispose()
    }

    override fun setPNGImage(bitmapPNG: Bitmap, pathPNG: String) {
        vb.imagePng.setImageBitmap(bitmapPNG)
        vb.textUriPngImage.text = pathPNG
    }
}