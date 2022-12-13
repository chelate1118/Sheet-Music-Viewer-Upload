package com.chelate1118.sheet.music

import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.chelate1118.sheet.music.databinding.ActivityMainBinding
import com.chelate1118.sheet.music.files.FileSystemFragment
import com.chelate1118.sheet.music.metronome.MetronomeFragment
import com.chelate1118.sheet.music.pdf.PdfViewerFragment
import com.chelate1118.sheet.music.tuner.TunerAudioListener
import com.chelate1118.sheet.music.pdf.PdfInfo
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    companion object {
        var metronomeState = Triple(4, 1, 150)
    }

    private lateinit var layout: ActivityMainBinding

    private var pdfViewerFragment: PdfViewerFragment? = null
    private var metronomeFragment: MetronomeFragment? = null
    private var fileSystemFragment: FileSystemFragment? = null
    var orientation = 0
        set(value) {
            if (field == value) return
            field = value
            setOrientation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityMainBinding.inflate(layoutInflater)
        setContentView(layout.root)

        orientation = resources.configuration.orientation
        setMetronomeSwitch()
        setTunerButton()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            orientation = newConfig.orientation
    }

    private fun setOrientation() {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.root.orientation = LinearLayout.HORIZONTAL
            layout.mainFrameLayout.layoutParams = LinearLayout.LayoutParams(
                0,
                MATCH_PARENT,
                1F
            )
        } else {
            layout.mainLinearLayout.orientation = LinearLayout.VERTICAL
            layout.mainFrameLayout.layoutParams = LinearLayout.LayoutParams(
                MATCH_PARENT,
                0,
                1F
            )
        }

        activePdf = activePdf
        isMetronomeShowing = isMetronomeShowing
    }

    var activePdf: PdfInfo? = null
        set(pdfInfo) {
            field = pdfInfo
            if (pdfInfo != null) {
                showPdf(pdfInfo.pdfUri)

                FileSystemFragment.pdfList.remove(pdfInfo)
                FileSystemFragment.pdfList.add(0, pdfInfo)
            }
            else selectPdf()
        }

    private fun showPdf(pdfUri: Uri) {
        resetFragment(pdfViewerFragment)
        resetFragment(fileSystemFragment)
        pdfViewerFragment = null
        fileSystemFragment = null

        pdfViewerFragment = PdfViewerFragment.newInstance(pdfUri)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.mainViewerFragment, pdfViewerFragment!!)
        }
    }

    private fun selectPdf() {
        resetFragment(pdfViewerFragment)
        resetFragment(fileSystemFragment)
        pdfViewerFragment = null
        fileSystemFragment = null

        fileSystemFragment = FileSystemFragment.newInstance()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.mainViewerFragment, fileSystemFragment!!)
        }
    }

    @Deprecated("TODO")
    override fun onBackPressed() {
        pdfViewerFragment?.turnBack() ?: super.onBackPressed()
    }

    private fun setMetronomeSwitch() {
        layout.metronomeOnOff.setOnCheckedChangeListener { _, value ->
            isMetronomeShowing = value
        }
    }

    private var isMetronomeShowing = false
        set(show) {
            field = show

            if (show) showMetronome()
            else hideMetronome()
        }

    private fun showMetronome() {
        layout.metronomeFragment.layoutParams =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) LinearLayout.LayoutParams(
                MATCH_PARENT,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100F, resources.displayMetrics)
                    .roundToInt(),
                0F
            )
            else LinearLayout.LayoutParams(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100F, resources.displayMetrics)
                    .roundToInt(),
                MATCH_PARENT,
                0F
            )

        resetFragment(metronomeFragment)
        metronomeFragment = MetronomeFragment.newInstance(
            metronomeState.first, metronomeState.second, metronomeState.third, orientation
        )

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.metronomeFragment, metronomeFragment!!)
        }
    }

    private fun hideMetronome() {
        resetFragment(metronomeFragment)
        layout.metronomeFragment.layoutParams =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) LinearLayout.LayoutParams(
                MATCH_PARENT,
                0,
                0F
            )
            else LinearLayout.LayoutParams(
                    0,
                    MATCH_PARENT,
                    0F
                )
    }

    private fun resetFragment(fragment: Fragment?) {
        fragment ?: return
        supportFragmentManager.commit {
            remove(fragment)
        }
    }

    private fun setTunerButton() {
        layout.tunerOn.setOnClickListener {
            showTuner()
        }
    }

    private fun showTuner() {
        TunerAudioListener(this).start()
    }
}
