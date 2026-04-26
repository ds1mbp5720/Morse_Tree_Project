package com.example.morsedecoder.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.sin

class MorseToneGenerator {
    private val sampleRate = 44100
    private val freq = 700.0
    private var activeTrack: AudioTrack? = null
    
    @Volatile
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
        
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
            
        activeTrack = track
        
        Thread {
            try {
                track.play()
                var angle = 0.0
                val samples = ShortArray(1024)
                var fadeCount = 0
                val fadeSamples = 441 // 10ms fade
                
                // Play logic
                while (isPlaying && activeTrack == track) {
                    for (i in samples.indices) {
                        val base = sin(angle)
                        // Smooth fade in
                        val envelope = if (fadeCount < fadeSamples) {
                            fadeCount.toDouble() / fadeSamples
                        } else 1.0
                        
                        samples[i] = (base * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
                        angle += 2.0 * Math.PI * freq / sampleRate
                        if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                        if (fadeCount < fadeSamples) fadeCount++
                    }
                    track.write(samples, 0, samples.size)
                }

                // Smooth fade out to prevent cracking
                fadeCount = fadeSamples
                while (fadeCount > 0) {
                    for (i in samples.indices) {
                        val base = sin(angle)
                        val envelope = fadeCount.toDouble() / fadeSamples
                        samples[i] = (base * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
                        angle += 2.0 * Math.PI * freq / sampleRate
                        if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                        if (fadeCount > 0) fadeCount--
                    }
                    track.write(samples, 0, samples.size)
                }

                track.stop()
            } catch (e: Exception) {
                // Handle or log
            } finally {
                try {
                    track.release()
                } catch (e: Exception) {}
                synchronized(this) {
                    if (activeTrack == track) activeTrack = null
                }
            }
        }.start()
    }

    @Synchronized
    fun stop() {
        isPlaying = false
    }
}
