package com.vishistv.apod

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.ActivityInfo
import android.graphics.PointF
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.vishistv.apod.datafiles.Apod
import com.vishistv.apod.extension.getViewModel
import com.vishistv.apod.extension.toast
import com.vishistv.apod.util.AppUtil
import com.vishistv.apod.util.Constants
import com.vishistv.apod.util.Log
import com.vishistv.apod.util.YoutubeUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception


class MainActivity :
    AppCompatActivity(),
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var mViewModel: MainViewModel
    private lateinit var mYouTubePlayerFragment: YouTubePlayerSupportFragment

    private var mIsTypeVideo: Boolean = false
    private var mIsFullScreen: Boolean = false
    private var mYoutubePlayer: YouTubePlayer? = null

    private var mVideoId: String? = null

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

        llPlayZoom.visibility = View.GONE
        llCalendar.visibility = View.GONE

        /// Marked as error because no androidx support
        mYouTubePlayerFragment = YouTubePlayerSupportFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.youtubePlayer, mYouTubePlayerFragment).commit()

        llPlayZoom.setOnClickListener(this)
        llCalendar.setOnClickListener(this)
        ivBackGround.setOnClickListener(this)
        ivTransparentMask.setOnClickListener(this)

        setObservers()
    }

    override fun onClick(v: View?) {
        when (v) {
            llPlayZoom -> {
                if (mIsTypeVideo) {
                    switchFullScreen()
                    mYoutubePlayer?.setFullscreen(true)
                } else {
                    if (!mIsFullScreen) {
                        switchFullScreen()
                    }
                }
            }
            llCalendar -> AppUtil.showDatePickerDialog(this, this)
            ivTransparentMask,
            ivBackGround -> {
                if (mIsFullScreen) {
                    switchFullScreen()
                }
            }
        }
    }

    private fun setObservers() {
        mViewModel.getApod.observe(this, Observer { apod ->
            llPlayZoom.visibility = View.VISIBLE
            llCalendar.visibility = View.VISIBLE
            apod?.let {
                when (apod.mediaType) {
                    Constants.MEDIA_TYPE_IMAGE -> {
                        mIsTypeVideo = false
                        showApodImageData(apod)
                    }
                    Constants.MEDIA_TYPE_VIDEO -> {
                        mIsTypeVideo = true
                        showApodVideoData(apod)
                    }
                    else -> {
                        toast(resources.getString(R.string.unsupported_format_error))
                        mIsTypeVideo = false
                    }
                }
            } ?: run {
                toast(resources.getString(R.string.generic_error))
                AppUtil.hideProgressBar()
            }
        })
    }

    private fun showApodImageData(apod: Apod) {
        try {
            tvTitle.text = apod.title
            tvDescription.text = apod.description
            youtubePlayer.visibility = View.GONE

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
            updatePlayButton()
        } catch (e: Exception) {
            toast(resources.getString(R.string.generic_error))
        }
    }

    private fun showApodVideoData(apod: Apod) {
        try {
            tvTitle.text = apod.title
            tvDescription.text = apod.description

            youtubePlayer.visibility = View.VISIBLE

            mVideoId = YoutubeUtil.getYoutubeVideoIdFromUrl(apod.url)

            mVideoId?.let {
                val url = YoutubeUtil.getThumbnail(it)

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
                            id: String?, imageInfo: ImageInfo?, animatable: Animatable?
                        ) {
                            AppUtil.hideProgressBar()
                        }
                    })
                    .build()
                ivBackGround.controller = controller
                initializeYoutubePlayer()
            }
            updatePlayButton()
        } catch (e: Exception) {
            toast(resources.getString(R.string.generic_error))
        }
    }

    private fun initializeYoutubePlayer() {
        mYoutubePlayer?.let {
            playVideo()
        } ?: run {
            // Marked as error because no androidx support
            mYouTubePlayerFragment.initialize(
                "AIzaSyDWiW7qTwWwX4Ur8-9GvI2CgoD25t1QsyY",
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?,
                                                         youTubePlayer: YouTubePlayer,
                                                         wasRestored: Boolean) {
                        if (!wasRestored) {
                            mYoutubePlayer = youTubePlayer
                            playVideo()
                        }

                        youTubePlayer.setPlayerStateChangeListener(object :
                            YouTubePlayer.PlayerStateChangeListener {
                            override fun onAdStarted() {}

                            override fun onLoading() {}

                            override fun onVideoStarted() {}

                            override fun onLoaded(p0: String?) {
                                mYoutubePlayer?.pause()
                            }

                            override fun onVideoEnded() {}

                            override fun onError(p0: YouTubePlayer.ErrorReason?) {}
                        })
                    }

                    override fun onInitializationFailure(provider: YouTubePlayer.Provider?,
                                                         error: YouTubeInitializationResult?) {
                        toast(resources.getString(R.string.generic_error))
                    }
                })
        }
    }

    private fun switchFullScreen() {
        mIsFullScreen = if (mIsFullScreen) {
            ivTransparentMask.visibility = View.GONE
            AppUtil.slideUp(llBottom, false)
            AppUtil.slideUp(llTop, true)
            false
        } else {
            ivTransparentMask.visibility = View.VISIBLE
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

    private fun playVideo() {
        mYoutubePlayer?.let {
            it.loadVideo(mVideoId)
            it.setShowFullscreenButton(false)
            it.play()
        }
    }

    override fun onBackPressed() {
        if (mIsFullScreen && mIsTypeVideo) {
            mYoutubePlayer?.setFullscreen(false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Handler()
                .postDelayed({
                    switchFullScreen()
                }, 500)
        } else if (mIsFullScreen && !mIsTypeVideo) {
            switchFullScreen()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        try {
            AppUtil.showProgressBar(this)
            llPlayZoom.visibility = View.GONE
            llCalendar.visibility = View.GONE
            val m = month + 1
            val date = StringBuilder()
            date.append(year).append("-")
            if (month < 10) date.append("0")
            date.append(m).append("-")
            if (dayOfMonth < 10) date.append("0")
            date.append(dayOfMonth)

            mViewModel.fetchApod(date.toString())
            Log.d(TAG, "🤡 $date")
        } catch (e: Exception) {
            toast(resources.getString(R.string.generic_error))
        }
    }
}