package com.chelate1118.sheet.music.metronome

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.chelate1118.sheet.music.MainActivity
import com.chelate1118.sheet.music.R
import com.chelate1118.sheet.music.abstracts.ViewBindFragment
import kotlin.math.roundToInt

private const val ARG_PARAM1 = "beats / bar"
private const val ARG_PARAM2 = "clicks / beat"
private const val ARG_PARAM3 = "beats / minute"
private const val ARG_PARAM4 = "orientation"

private const val MIN_BPM = 40
private const val MAX_BPM = 360
const val MAX_BEAT = 8

class MetronomeFragment : ViewBindFragment() {
    private lateinit var tempoButton: Button
    private lateinit var playPauseButton: ToggleButton
    private lateinit var circleLayout: CircleLayout
    private var orientation = 0

    var beatsPerBar = 4
        set(value) {
            field = value
            circleLayout.setCircles()
            bindState()
        }

    var clicksPerBeat = 1
        set(value) {
            field = value
            bindState()
        }

    var bpm = 120
        set(value) {
            field = value
            tempoButton.text = value.toString()
            bindState()
        }

    private fun bindState() {
        MainActivity.metronomeState = Triple(beatsPerBar, clicksPerBeat, bpm)
    }

    private var previousTapTime: Long = 0
        set(value) {
            val newBpm = 60.0 / (value - field) * 1000
            if (newBpm.roundToInt() in MIN_BPM..MAX_BPM)
                bpm = newBpm.roundToInt()

            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_metronome, container, false)
    }

    override fun bindView() {
        arguments?.let {
            orientation = it.getInt(ARG_PARAM4)
        }

        tempoButton = requireView().findViewById(R.id.metronome_tempo_button)
        playPauseButton = requireView().findViewById(R.id.metronome_play_pause_toggle)
        circleLayout = CircleLayout(
            requireContext(),
            requireView().findViewById(R.id.metronome_circle_layout),
            this,
            orientation
        )
    }

    override fun setView() {
        arguments?.let {
            beatsPerBar = it.getInt(ARG_PARAM1)
            clicksPerBeat = it.getInt(ARG_PARAM2)
            bpm = it.getInt(ARG_PARAM3)
        }

        requireView().findViewById<LinearLayout>(R.id.metronome_root_layout).orientation =
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) LinearLayout.VERTICAL
            else LinearLayout.HORIZONTAL

        setTempoButton()
        setPlayPauseButton()
    }

    private fun setTempoButton() {
        tempoButton.setOnClickListener {
            pickTempoDialog()
        }
    }

    private fun setPlayPauseButton() {
        playPauseButton.setOnCheckedChangeListener { toggleButton, isChecked ->
            toggleButton.background = ContextCompat.getDrawable(
                requireContext(),
                if (isChecked) R.drawable.metronome_pause
                else R.drawable.metronome_play
            )
            circleLayout.isPlaying = isChecked
        }
    }

    @SuppressLint("InflateParams")
    private fun pickTempoDialog() {
        AlertDialog.Builder(context).apply {
            setTitle("Pick Tempo")

            val view = layoutInflater.inflate(R.layout.dialog_tempo, null)
            val beatsPerBarPicker = view.findViewById<NumberPicker>(R.id.tempo_beats_per_bar)
            val clicksPerBeatPicker = view.findViewById<NumberPicker>(R.id.tempo_clicked_per_beat)
            val bpmPicker = view.findViewById<NumberPicker>(R.id.tempo_bpm)
            val tapTempo = view.findViewById<AppCompatButton>(R.id.tempo_tap)

            beatsPerBarPicker.apply {
                minValue = 1
                maxValue = MAX_BEAT
                value = beatsPerBar
                setOnValueChangedListener { _, _, newVal ->
                    beatsPerBar = newVal
                }
            }

            clicksPerBeatPicker.apply {
                minValue = 1
                maxValue = 8
                value = clicksPerBeat
                setOnValueChangedListener { _, _, newVal ->
                    clicksPerBeat = newVal
                }
            }

            bpmPicker.apply {
                minValue = MIN_BPM
                maxValue = MAX_BPM
                value = bpm
                setOnValueChangedListener { _, _, newVal ->
                    bpm = newVal
                }
            }

            tapTempo.setOnClickListener {
                previousTapTime = System.currentTimeMillis()
                bpmPicker.value = bpm
            }

            setView(view)
        }.create().show()
    }

    override fun onDestroy() {
        circleLayout.playThread.interrupt()

        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(beatsPerBar: Int, clicksPerBeat: Int, bpm: Int, orientation: Int) =
            MetronomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, beatsPerBar)
                    putInt(ARG_PARAM2, clicksPerBeat)
                    putInt(ARG_PARAM3, bpm)
                    putInt(ARG_PARAM4, orientation)
                }
            }
    }
}
