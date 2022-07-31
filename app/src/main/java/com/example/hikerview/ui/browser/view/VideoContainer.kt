package com.example.hikerview.ui.browser.view

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import chuangyuan.ycj.videolibrary.R
import timber.log.Timber
import kotlin.math.abs

/**
 * 作者：By 15968
 * 日期：On 2022/3/2
 * 时间：At 11:52
 */
open class VideoContainer(
    var activity: Activity,
    var webView: IVideoWebView
) : FrameLayout(activity) {

    companion object {
        var tempFastPlay = false
    }

    /***音量的最大值 */
    private var mMaxVolume = 0

    /*** 亮度值  */
    private var brightness = -1f

    private var hasSetBrightness = false

    /**** 当前音量   */
    private var volume = -1

    /*** 音量管理  */
    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /*** 手势操作管理  */
    private val gestureDetector: GestureDetector =
        GestureDetector(context, PlayerGestureListener(this))

    /***控制音频 */
    private var exoAudioLayout: android.view.View? = null

    /***亮度布局 */
    private lateinit var exoBrightnessLayout: android.view.View

    private lateinit var exoTempFastLayout: android.view.View

    /***水印,封面图占位,显示音频和亮度布图 */
    private lateinit var videoAudioImg: ImageView

    /***水印,封面图占位,显示音频和亮度布图 */
    private lateinit var videoBrightnessImg: ImageView

    /***显示音频和亮度 */
    private lateinit var videoAudioPro: ProgressBar

    /***显示音频和亮度 */
    private lateinit var videoBrightnessPro: ProgressBar
    private lateinit var tempFastTextView: TextView

    var gestureDispatch = false

    init {
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        brightness = getScreenBrightness(context) / 255.0f
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 处理手势结束
        if (ev?.action == MotionEvent.ACTION_UP) {
            if(gestureDispatch){
                endGesture()
            }
        }
        if(gestureDispatch) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(evt: MotionEvent?): Boolean {
        return true
    }

    fun addVideoView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)
        if (exoAudioLayout == null) {
            val audioId: Int = R.layout.simple_video_audio_brightness_dialog
            val brightnessId: Int = R.layout.simple_video_audio_brightness_dialog
            val exoTempFastId: Int = R.layout.simple_exo_video_temp_fast_dialog
            intiGestureView(audioId, brightnessId, exoTempFastId)
            setBackgroundColor(activity.resources.getColor(android.R.color.black))
        }
        webView.evaluateJS("(function(){\n" +
                "    let arr = document.querySelectorAll('video');\n" +
                "    if(arr && arr.length > 0) return 'ok';\n" +
                "    else {\n" +
                "        let iframe = document.querySelectorAll('iframe');\n" +
                "        if (iframe && iframe.length > 0) {\n" +
                "            for (let i = 0; i < iframe.length; i++) {\n" +
                "                try {\n" +
                "                    arr = iframe[i].contentWindow.document.querySelectorAll('video')\n" +
                "                    if(arr && arr.length > 0) return 'ok';\n" +
                "                } catch(e){}\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    return 'no'\n" +
                "})()"
        ) {
            val result = it.substring(1, it.length - 1)
            Timber.d("find video: $result")
            gestureDispatch = "ok" == result
        }
    }

    fun destroy(){
        if(hasSetBrightness && !activity.isFinishing){
            val lpa: WindowManager.LayoutParams = activity.window.attributes
            lpa.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.window.attributes = lpa
        }
    }

    /***
     * 初始化手势布局view
     * @param audioId 音频布局id
     * @param brightnessId 亮度布局id
     */
    private fun intiGestureView(
        audioId: Int,
        brightnessId: Int,
        exoTempFastId: Int
    ) {
        exoAudioLayout = inflate(context, audioId, null)
        exoBrightnessLayout = inflate(context, brightnessId, null)
        exoTempFastLayout = inflate(context, exoTempFastId, null)
        exoAudioLayout!!.visibility = GONE
        exoBrightnessLayout.visibility = GONE
        exoTempFastLayout.visibility = GONE
        addView(exoAudioLayout, childCount)
        addView(exoBrightnessLayout, childCount)
        addView(exoTempFastLayout, childCount)
        if (audioId == R.layout.simple_video_audio_brightness_dialog) {
            videoAudioImg =
                exoAudioLayout!!.findViewById(R.id.exo_video_audio_brightness_img)
            videoAudioPro =
                exoAudioLayout!!.findViewById(R.id.exo_video_audio_brightness_pro)
        }
        if (brightnessId == R.layout.simple_video_audio_brightness_dialog) {
            videoBrightnessImg =
                exoBrightnessLayout.findViewById(R.id.exo_video_audio_brightness_img)
            videoBrightnessPro =
                exoBrightnessLayout.findViewById(R.id.exo_video_audio_brightness_pro)
        }
        tempFastTextView = exoTempFastLayout.findViewById(R.id.exo_video_dialog_pro_temp_fast_text)
    }

    /**
     * 1.获取系统默认屏幕亮度值 屏幕亮度值范围（0-255）
     */
    private fun getScreenBrightness(context: Context): Int {
        val contentResolver = context.contentResolver
        val defVal = 125
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, defVal
        )
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent percent 滑动
     */
    fun showVolumeDialog(percent: Float) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (volume < 0) {
                volume = 0
            }
        }
        var index: Int = (percent * mMaxVolume).toInt() + volume
        if (index > mMaxVolume) {
            index = mMaxVolume
        } else if (index < 0) {
            index = 0
        }
        // 变更进度条 // int i = (int) (index * 1.5 / mMaxVolume * 100);
        //  String s = i + "%";  // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
        setVolumePosition(mMaxVolume, index)
    }

    /**
     * 滑动改变亮度
     *
     * @param percent 值大小
     */
    @Synchronized
    private fun showBrightnessDialog(percent: Float) {
        if (brightness < 0) {
            brightness = activity.window.attributes.screenBrightness
            if (brightness <= 0.00f) {
                brightness = 0.50f
            } else if (brightness < 0.01f) {
                brightness = 0.01f
            }
        }
        val lpa: WindowManager.LayoutParams = activity.window.attributes
        lpa.screenBrightness = brightness + percent
        if (lpa.screenBrightness > 1.0) {
            lpa.screenBrightness = 1.0f
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f
        }
        activity.window.attributes = lpa
        setBrightnessPosition(100, (lpa.screenBrightness * 100).toInt())
        hasSetBrightness = true
    }

    /**
     * 长按临时快进
     *
     */
    @Synchronized
    fun showTempFastDialog() {
        tempFastPlay = true
        exoTempFastLayout.visibility = VISIBLE
        webView.useFastPlay(true)
    }

    /**
     *
     */
    private fun setVolumePosition(mMaxVolume: Int, currIndex: Int) {
        if (exoAudioLayout == null) {
            return
        }
        if (exoAudioLayout!!.visibility != VISIBLE) {
            videoAudioPro.max = mMaxVolume
        }
        exoAudioLayout!!.visibility = VISIBLE
        videoAudioPro.progress = currIndex
        videoAudioImg.setImageResource(if (currIndex == 0) R.drawable.ic_volume_off_white_48px else R.drawable.ic_volume_up_white_48px)
    }

    /**
     *
     */
    private fun setBrightnessPosition(mMaxVolume: Int, currIndex: Int) {
        if (exoBrightnessLayout.visibility != VISIBLE) {
            videoBrightnessPro.max = mMaxVolume
            videoBrightnessImg.setImageResource(R.drawable.ic_brightness_6_white_48px)
        }
        exoBrightnessLayout.visibility = VISIBLE
        videoBrightnessPro.progress = currIndex
    }

    /**
     * 手势结束
     */
    @Synchronized
    private fun endGesture() {
        volume = -1
        brightness = -1f
        if (tempFastPlay) {
            webView.useFastPlay(false)
            tempFastPlay = false
        }
        showGesture(GONE)
    }

    /***
     * 显示隐藏手势布局
     *
     * @param visibility 状态
     */
    protected fun showGesture(visibility: Int) {
        if (exoAudioLayout != null) {
            exoAudioLayout!!.visibility = visibility
        }
        exoBrightnessLayout.visibility = visibility
        exoTempFastLayout.visibility = visibility
    }

    /****
     * 手势监听类
     */
    private class PlayerGestureListener constructor(var videoContainer: VideoContainer) :
        GestureDetector.SimpleOnGestureListener() {
        private var firstTouch = false
        private var volumeControl = false
        private var toSeek = false
        private var isNowVerticalFullScreen = false
        private val displayMetrics: DisplayMetrics = videoContainer.resources.displayMetrics
        private val screeHeightPixels = displayMetrics.heightPixels
        private val screeWidthPixels = displayMetrics.widthPixels
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if(videoContainer.gestureDispatch) {
                videoContainer.showTempFastDialog()
            }
            super.onLongPress(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return super.onDoubleTap(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            firstTouch = true
            return super.onDown(e)
        }

        /**
         * 滑动
         */
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e2.pointerCount > 1 || tempFastPlay || !videoContainer.gestureDispatch) {
                return false
            }
            val mOldX = e1.x
            val mOldY = e1.y
            val deltaY = mOldY - e2.y
            var deltaX = mOldX - e2.x
            if (firstTouch) {
                toSeek = abs(distanceX) >= abs(distanceY)
                if (isNowVerticalFullScreen) {
                    volumeControl = mOldX > screeWidthPixels * 0.5f
                    if (mOldY < screeHeightPixels * 0.1f) {
                        return false
                    }
                } else {
                    volumeControl = mOldX > screeHeightPixels * 0.5f
                    if (mOldY < screeWidthPixels * 0.1f) {
                        return false
                    }
                }
                firstTouch = false
            }
            if (toSeek) {
//                    deltaX = -deltaX
//                    val position: Long = player.getCurrentPosition()
//                    val duration: Long = player.getDuration()
//                    var newPosition: Long =
//                        (position + deltaX * duration / screeHeightPixels / 3) as Int.toLong()
//                    if (newPosition > duration) {
//                        newPosition = duration
//                    } else if (newPosition <= 0) {
//                        newPosition = 0
//                    }
//                    showProgressDialog(
//                        Util.getStringForTime(formatBuilder, formatter, position),
//                        newPosition,
//                        duration,
//                        Util.getStringForTime(formatBuilder, formatter, newPosition),
//                        Util.getStringForTime(formatBuilder, formatter, duration)
//                    )
            } else {
                val percent: Float = deltaY / videoContainer.measuredHeight
                if (volumeControl) {
                    videoContainer.showVolumeDialog(percent)
                } else {
                    videoContainer.showBrightnessDialog(percent)
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }
}