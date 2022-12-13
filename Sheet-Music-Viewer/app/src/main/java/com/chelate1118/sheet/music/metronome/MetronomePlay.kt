package com.chelate1118.sheet.music.metronome

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Space
import androidx.core.content.ContextCompat
import com.chelate1118.sheet.music.R

private enum class MetronomeState {
    LeadActivate,
    LeadDeactivate,
    NormalActivate,
    NormalDeactivate;

    fun toDrawable() = when (this) {
        NormalActivate -> R.drawable.metronome_circle_normal_activate
        NormalDeactivate -> R.drawable.metronome_circle_normal_deactivate
        LeadActivate -> R.drawable.metronome_circle_lead_activate
        LeadDeactivate -> R.drawable.metronome_circle_lead_deactivate
    }
}

class PlayThread(private val circleLayout: CircleLayout) : Thread() {
    private val handler = android.os.Handler(Looper.getMainLooper())

    override fun run() {
        super.run()

        try {
            while (!isInterrupted) {
                handler.post { circleLayout.increaseClick() }
                sleep(circleLayout.clickMillis)
            }
        } catch (_: InterruptedException) {
        } finally {
            handler.post { circleLayout.deleteClick() }
        }
    }
}

class CircleLayout(
    private var context: Context,
    var layout: LinearLayout,
    private var parent: MetronomeFragment,
    private val orientation: Int
) {
    private val beatsPerBar
        get() = parent.beatsPerBar
    private val clicksPerBeat
        get() = parent.clicksPerBeat
    private val bpm
        get() = parent.bpm
    val clickMillis
        get() = (60.0 / bpm / clicksPerBeat * 1000).toLong()

    private var clickCount = clicksPerBeat
    private var beatCount = MAX_BEAT

    var playThread: PlayThread = PlayThread(this)

    var isPlaying = false
        set(value) {
            field = value
            when {
                value -> playThread = PlayThread(this).apply { start() }
                else -> playThread.interrupt()
            }
        }

    fun increaseClick() {
        var sound = 1
        clickCount++
        if (clickCount >= clicksPerBeat) {
            clickCount = 0
            beatCount++
        } else {
            sound = 2
        }
        if (beatCount >= beatsPerBar) {
            beatCount = 0
            sound = 0
        }

        play(sound)
        setCircles()
    }

    fun deleteClick() {
        clickCount = clicksPerBeat
        beatCount = MAX_BEAT

        setCircles()
    }

    private fun play(soundNum: Int) {
        val sound = arrayOf (R.raw.beat_1, R.raw.beat_2, R.raw.beat_3)
        MediaPlayer.create(context, sound[soundNum]).apply {
            start()
            setOnCompletionListener {
                it.reset()
                it.release()
            }
        }
    }

    fun setCircles() {
        layout.removeAllViews()
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.orientation = LinearLayout.VERTICAL
            layout.layoutParams = LinearLayout.LayoutParams(
                MATCH_PARENT,
                0,
                1F
            )
        }
        else {
            layout.orientation = LinearLayout.HORIZONTAL
            layout.layoutParams = LinearLayout.LayoutParams(
                0,
                MATCH_PARENT,
                1F
            )
        }

        for (i in 0 until beatsPerBar) {
            addCircle(
                when {
                    i == 0 && beatCount == 0 -> MetronomeState.LeadActivate
                    i == 0 && beatCount != 0 -> MetronomeState.LeadDeactivate
                    beatCount == i -> MetronomeState.NormalActivate
                    else -> MetronomeState.NormalDeactivate
                }
            )
        }
    }

    private fun addCircle(state: MetronomeState) {
        layout.addView(newSpace())
        layout.addView(newCircle(state))
        layout.addView(newSpace())
    }

    private fun newCircle(state: MetronomeState): View = View(context).apply {
        background = ContextCompat.getDrawable(context, state.toDrawable())
        layoutParams = LinearLayout.LayoutParams(
            100, 100
        )
    }

    private fun newSpace(): Space = Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1.0f
        )
    }
}