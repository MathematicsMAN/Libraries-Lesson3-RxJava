package ru.android.libraries_lesson3_rxjava

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MainPresenter(
    private val view: MainView,
    private val model: ConvertModel
) {
    private val TAG: String = "convertJPGtoPNG"

    fun convertJPGtoPNG(imagePicked: Bitmap, pathImagePicked: String) {
        MainActivity.converterDisposable = CompositeDisposable()

        MainActivity.converterDisposable?.add(
            convert(imagePicked, pathImagePicked)
                .delay(1, TimeUnit.SECONDS)
                .cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    model.pathImageConverted = it.first
                    view.setPNGImage(it.second, it.first)
                }, {
                    Log.e(TAG, "Can't convert", it)
                })
        )
    }

    private fun convert(bitmap: Bitmap, pathToBitmap: String): Single<Pair<String, Bitmap>> {
        val (pathImagePickedDir, nameImagePicked) = splitPathToBitmap(pathToBitmap)
        return Single.fromCallable {
            val pathImageOutput = "$pathImagePickedDir/$nameImagePicked.png"
            val imageOutputStream = FileOutputStream(pathImageOutput)

            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutputStream)) {
                return@fromCallable (pathImageOutput to BitmapFactory.decodeFile(pathImageOutput))
            } else {
                throw Exception("Conversion problem")
            }
        }
    }

    private fun splitPathToBitmap(pathToBitmap: String): Pair<String, String> {
        val pathImagePickedParts = pathToBitmap.split("/")
        val pathImagePickedDir = pathImagePickedParts
            .subList(1, pathImagePickedParts.size - 1)
            .joinToString(prefix = "/", separator = "/")
        val nameImagePicked = pathImagePickedParts[pathImagePickedParts.size - 1]
            .split(".")[0]

        return pathImagePickedDir to nameImagePicked
    }
}
