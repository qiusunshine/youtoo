package com.example.hikerview.ui.music

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import com.example.hikerview.ui.video.MusicForegroundService
import com.example.hikerview.ui.video.event.MusicAction
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * 作者：By 15968
 * 日期：On 2022/1/19
 * 时间：At 23:32
 */
class HeadsetButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            handleIntent(intent)
        }
    }

    companion object {

        private var lastClickTime: Long = 0
        const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_REWIND
                or PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_FAST_FORWARD
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SEEK_TO
                or PlaybackStateCompat.ACTION_SET_RATING
                or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
                or PlaybackStateCompat.ACTION_PLAY_FROM_URI
                or PlaybackStateCompat.ACTION_PREPARE
                or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH
                or PlaybackStateCompat.ACTION_PREPARE_FROM_URI
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                or PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED)

        fun handleIntent(intent: Intent): Boolean {
            val keyEvent: KeyEvent? = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            val gap = System.currentTimeMillis() - lastClickTime
            Timber.i("HeadsetButtonReceiver：" + "onReceive: gap: " + gap + ", if:" + keyEvent?.keyCode + ", action: " + keyEvent?.action)
            if (gap < 500) {
                //不处理双击的情况
                return false
            }
            lastClickTime = System.currentTimeMillis()
            val isPlayOrPause = (keyEvent?.keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                    || keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                    || keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            when {
                BluetoothDevice.ACTION_ACL_DISCONNECTED == intent.action -> {
                    Log.i(
                        "headSet",
                        "HeadsetButtonReceiver：" + "onReceive:" + "if: ACTION_ACL_DISCONNECTED"
                    )
                    EventBus.getDefault().post(MusicAction(MusicForegroundService.PAUSE_NOW))
                    return true
                }
                isPlayOrPause -> {
                    Log.i(
                        "headSet",
                        "HeadsetButtonReceiver：" + "onReceive:" + "if: HEADSETHOOK"
                    )
                    EventBus.getDefault().post(MusicAction(MusicForegroundService.PAUSE))
                    return true
                }
                keyEvent?.keyCode === KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    Log.i(
                        "headSet",
                        "HeadsetButtonReceiver：" + "onReceive:" + "if: KEYCODE_HEADSETHOOK"
                    )
                    EventBus.getDefault().post(MusicAction(MusicForegroundService.NEXT))
                    return true
                }
                keyEvent?.keyCode === KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    Log.i(
                        "headSet",
                        "HeadsetButtonReceiver：" + "onReceive:" + "if: KEYCODE_MEDIA_PREVIOUS"
                    )
                    EventBus.getDefault().post(MusicAction(MusicForegroundService.PREV))
                    return true
                }
            }
            return false
        }

        fun registerHeadsetReceiver(context: Context) {
            val audioManager: AudioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val name = ComponentName(context.packageName, HeadsetButtonReceiver::class.java.name)
            audioManager.registerMediaButtonEventReceiver(name)
        }

        fun unregisterHeadsetReceiver(context: Context) {
            val audioManager: AudioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val name = ComponentName(context.packageName, HeadsetButtonReceiver::class.java.name)
            audioManager.unregisterMediaButtonEventReceiver(name)
        }
    }
}