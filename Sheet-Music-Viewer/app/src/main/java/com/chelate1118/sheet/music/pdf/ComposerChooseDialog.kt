package com.chelate1118.sheet.music.pdf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import com.chelate1118.sheet.music.files.FileSystem
import com.chelate1118.sheet.music.files.FileSystemFragment
import kotlin.math.roundToInt

class ComposerChooseDialog(
    private val composerInput: EditText,
    private val fragment: FileSystemFragment
) : DialogFragment() {
    private lateinit var layout: LinearLayout

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setLayout()

        return activity?.let {
            AlertDialog.Builder(it).setView(layout).setTitle("Choose Composer").create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setLayout() {
        layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
            )

            updatePadding(top = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15F,
                resources.displayMetrics
            ).roundToInt())

            FileSystem.updateFileTree(context)

            for (composer in FileSystem.composers) {
                addView(View(activity).apply {
                    layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            1F,
                            resources.displayMetrics
                        ).roundToInt()
                    )
                    setBackgroundColor(Color.parseColor("#AAAAAA"))
                })

                addView(composerItem(composer))
            }
        }
    }

    private fun composerItem(composer: String): View {
        return LinearLayout(context).apply {
            addView(TextView(activity).apply {
                text = composer
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        40F,
                        resources.displayMetrics
                    ).roundToInt(),
                    1F
                )
                gravity = Gravity.CENTER
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(Color.BLACK)
                setBackgroundResource(TypedValue().apply {
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
                }.resourceId)
                isFocusable = true
                isClickable = true
                setOnClickListener {
                    composerInput.setText(text.toString())
                    fragment.fileListAdapter.notifyDataSetChanged()
                    dismiss()
                }
            })
        }
    }
}