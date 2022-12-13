package com.chelate1118.sheet.music

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chelate1118.sheet.music.databinding.ActivityStartBinding
import com.chelate1118.sheet.music.files.FileSystem

class StartActivity : AppCompatActivity() {
    private lateinit var layout: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityStartBinding.inflate(layoutInflater)
        setContentView(layout.root)

        layout.startLogo.setImageBitmap(
            BitmapFactory.decodeStream(assets.open("AppLogo.png"))
        )

        layout.root.background = TransitionDrawable(
            arrayOf(ColorDrawable(Color.TRANSPARENT), ColorDrawable(Color.parseColor("#C000ABB3")))
        ).apply {
            startTransition(1400)
        }

        FileSystem.loadFileSystem(this)

        Thread {
            Thread.sleep(1700)

            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
        }.start()
    }
}