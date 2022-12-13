package com.chelate1118.sheet.music.pdf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.chelate1118.sheet.music.R
import com.chelate1118.sheet.music.files.FileSystem
import com.chelate1118.sheet.music.files.FileSystemFragment

@SuppressLint("Range")
fun getFileName(uri: Uri, context: Context): String {
    if (uri.scheme.equals("content")) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            cursor.close()
            return fileName
        }
        return "[Error] File name not found"
    }

    return uri.lastPathSegment ?: return "[Error] File name not found"
}

fun getByteArray(uri: Uri, context: Context): ByteArray {
    val inputStream =
        context.contentResolver.openInputStream(uri) ?: throw IllegalStateException("Incorrect Uri")

    val data = inputStream.readBytes()
    inputStream.close()

    return data
}

class ChangePdfInfo(
    private val pdfInfo: PdfInfo,
    private val fragmentActivity: FragmentActivity,
    private val fragment: FileSystemFragment
) : DialogFragment() {
    private lateinit var titleInput: EditText
    private lateinit var composerInput: EditText

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                val view = layoutInflater.inflate(R.layout.dialog_change_info, null)

                setSaveCancel(view)
                setDeleteButton(view)
                setTitleInput(view)
                setComposerInput(view)

                setView(view)
            }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setSaveCancel(root: View) {
        root.findViewById<AppCompatButton>(R.id.info_cancel).setOnClickListener { dismiss() }
        root.findViewById<AppCompatButton>(R.id.info_save).setOnClickListener {
            pdfInfo.title = titleInput.text.toString()
            pdfInfo.composer = composerInput.text.toString()
            if (pdfInfo.composer == "") pdfInfo.composer = null

            dismiss()
        }
    }

    private fun setDeleteButton(root: View) {
        root.findViewById<LinearLayout>(R.id.info_delete_button).setOnClickListener {
            FileSystemFragment.pdfList.remove(pdfInfo)
            dismiss()
        }
    }

    private fun setTitleInput(root: View) {
        titleInput = root.findViewById(R.id.info_input_title)
        titleInput.setText(pdfInfo.title)
    }

    private fun setComposerInput(root: View) {
        composerInput = root.findViewById(R.id.info_input_composer)
        composerInput.setText(pdfInfo.composer ?: "")

        root.findViewById<AppCompatButton>(R.id.info_composer_dialog_show).setOnClickListener {
            ComposerChooseDialog(composerInput, fragment)
                .show(fragmentActivity.supportFragmentManager, "Composer Choose")
        }
    }

    override fun onDestroy() {
        FileSystem.updateFileTree(requireContext())
        fragment.fileListAdapter.notifyDataSetChanged()

        super.onDestroy()
    }
}

class PdfInfo(
    var pdfUri: Uri,
    var composer: String?,
    var title: String,
    val id: Int,
    var lastPage: Int
) {
    fun changeInfoDialog(activity: FragmentActivity, fragment: FileSystemFragment) {
        ChangePdfInfo(this, activity, fragment)
            .show(activity.supportFragmentManager, "Change Info Dialog")
    }
}
