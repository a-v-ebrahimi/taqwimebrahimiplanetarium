package com.daftar.taqwimplanetarium

import MyGLSurfaceView
import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView

class OpenGLES20Activity : Activity() {

    lateinit var openGlSkyView: MyGLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        setContentView(R.layout.activity_main)
        val rootView = findViewById<FrameLayout>(R.id.rootView)
        val sunView = findViewById<ImageView>(R.id.sunImage)
        val moonView = findViewById<ImageView>(R.id.moonImage)
        val listOfMasses = arrayListOf<ImageView>()
        listOfMasses.add(findViewById(R.id.mass1))
        listOfMasses.add(findViewById(R.id.mass2))
        listOfMasses.add(findViewById(R.id.mass3))
        listOfMasses.add(findViewById(R.id.mass4))
        listOfMasses.add(findViewById(R.id.mass5))
        listOfMasses.add(findViewById(R.id.mass6))
        openGlSkyView = MyGLSurfaceView(this, sunView, moonView, listOfMasses, findViewById<LabelsView>(R.id.labelsView))
        rootView.addView(openGlSkyView, 0)

        openGlSkyView.setSunAzimth(0f)
        openGlSkyView.setSunAltitude((Math.PI / 4f).toFloat())
    }
}