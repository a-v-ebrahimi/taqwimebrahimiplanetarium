package com.daftar.taqwimplanetarium

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.daftar.taqwimplanetarium.views.MASS_SUN
import com.daftar.taqwimplanetarium.views.TaqwimPlanetariumView

@Suppress("UNUSED_PARAMETER")
class OpenGLES20Activity : Activity() {

    private lateinit var taqwimPlanetariumView: TaqwimPlanetariumView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        taqwimPlanetariumView = findViewById<TaqwimPlanetariumView>(R.id.taqwimPlanetariumView)
    }

    fun setCenterClicked(view: View) {
        taqwimPlanetariumView.openGlSkyView.setCenter(0f, 0f)
    }

    fun setZoomClicked(view: View) {
        taqwimPlanetariumView.openGlSkyView.zoom = 30f
    }

    @SuppressLint("SetTextI18n")
    fun setLockMass(view: View) {
        if (taqwimPlanetariumView.openGlSkyView.getLockedMass() == -1) {
            taqwimPlanetariumView.openGlSkyView.setLockMass(MASS_SUN)
            (findViewById<Button>(R.id.btnSetLockMass)).text = "Locked to Sun"
        } else {
            taqwimPlanetariumView.openGlSkyView.setLockMass(-1)
            (findViewById<Button>(R.id.btnSetLockMass)).text = "Not Locked"
        }
    }

    fun onZoomOut(view: View) {
        taqwimPlanetariumView.openGlSkyView.zoom = taqwimPlanetariumView.openGlSkyView.zoom + 10
    }

    fun onZoomIn(view: View) {
        taqwimPlanetariumView.openGlSkyView.zoom = taqwimPlanetariumView.openGlSkyView.zoom - 10
    }
}