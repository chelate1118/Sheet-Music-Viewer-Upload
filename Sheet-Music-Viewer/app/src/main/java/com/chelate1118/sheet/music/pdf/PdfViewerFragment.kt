package com.chelate1118.sheet.music.pdf

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.chelate1118.sheet.music.MainActivity
import com.chelate1118.sheet.music.R
import com.chelate1118.sheet.music.abstracts.ViewBindFragment
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.slider.Slider
import com.chelate1118.sheet.music.files.FileSystem
import org.apache.pdfbox.pdmodel.PDDocument
import kotlin.math.roundToInt

private const val ARG_PARAM1 = "pdf data"

class PdfViewerFragment: ViewBindFragment() {
    private lateinit var pdfView: PDFView
    private lateinit var turnBackButton: AppCompatButton
    private lateinit var setPage: Slider
    private lateinit var showPage: TextView
    private lateinit var pdfUri: Uri
    private val mainActivity
        get() = requireActivity() as MainActivity

    private var pageCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            pdfUri = Uri.parse(it.getString(ARG_PARAM1))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_pdf_viewer, container, false)

    override fun bindView() {
        pdfView = requireView().findViewById(R.id.pdfView)
        turnBackButton = requireView().findViewById(R.id.pdf_turn_back)
        setPage = requireView().findViewById(R.id.pdf_set_page_slider)
        showPage = requireView().findViewById(R.id.pdf_show_page)
    }

    @SuppressLint("SetTextI18n")
    override fun setView() {
        val pdfByteArray = getByteArray(pdfUri, requireContext())

        pageCount = PDDocument.load(pdfByteArray).numberOfPages

        pdfView
            .fromBytes(pdfByteArray)
            .swipeHorizontal(true)
            .spacing(8)
            .defaultPage(mainActivity.activePdf!!.lastPage)
            .onPageChange { page, pageCount ->
                mainActivity.activePdf!!.lastPage = page
                FileSystem.updateFileTree(requireContext())

                showPage.text = "${page+1} / $pageCount"
                setPage.apply {
                    value = stepSize * mainActivity.activePdf!!.lastPage
                }
            }
            .onTap {
                if(it.x > pdfView.width/3*2 && pdfView.currentPage+1 < pdfView.pageCount) {
                    pdfView.jumpTo(pdfView.currentPage+1)
                } else if (it.x < pdfView.width/3 && pdfView.currentPage-1 >= 0) {
                    pdfView.jumpTo(pdfView.currentPage-1)
                }
                true
            }
            .enableDoubletap(false)
            .pageFitPolicy(FitPolicy.BOTH)
            .pageFling(mainActivity.orientation == Configuration.ORIENTATION_PORTRAIT)
            .load()

        turnBackButton.setOnClickListener {
            turnBack()
        }

        setPage.apply {
            if (pageCount > 1) stepSize = 1F / (pageCount-1)

            value = stepSize * mainActivity.activePdf!!.lastPage

            addOnChangeListener { _, value, byUser ->
                if (byUser) {
                    pdfView.jumpTo((value * (pageCount-1)).roundToInt())
                }
            }
        }

        showPage.text = "${mainActivity.activePdf!!.lastPage+1} / $pageCount"
    }

    fun turnBack() {
        mainActivity.activePdf = null
    }

    companion object {
        @JvmStatic
        fun newInstance(pdfUri: Uri) =
            PdfViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, pdfUri.toString())
                }
            }
    }
}