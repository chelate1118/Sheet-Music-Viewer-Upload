package com.chelate1118.sheet.music.files

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.chelate1118.sheet.music.R
import com.chelate1118.sheet.music.pdf.PdfInfo

class FileListAdapter(
    private val context: Context
): BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)

    private val data: List<PdfInfo>
        get() {
            setShowingPdf()
            return FileSystemFragment.showingList
        }

    override fun getCount(): Int{
        return data.size
    }

    override fun getItem(position: Int): PdfInfo {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, converView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.pdf_list_element, null)

        val titleView: TextView = view.findViewById(R.id.pdf_list_title)
        titleView.text = data[position].title

        val informationView: TextView = view.findViewById(R.id.pdf_list_information)
        informationView.text = data[position].composer ?: "Unknown"

        val thumbnail: ImageView = view.findViewById(R.id.pdf_list_thumbnail)
        thumbnail.setImageBitmap(
            BitmapFactory.decodeFile(
                FileSystem.internalFile("${data[position].id}.png", context).path
            )
        )

        return view
    }

    private fun setShowingPdf() {
        FileSystemFragment.let {
            it.showingList = it.pdfList
            it.composer?.let { com ->
                it.showingList = it.pdfList.filter { name -> name.composer == com }
            }
        }
    }
}