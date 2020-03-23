package com.qr.library.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Window
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

class AudioRecorderButton @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    // 当前状态
    private var curState = STATE_NORMAL


    init {
        setBackgroundResource(R.drawable.audio_btn_recorder_normal)
        setText(R.string.audio_str_recorder_normal)

        // 解决RecyclerView吞噬触摸事件的问题
        setOnTouchListener { v, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                v.parent.requestDisallowInterceptTouchEvent(true)
            } else if (action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // 显示Dialog
                // 检查权限
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    // 无录音权限 发生错误
                    recordStateChangedListener?.onPermissionDenied(Manifest.permission.RECORD_AUDIO)
                    return false
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    recordStateChangedListener?.onPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    return false
                }
                startRecord()
                changeState(STATE_RECORDING_NORMAL)
            }
            MotionEvent.ACTION_MOVE -> {
                if (curState == STATE_RECORDING_NORMAL) {
                    if (wantToCancel(x, y)) {
                        updateNoticeText(R.string.audio_str_recorder_recording_cancel)
                        changeState(STATE_RECORDING_CANCEL)
                    }
                } else {
                    if (!wantToCancel(x, y)) {
                        updateNoticeText(R.string.audio_str_recorder_recording_normal)
                        changeState(STATE_RECORDING_NORMAL)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (curState == STATE_RECORDING_NORMAL) {
                    // 录制结束
                    stopRecord()
                    changeState(STATE_FINISH)
                } else {
                    // 录制取消
                    cancelRecord()
                    changeState(STATE_CANCEL)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelRecord()
                changeState(STATE_CANCEL)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun wantToCancel(x: Int, y: Int): Boolean {
        return if (x < 0 || x > width) {
            true
        } else {
            y < -DISTANCE_Y_CANCEL || y > height + DISTANCE_Y_CANCEL
        }
    }

    private fun changeState(state: Int) {
        if (curState == state) {
            return
        }

        curState = state
        when (curState) {
            STATE_NORMAL -> {
                setBackgroundResource(R.drawable.audio_btn_recorder_normal)
                setText(R.string.audio_str_recorder_normal)
            }
            STATE_RECORDING_NORMAL -> {
                setBackgroundResource(R.drawable.audio_btn_recording)
                setText(R.string.audio_str_recorder_recording)
            }
            STATE_RECORDING_CANCEL -> {
                setBackgroundResource(R.drawable.audio_btn_recording)
                setText(R.string.audio_str_recorder_recording_cancel)
            }
            STATE_CANCEL -> {
                setBackgroundResource(R.drawable.audio_btn_recorder_normal)
                setText(R.string.audio_str_recorder_normal)
            }
            STATE_FINISH -> {
                setBackgroundResource(R.drawable.audio_btn_recorder_normal)
                setText(R.string.audio_str_recorder_normal)
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var dir: File? = null
    private var filePath: String? = null
    private var startTime: Long = 0

    fun setDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        this.dir = dir
    }

    private val mHandler = Handler()
    private val mUpdateMicStatusTimer = Runnable { updateMicStatus() }


    private fun startRecord() {
        mediaRecorder?.let {
            it.reset()
            it.release()
        }
        mediaRecorder = null

        // 创建新得MediaPlayer
        mediaRecorder = MediaRecorder()
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC) // 设置麦克风
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            if (dir == null) {
                dir = context.externalCacheDir
            }
            val file = File(dir, generateFileName())
            filePath = file.absolutePath
            mediaRecorder!!.setOutputFile(filePath)
            mediaRecorder!!.setMaxDuration(MAX_LENGTH)
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            startTime = System.currentTimeMillis()
            Log.e(TAG, "Start Path: " + filePath + "StartTime: " + startTime)
            recordStateChangedListener?.onStart(filePath)

            // 录音开启成功 显示Dialog
            showRecordingDialog()

            // 开始更新状态
            updateMicStatus()
        } catch (e: Exception) {
            e.printStackTrace()
            recordStateChangedListener?.onError(e.message)
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            filePath = null
            Log.i(TAG, "call startAmr(File mRecAudioFile) failed!" + e.message)
        }
    }

    private fun stopRecord() {
        // 关闭Dialog
        dismissDialog()
        if (mediaRecorder == null) {
            return
        }

        try {
            mediaRecorder!!.stop()
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            if (duration < 1000) {
                throw RuntimeException("录制时间太短 : " + duration + "ms")
            }

            recordStateChangedListener?.onFinish(filePath, duration)
            Log.d(TAG, "Stop: $filePath")
        } catch (e: Exception) {
            Log.d(TAG, "Error: " + e.message)
            recordStateChangedListener?.onError(e.message)

            // 录音发生错误 删除错误文件
            filePath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            }
        } finally {
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
            mediaRecorder = null
            filePath = null
        }
    }

    private fun cancelRecord() {
        // 关闭Dialog
        dismissDialog()
        if (mediaRecorder == null) {
            return
        }
        try {
            mediaRecorder!!.stop()
            recordStateChangedListener?.onCancel()
            Log.d(TAG, "Cancel: $filePath")
        } catch (e: Exception) {
            Log.d(TAG, "Error: " + e.message)
            recordStateChangedListener?.onError(e.message)
        } finally {
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
            mediaRecorder = null

            // 删除文件
            filePath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            }
            filePath = null
        }
    }

    private fun updateMicStatus() {
        if (mediaRecorder == null) {
            return
        }
        val level = getVoiceLevel(levelResource.size)
        updateVoiceLevel(level)
        recordStateChangedListener?.onUpdate(filePath, System.currentTimeMillis() - startTime)

        // 间隔取样时间
        mHandler.postDelayed(mUpdateMicStatusTimer, SPACE)
    }

    private fun getVoiceLevel(maxLevel: Int): Int {
        if (mediaRecorder == null) {
            return 0
        }

        return try {
            maxLevel * mediaRecorder!!.maxAmplitude / 32768
        } catch (ignored: Exception) {
            0
        }
    }

    private var dialog: Dialog? = null
    private var ivLevel: AppCompatImageView? = null
    private var tvNotice: AppCompatTextView? = null
    private fun showRecordingDialog() {
        val context = context ?: return
        dialog = Dialog(context)
        val window = dialog!!.window
        if (window == null) {
            dialog = null
            return
        }
        @SuppressLint("InflateParams") val view =
            LayoutInflater.from(context).inflate(R.layout.audio_dialog_record_audio, null)
        ivLevel = view.findViewById(R.id.iv_level)
        tvNotice = view.findViewById(R.id.tv_notis)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0.0f)
        window.setLayout(180, 180)
        dialog!!.setContentView(view)
        dialog!!.show()
    }

    private fun updateVoiceLevel(level: Int) {
        if (dialog != null && dialog!!.isShowing && ivLevel != null) {
            ivLevel!!.setImageResource(levelResource[level])
        }
    }

    private fun updateNoticeText(@StringRes id: Int) {
        if (dialog != null && dialog!!.isShowing && tvNotice != null) {
            tvNotice!!.setText(id)
        }
    }

    private fun dismissDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        dialog = null
    }

    // 状态通知相关
    private var recordStateChangedListener: OnAudioRecordStateChangedListener? = null
    fun setRecordStateChangedListener(recordStateChangedListener: OnAudioRecordStateChangedListener?) {
        this.recordStateChangedListener = recordStateChangedListener
    }

    interface OnAudioRecordStateChangedListener {
        fun onPermissionDenied(permission: String?)
        fun onStart(filePath: String?)
        fun onUpdate(filePath: String?, duration: Long)
        fun onFinish(filePath: String?, duration: Long)
        fun onError(error: String?)
        fun onCancel()
    }

    companion object {
        private val TAG = AudioRecorderButton::class.java.simpleName

        // 触摸逻辑和状态
        // 滑动距离
        private const val DISTANCE_Y_CANCEL = 100

        // 间隔取样时间
        const val SPACE = 100L

        // 状态
        private const val STATE_NORMAL = 1
        private const val STATE_RECORDING_NORMAL = 2
        private const val STATE_RECORDING_CANCEL = 3
        private const val STATE_CANCEL = 4
        private const val STATE_FINISH = 5

        // 录音相关
        private const val MAX_LENGTH = 1000 * 60 * 10 // 最大录音时长1000*60*10;
        private fun generateFileName(): String {
            return UUID.randomUUID().toString() + ".amr"
        }

        // Dialog相关
        private val levelResource = intArrayOf(
            R.drawable.audio_volume_record_level_1,
            R.drawable.audio_volume_record_level_2,
            R.drawable.audio_volume_record_level_3,
            R.drawable.audio_volume_record_level_4,
            R.drawable.audio_volume_record_level_5,
            R.drawable.audio_volume_record_level_6,
            R.drawable.audio_volume_record_level_7,
            R.drawable.audio_volume_record_level_8
        )
    }

}