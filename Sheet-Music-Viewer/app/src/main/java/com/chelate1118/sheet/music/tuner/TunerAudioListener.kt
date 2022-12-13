package com.chelate1118.sheet.music.tuner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

private const val PERMISSION_REQUEST_CODE = 100
private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
private const val SAMPLE_RATE = 44100
private const val CHANNELS = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
private var BUFFER_SIZE = 0

class TunerAudioListener(private val activity: AppCompatActivity) {
    private lateinit var audioRecord: AudioRecord

    @SuppressLint("MissingPermission")
    fun start() {
        BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNELS, AUDIO_ENCODING
        )

        requestPermission()
        if (!hasPermission) return

        audioRecord = AudioRecord(
            AUDIO_SOURCE, SAMPLE_RATE, CHANNELS, AUDIO_ENCODING, BUFFER_SIZE
        )

        RecordThread(
            audioRecord,
            TunerDialog(),
            activity
        ).start()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE
        )
    }

    private val hasPermission
        get() = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
}

class RecordThread(
    private val audioRecord: AudioRecord,
    private val tunerDialog: TunerDialog,
    private val activity: AppCompatActivity
) : Thread() {
    init {
        tunerDialog.recordThread = this
    }

    private val fftBuffer = arrayListOf<Byte>()
    private val readData = ByteArray(BUFFER_SIZE)
    private val handler = Handler(Looper.getMainLooper())

    private var count = 0

    private fun addByte(byte: Byte) {
        fftBuffer.add(byte)
        if (fftBuffer.size > TunerFFT.bufferSize) {
            fftBuffer.removeAt(0)
            count++
            if (count > TunerFFT.bufferSize * 0.125) {
                count = 0
                val freq = TunerFFT.findFrequency(fftBuffer)
                handler.post {
                    tunerDialog.textView.text = String.format("%.1f", freq)
                }
            }
        }
    }

    override fun run() {
        tunerDialog.show(activity.supportFragmentManager, "Tuner Dialog")
        audioRecord.startRecording()

        while (!isInterrupted) {
            audioRecord.read(readData, 0, BUFFER_SIZE)

            for (byte in readData) {
                if (isInterrupted) break

                addByte(byte)
            }
        }

        audioRecord.stop()
        audioRecord.release()
    }
}
