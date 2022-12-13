package com.chelate1118.sheet.music.files

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import com.chelate1118.sheet.music.MainActivity
import com.chelate1118.sheet.music.R
import com.chelate1118.sheet.music.abstracts.ViewBindFragment
import com.chelate1118.sheet.music.files.FileSystem.addPdf
import com.chelate1118.sheet.music.pdf.ComposerFilterDialog
import com.chelate1118.sheet.music.pdf.PdfInfo

class FileSystemFragment : ViewBindFragment() {
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var listView: ListView
    private lateinit var addButton: AppCompatButton
    private lateinit var mainLogo: ImageView
    private lateinit var filterButton: AppCompatButton
    lateinit var fileListAdapter: FileListAdapter
        private set
    private val mainActivity
        get() = requireActivity() as MainActivity

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            mainActivity.activePdf = uri?.let {
                addPdf(uri, requireContext())
            }
        }

        return inflater.inflate(R.layout.fragment_file_system, container, false)
    }

    override fun bindView() {
        addButton = requireView().findViewById(R.id.file_add_button)
        listView = requireView().findViewById(R.id.file_list_view)
        mainLogo = requireView().findViewById(R.id.main_logo)
        filterButton = requireView().findViewById(R.id.file_filter_button)
    }

    override fun setView() {
        addButton.setOnClickListener { getContent.launch("application/pdf") }
        fileListAdapter = FileListAdapter(requireContext())
        mainLogo.setImageBitmap(
            BitmapFactory.decodeStream(mainActivity.assets.open("AppLogo.png"))
        )
        listView.let {
            it.adapter = fileListAdapter
            it.setOnItemClickListener { _, _, pos, _ ->
                mainActivity.activePdf = showingList[pos]
            }
            it.setOnItemLongClickListener { _, _, pos, _ ->
                pdfList[pos].changeInfoDialog(requireActivity(), this)
                true
            }
        }
        filterButton.setOnClickListener {
            ComposerFilterDialog(this).show(mainActivity.supportFragmentManager, "Composer Filter")
        }
    }

    companion object {
        val pdfList = ArrayList<PdfInfo>()
        lateinit var showingList: List<PdfInfo>
        var composer: String? = null

        @JvmStatic
        fun newInstance() = FileSystemFragment()
    }
}