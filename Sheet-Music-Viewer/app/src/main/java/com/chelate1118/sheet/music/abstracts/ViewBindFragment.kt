package com.chelate1118.sheet.music.abstracts

import androidx.fragment.app.Fragment

abstract class ViewBindFragment: Fragment() {
    abstract fun bindView()
    abstract fun setView()

    override fun onStart() {
        super.onStart()

        bindView()
        setView()
    }
}