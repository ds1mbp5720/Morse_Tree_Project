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
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
                
                while (isPlaying) {
                    for (i in samples.indices) {
                        samples[i] = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
                        angle += 2.0 * Math.PI * freq / sampleRate
                        if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                    }
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.write(samples, 0, samples.size)
                    } else {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    track.stop()
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
