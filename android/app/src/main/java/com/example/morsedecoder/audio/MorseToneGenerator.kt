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

    fun start() {
        if (isPlaying) return
        isPlaying = true
        
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, 
            AudioFormat.CHANNEL_OUT_MONO, 
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        val samples = ShortArray(bufferSize)
        for (i in samples.indices) {
            samples[i] = (sin(2.0 * Math.PI * i / (sampleRate / freq)) * Short.MAX_VALUE).toInt().toShort()
        }
        
        audioTrack?.play()
        
        // Feed the buffer in a background thread to keep it playing
        Thread {
            while (isPlaying) {
                audioTrack?.write(samples, 0, samples.size)
            }
        }.start()
    }

    fun stop() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (e: Exception) {
            // Log error
        } finally {
            audioTrack = null
        }
    }
}
