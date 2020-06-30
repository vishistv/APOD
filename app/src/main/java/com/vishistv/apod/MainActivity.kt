package com.vishistv.apod

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.PointF
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.vishistv.apod.datafiles.Apod
import com.vishistv.apod.extension.getViewModel
import com.vishistv.apod.util.AppUtil
import com.vishistv.apod.util.Constants
import com.vishistv.apod.util.Log
import com.vishistv.apod.util.YoutubeUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity :
    AppCompatActivity(),
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var mViewModel: MainViewModel

    private var mIsTypeVideo: Boolean = false
    private var mIsFullScreen: Boolean = false

    companion object {
        const val TAG = "MainActivity"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Fresco.initialize(this)
        setContentView(R.layout.activity_main)

        AppUtil.showProgressBar(this)

        tvDescription.movementMethod = ScrollingMovementMethod.getInstance()

        mViewModel = getViewModel()
        mViewModel.fetchApod(null)

        llPlayZoom.setOnClickListener(this)
        rootContainer.setOnClickListener(this)
        llCalendar.setOnClickListener(this)

        setObservers()
    }

    override fun onClick(v: View?) {
        when (v) {
            llPlayZoom -> {
                if (mIsTypeVideo) {

                } else {
                    if (!mIsFullScreen) {
                        switchFullScreen()
                    }
                }
            }
            llCalendar -> {
                AppUtil.showDatePickerDialog(this, this)
            }
            rootContainer -> {
                if (mIsFullScreen) {
                    switchFullScreen()
                }
            }
        }
    }

    private fun setObservers() {
        mViewModel.getApod.observe(this, Observer { apod ->
            when (apod.mediaType) {
                Constants.MEDIA_TYPE_IMAGE -> {
                    mIsTypeVideo = false
                    updatePlayButton()
                    showApodImageData(apod)
                }
                Constants.MEDIA_TYPE_VIDEO -> {
                    mIsTypeVideo = true
                    showApodVideoData(apod)
                }
                else -> {
                    // role back to default
                    mIsTypeVideo = false
                }
            }
        })
    }

    private fun showApodImageData(apod: Apod) {
        tvTitle.text = apod.title
        tvDescription.text = apod.description

        val focusPoint = PointF(0f, 0.5f)

        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(apod.hdUrl))
            .setProgressiveRenderingEnabled(true)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setLowResImageRequest(ImageRequest.fromUri(apod.url))
            .setImageRequest(request)
            .setControllerListener(object : ControllerListener<ImageInfo?> {
                override fun onRelease(id: String?) {}
                override fun onSubmit(id: String?, callerContext: Any?) {}
                override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                    AppUtil.hideProgressBar()
                }

                override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
                    AppUtil.hideProgressBar()
                }

                override fun onFailure(id: String?, throwable: Throwable?) {
                    AppUtil.hideProgressBar()
                }

                override fun onFinalImageSet(
                    id: String?,
                    imageInfo: ImageInfo?,
                    animatable: Animatable?
                ) {
                    AppUtil.hideProgressBar()
                }
            })
            .build()

        ivBackGround
            .hierarchy
            .setActualImageFocusPoint(focusPoint)
        ivBackGround
            .controller = controller
    }

    private fun showApodVideoData(apod: Apod) {
        tvTitle.text = apod.title
        tvDescription.text = apod.description

        val url = YoutubeUtil.getThumbnail(apod.url)
        val controller = Fresco
            .newDraweeControllerBuilder()
            .setImageRequest(ImageRequest.fromUri((Uri.parse(url))))
            .setControllerListener(object : ControllerListener<ImageInfo?> {
                override fun onRelease(id: String?) {}
                override fun onSubmit(id: String?, callerContext: Any?) {}
                override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                    AppUtil.hideProgressBar()
                }

                override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
                    AppUtil.hideProgressBar()
                }

                override fun onFailure(id: String?, throwable: Throwable?) {
                    AppUtil.hideProgressBar()
                }

                override fun onFinalImageSet(
                    id: String?,
                    imageInfo: ImageInfo?,
                    animatable: Animatable?
                ) {
                    AppUtil.hideProgressBar()
                }
            })
            .build()
        ivBackGround.controller = controller
    }

    private fun switchFullScreen() {
        mIsFullScreen = if (mIsFullScreen) {
            AppUtil.slideUp(llBottom, false)
            AppUtil.slideUp(llTop, true)
            false
        } else {
            AppUtil.slideDown(llTop, true)
            AppUtil.slideDown(llBottom, false)
            true
        }
    }

    private fun updatePlayButton() {
        if (mIsTypeVideo) {
            ivPlayZoom.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        } else {
            ivPlayZoom.setImageResource(R.drawable.ic_baseline_fullscreen_24)
        }
    }

    override fun onBackPressed() {
        if (mIsFullScreen) {
            switchFullScreen()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        AppUtil.showProgressBar(this)

        val date = StringBuilder()
        date.append(year).append("-")
        if (month < 10) date.append("0")
        date.append(month).append("-")
        if (dayOfMonth < 10) date.append("0")
        date.append(dayOfMonth)

        mViewModel.fetchApod(date.toString())
        Log.d(TAG, "🤡 $date")
    }
}