@file:Suppress("NestedLambdaShadowedImplicitParameter")

package com.daftar.taqwimplanetarium.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.daftar.taqwimplanetarium.R

@SuppressLint("SetTextI18n")
class TaqwimPlanetariumView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var openGlSkyView: MyGLSurfaceView


    init {
        // Init View
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.planetarium_view, this, true)

        val infoPanel = findViewById<TextView>(R.id.infoPanel)

        openGlSkyView = MyGLSurfaceView(
            context as Activity,
            findViewById(R.id.labelsView),
            onMassLockedOrUnlocked = {
                Log.d("tqpt", "Mass Locked : $it")
                if (it == -1) {
                    infoPanel.visibility = GONE
                } else {
                    infoPanel.visibility = VISIBLE
                }
            }
        ) {
            val mass = openGlSkyView.getMass(it)
            mass?.let {
                infoPanel.text =
                    "selected mass : ${it.massID}, azimuth : ${it.azimuth}, altitude ; ${it.altitude}"
                openGlSkyView.setLockMass(it.massID)
            }

        }
        this.addView(openGlSkyView, 0)

        openGlSkyView.onSurfaceCreated = {
            openGlSkyView.setMassAzimuthAltitude(MASS_MOON, 0f, 0f)
            openGlSkyView.setMassAzimuthAltitude(MASS_SUN, Math.PI.toFloat() / 1f, 0f)
        }

    }


}