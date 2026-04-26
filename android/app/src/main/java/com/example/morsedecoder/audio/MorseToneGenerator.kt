package com.example.morsedecoder.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import kotlin.math.sin

class MorseToneGenerator {
    private val sampleRate = 44100
    private val freq = 700.0
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    @Synchronized
    fun start() {
        if (isPlaying) return
        isPlaying = true
        
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, 
            AudioFormat.CHANNEL_OUT_MONO, 
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)
        
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        
        audioTrack?.play()
        
        Thread {
            var angle = 0.0
            val samples = ShortArray(1024)
            while (isPlaying) {
                for (i in samples.indices) {
                    samples[i] = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
                    angle += 2.0 * Math.PI * freq / sampleRate
                    if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                }
                audioTrack?.write(samples, 0, samples.size)
            }
            synchronized(this) {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {}
                finally {
                    audioTrack = null
                }
            }
        }.start()
    }

    @Synchronized
    fun stop() {
        isPlaying = false
    }
}
