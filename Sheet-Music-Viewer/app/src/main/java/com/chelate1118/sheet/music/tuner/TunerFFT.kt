package com.chelate1118.sheet.music.tuner

object TunerFFT {
    init {
        System.loadLibrary("music")
    }

    fun findFrequency(buffer: ArrayList<Byte>): Double {
        return nativeFindFrequency(buffer.toByteArray())
    }

    val bufferSize = getBufferSize()

    private external fun nativeFindFrequency(buffer: ByteArray): Double
    @JvmName("getBufferSize1")
    private external fun getBufferSize(): Int
}