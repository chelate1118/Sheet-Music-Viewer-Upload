package com.chelate1118.sheet.music.files

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.chelate1118.sheet.music.pdf.getByteArray
import com.chelate1118.sheet.music.pdf.PdfInfo
import com.chelate1118.sheet.music.pdf.getFileName
import com.shockwave.pdfium.PdfiumCore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

object FileSystem {
    private lateinit var fileTree: JSONArray
    private const val fileTreePath = "Pdf-files.json"
    var composers = ArrayList<String>()
    private var currentID = 0

    fun loadFileSystem(context: Context) {
        val pdfFilesJsonFile = internalFile(fileTreePath, context)
        if (!pdfFilesJsonFile.isFile) {
            FileWriter(pdfFilesJsonFile).apply {
                write("[]")
                flush()
                close()
            }
        }

        fileTree = JSONArray(pdfFilesJsonFile.readText())

        for (i in 0 until fileTree.length()) {
            currentID = currentID.coerceAtLeast(fileTree.getJSONObject(i).getInt("ID"))
        }

        setPdfList()
    }

    private fun setPdfList() {
        FileSystemFragment.pdfList.clear()
        for (i in 0 until fileTree.length()) {
            FileSystemFragment.pdfList.add(jsonObjectToPdfInfo(fileTree.getJSONObject(i)))
        }
    }

    private fun jsonObjectToPdfInfo(jsonObject: JSONObject): PdfInfo {
        var composer: String? = null
        try {
            composer = jsonObject.getString("composer")
        } catch (_: JSONException) {}

        return PdfInfo(
            Uri.parse(jsonObject.getString("uri")),
            composer,
            jsonObject.getString("title"),
            jsonObject.getInt("ID"),
            jsonObject.getInt("last-page")
        )
    }

    fun addPdf(uri: Uri, context: Context): PdfInfo {
        ++currentID

        val pdfInfo = uriToPdfInfo(uri, context)
        FileSystemFragment.pdfList.add(pdfInfo)

        updateFileTree(context)

        return pdfInfo
    }

    private fun uriToPdfInfo(uri: Uri, context: Context): PdfInfo {
        val newUri = savePdfToInternal(uri, context)

        return PdfInfo(
            newUri,
            null,
            getFileName(uri, context).let { it.substring(0, it.lastIndexOf('.')) },
            currentID,
            0
        ).apply {
            extractThumbnail(this, context)
        }
    }

    private fun extractThumbnail(pdfInfo: PdfInfo, context: Context) {
        context.contentResolver.openFileDescriptor(pdfInfo.pdfUri, "r")?.let { fileDescriptor ->
            PdfiumCore(context).apply {
                newDocument(fileDescriptor).let{
                    openPage(it , 0)
                    val width = getPageWidthPoint(it, 0)
                    val height = getPageHeightPoint(it, 0)
                    val bmp: Bitmap
                    if (width < height) {
                        bmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
                        renderPageBitmap(it, bmp, 0, 0, 0, width, width)
                    }
                    else {
                        bmp = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888)
                        renderPageBitmap(it, bmp, 0, 0, 0, height, height)
                    }
                    saveBmp(pdfInfo.id, bmp, context)
                    closeDocument(it)
                }
            }
            fileDescriptor.close()
        }
    }

    private fun saveBmp(id: Int, bmp: Bitmap, context: Context) {
        println("$id.png")
        val fos = FileOutputStream(internalFile("$id.png", context))
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    }

    fun updateFileTree(context: Context) {
        setFileTree()
        writeFileTree(context)
    }

    private fun setFileTree() {
        fileTree = JSONArray("[]")
        composers = ArrayList()
        for (pdfInfo in FileSystemFragment.pdfList) {
            val jsonObject = JSONObject()

            jsonObject.put("uri", pdfInfo.pdfUri)
            jsonObject.put("ID", pdfInfo.id)
            pdfInfo.composer?.let {
                jsonObject.put("composer", it)
                if (composers.none { com -> it == com }) {
                    composers.add(it)
                }
            }
            jsonObject.put("title", pdfInfo.title)
            jsonObject.put("last-page", pdfInfo.lastPage)

            fileTree.put(jsonObject)
        }
    }

    private fun writeFileTree(context: Context) {
        FileWriter(internalFile(fileTreePath, context)).apply {
            write(fileTree.toString())
            flush()
            close()
        }
    }

    private fun savePdfToInternal(uri: Uri, context: Context): Uri {
        val newFile = writeFileToInternal("$currentID.pdf", uri, context)

        return newFile.toUri()
    }

    private fun writeFileToInternal(name: String, external: Uri, context: Context): File {
        val file = internalFile(name, context)

        if (file.exists()) file.delete()

        FileOutputStream(file).apply {
            write(getByteArray(external, context))
            close()
        }

        return file
    }

    internal fun internalFile(path: String, context: Context): File {
        return File("${context.filesDir}/${path}")
    }
}