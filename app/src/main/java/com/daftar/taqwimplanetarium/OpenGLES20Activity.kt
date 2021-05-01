package com.daftar.taqwimplanetarium

import MyGLSurfaceView
import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView

class OpenGLES20Activity : Activity() {

    lateinit var gLView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        setContentView(R.layout.activity_main)
        val rootView = findViewById<FrameLayout>(R.id.rootView)
        val sunView = findViewById<ImageView>(R.id.sunImage)
        val moonView = findViewById<ImageView>(R.id.moonImage)
        gLView = MyGLSurfaceView(this, sunView, moonView)
        rootView.addView(gLView, 0)
    }
}