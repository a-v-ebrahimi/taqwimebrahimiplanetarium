package com.daftar.taqwimplanetarium

import MyGLSurfaceView
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*
import kotlin.concurrent.schedule

class OpenGLES20Activity : Activity() {

    lateinit var openGlSkyView: MyGLSurfaceView
    var sunAzimuth: Float = (Math.PI / 16).toFloat()
    var sunAltitude: Float = (Math.PI / 3).toFloat()

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

        openGlSkyView.sunAzimuth(sunAzimuth)
        openGlSkyView.setSunAltitude(sunAltitude)

        Timer("SettingUp", false).schedule(50, 50) {
            runOnUiThread {
                sunAltitude -= 0.001f
                sunAzimuth += 0.001f
                openGlSkyView.setSunAzimuthAltitude(sunAzimuth, sunAltitude)
            }

        }
    }

    fun setCenterClicked(view: View) {
        openGlSkyView.setCenter(0f, 0f)
    }

    fun setZoomClicked(view: View) {
        openGlSkyView.SetZoomAngle(30f)
    }

    fun setLockMass(view: View) {
        openGlSkyView.setLockMass(0)
    }
}