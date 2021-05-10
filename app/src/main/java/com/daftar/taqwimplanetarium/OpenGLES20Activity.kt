package com.daftar.taqwimplanetarium

import MyGLSurfaceView
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class OpenGLES20Activity : Activity() {

    lateinit var openGlSkyView: MyGLSurfaceView
    var sunA: Float = (Math.PI / 16).toFloat()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        setContentView(R.layout.activity_main)
        val rootView = findViewById<FrameLayout>(R.id.rootView)
        val massViews = arrayListOf<ImageView>()
        massViews.add(findViewById(R.id.sunImage))
        massViews.add(findViewById(R.id.moonImage))
        massViews.add(findViewById(R.id.mass1))
        massViews.add(findViewById(R.id.mass2))
        massViews.add(findViewById(R.id.mass3))
        massViews.add(findViewById(R.id.mass4))
        massViews.add(findViewById(R.id.mass5))
        massViews.add(findViewById(R.id.mass6))
        openGlSkyView = MyGLSurfaceView(
            this,
            massViews, findViewById<LabelsView>(R.id.labelsView)
        )
        rootView.addView(openGlSkyView, 0)

        openGlSkyView.sunAzimuth(0f)
        openGlSkyView.setSunAltitude(sunA)

//        Timer("SettingUp", false).schedule(50,50) {
//            runOnUiThread {
//                sunA -= 0.001f
//                openGlSkyView.setSunAltitude(sunA)
//            }
//
//        }
    }

    fun setCenterClicked(view: View) {
        openGlSkyView.setCenter((Math.PI / 4f).toFloat(), (Math.PI / 4f).toFloat())
    }

    fun setZoomClicked(view: View) {
        openGlSkyView.SetZoomAngle((Math.PI / 8f).toFloat())
    }

    fun setLockMass(view: View) {
        openGlSkyView.setLockMass(0)
    }
}