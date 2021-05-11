package com.daftar.taqwimplanetarium

import MASS_MOON
import MASS_SUN
import MyGLSurfaceView
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*
import kotlin.concurrent.schedule

class OpenGLES20Activity : Activity() {

    lateinit var openGlSkyView: MyGLSurfaceView

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

        openGlSkyView.onSurfaceCreated = {
            openGlSkyView.setMassAzimuthAltitude(MASS_MOON, Math.PI.toFloat() / 3f, 0f)
        }

//        Timer("SettingUp", false).schedule(50, 50) {
//            runOnUiThread {
//                sunAltitude -= 0.003f
//                sunAzimuth += 0.003f
//                openGlSkyView.setMassAzimuthAltitude(MASS_SUN, sunAzimuth, sunAltitude)
//            }
//
//        }
    }

    fun setCenterClicked(view: View) {
        openGlSkyView.setCenter(0f, 0f)
    }

    fun setZoomClicked(view: View) {
        openGlSkyView.zoom = 30f
    }

    fun setLockMass(view: View) {
        if (openGlSkyView.getLockedMass() == -1) {
            openGlSkyView.setLockMass(MASS_SUN)
            (findViewById<Button>(R.id.btnSetLockMass)).text = "Locked to Sun"
        } else {
            openGlSkyView.setLockMass(-1)
            (findViewById<Button>(R.id.btnSetLockMass)).text = "Not Locked"
        }
    }

    fun onZoomOut(view: View) {
        openGlSkyView.zoom = openGlSkyView.zoom + 10
    }

    fun onZoomIn(view: View) {
        openGlSkyView.zoom = openGlSkyView.zoom - 10
    }
}