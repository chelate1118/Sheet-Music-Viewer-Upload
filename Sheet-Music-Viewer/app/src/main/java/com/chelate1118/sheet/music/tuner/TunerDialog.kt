package com.chelate1118.sheet.music.tuner

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.chelate1118.sheet.music.R

class TunerDialog: DialogFragment() {
    lateinit var textView: TextView
    lateinit var recordThread: RecordThread

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Tuner")
                val view = layoutInflater.inflate(R.layout.dialog_tuner, null)
                textView = view.findViewById(R.id.textView)
                setView(view)
            }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroy() {
        recordThread.interrupt()

        super.onDestroy()
    }
}