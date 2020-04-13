package com.qr.library.views

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View.OnClickListener
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import java.util.*

class AudioPlayerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        // 默认16sp
        textSize = 16f
        setPadding(0, 0, 8, 0)
        super.setOnClickListener(OnClickListener {
            for (audioPlayerTextView in audioPlayerTextViews) {
                if (audioPlayerTextView === this@AudioPlayerTextView) {
                    continue
                }
                // 所有其他AudioPlayerTextView暂停播放
                audioPlayerTextView.pause()
            }

            // 设置点击事件
            if (!hasPrepared) {
                // 没有准备好，点击无效
                return@OnClickListener
            }
            if (mediaPlayer!!.isPlaying) {
                pause()
            } else {
                play()
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        audioPlayerTextViews.add(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        audioPlayerTextViews.remove(this)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        // 啥也不做 禁止 防止覆盖当前
    }

    // 播放相关
    private var mediaPlayer: MediaPlayer? = null
    private var hasPrepared = false

    @Suppress("DEPRECATION")
    fun setDataSource(url: String?) {
        if (mediaPlayer == null) {
            // 设置MediaPlayer
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.setOnErrorListener { _, _, _ ->
                    mediaPlayer!!.reset()
                    stopAnimate()
                    stopUpdateDuration()
                    hasPrepared = false
                    false
                }
                mediaPlayer!!.setOnPreparedListener {
                    hasPrepared = true
                    initDuration()
                    initAnimate()
                }
                mediaPlayer!!.setOnCompletionListener {
                    stopUpdateDuration()
                    stopAnimate()
                    initAnimate()
                    initDuration()
                }
            } catch (e: Exception) {
                Log.d(
                    TAG,
                    "Init MediaPlayer Failure: " + e.message
                )
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
        if (mediaPlayer == null) {
            Log.d(TAG, "MediaPlayer == null")
            return
        }
        try {
            hasPrepared = false
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(url)
            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(
                TAG,
                "MediaPlayer SetDataSource Error: " + e.message
            )
        }
    }

    fun play() {
        if (mediaPlayer == null || mediaPlayer!!.isPlaying) {
            return
        }
        try {
            mediaPlayer!!.start()
            preUpdateTime = System.currentTimeMillis()
            startAnimate()
            startUpdateDuration()
        } catch (e: Exception) {
            Log.d(TAG, "Play Failure : " + e.message)
        }
    }

    fun pause() {
        if (mediaPlayer == null || !mediaPlayer!!.isPlaying) {
            return
        }
        try {
            mediaPlayer!!.pause()
            stopUpdateDuration()
            stopAnimate()
        } catch (e: Exception) {
            Log.d(TAG, "Pause Failure : " + e.message)
        }
    }

    fun release() {
        if (mediaPlayer == null) {
            return
        }
        try {
            mediaPlayer!!.stop()
        } catch (e: Exception) {
            Log.d(TAG, "Stop Failure : " + e.message)
        } finally {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    // 倒计时相关
    private var leftDuration: Long = 0
    private var preUpdateTime: Long = 0
    private val timeHandler = Handler()
    private val timeRunnable = Runnable { startUpdateDuration() }

    private fun initDuration() {
        leftDuration = mediaPlayer!!.duration.toLong()
        if (leftDuration <= 0) {
            text = context.getString(R.string.audio_00_00)
            return
        }
        val sec = leftDuration / 1000
        val m = sec / 60
        val s = sec % 60
        text = String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    private fun startUpdateDuration() {
        val currentTimeMillis = System.currentTimeMillis()
        val deltaTime = currentTimeMillis - preUpdateTime
        preUpdateTime = currentTimeMillis
        leftDuration -= deltaTime
        if (leftDuration <= 0) {
            text = context.getString(R.string.audio_00_00)
            return
        }
        val sec = leftDuration / 1000
        val m = sec / 60
        val s = sec % 60
        text = String.format(Locale.getDefault(), "%02d:%02d", m, s)
        timeHandler.postDelayed(timeRunnable, 1000)
    }

    private fun stopUpdateDuration() {
        timeHandler.removeCallbacks(timeRunnable)
    }

    // 声音动画相关
    private val drawLefts = intArrayOf(
        R.drawable.audio_volume_play_level_1,
        R.drawable.audio_volume_play_level_2,
        R.drawable.audio_volume_play_level_3
    )
    private val animateHandle = Handler()
    private val animateRunnable = Runnable { startAnimate() }
    private var count = 0
    private fun initAnimate() {
        count = 0
        setDrawableLeft(drawLefts[1])
    }

    private fun startAnimate() {
        setDrawableLeft(drawLefts[count % drawLefts.size])
        ++count
        animateHandle.postDelayed(animateRunnable, 300)
    }

    private fun stopAnimate() {
        animateHandle.removeCallbacks(animateRunnable)
    }

    private fun setDrawableLeft(@DrawableRes resId: Int) {
        val drawable = ContextCompat.getDrawable(context, resId)
        drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        setCompoundDrawables(drawable, null, null, null)
    }

    companion object {
        private val TAG = AudioPlayerTextView::class.java.simpleName
        private val audioPlayerTextViews: MutableList<AudioPlayerTextView> = ArrayList()

        fun pauseAll() {
            for (audioPlayerTextView in audioPlayerTextViews) {
                audioPlayerTextView.pause()
            }
        }

        fun releaseAll() {
            for (audioPlayerTextView in audioPlayerTextViews) {
                audioPlayerTextView.release()
            }
        }
    }
}