package com.daftar.taqwimplanetarium.views

import android.animation.ValueAnimator
import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.daftar.taqwimplanetarium.objects.Horizon
import com.daftar.taqwimplanetarium.views.LabelsView
import com.daftar.taqwimplanetarium.objects.SkyGrid
import com.daftar.taqwimplanetarium.objects.Sphere
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*
import kotlin.random.Random

const val COORDS_PER_VERTEX = 3


class MyGLRenderer(
    private val mainActivity: Activity,
    private val surfaceView: MyGLSurfaceView,
    private val labelsView: LabelsView,
    private val onSurfaceCreatedListener: (() -> Unit)?
) : GLSurfaceView.Renderer {
    private val skyRadius = 10f
    private val sunVisibleRadius = skyRadius * Math.PI.toFloat() * 0.5f / 180f

    var lockedMass: Int = -1
        set(value) {
            field = value
            if (lockedMass == -1 || lockedMass > masses.size - 1)
                return
            panAzimuth = masses[lockedMass].azimuth
            panAltitude = masses[lockedMass].altitude
        }

    private var width: Int = 100
    private var height: Int = 100


    private lateinit var mSkyGrid: SkyGrid
    private lateinit var mHorizon: Horizon

    private var masses = arrayListOf<Sphere>()

    @Volatile
    var panAzimuth: Float = 0.1f
    var panAltitude: Float = Math.PI.toFloat() / 4f
    var zoom: Float = 45f
        set(value) {
            field = min(45f, max(1f, value))
            updateViewport(this.width, this.height)
        }


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color


        mSkyGrid = SkyGrid(mainActivity, skyRadius, labelsView)

        mHorizon = Horizon(skyRadius)

        val mSun = Sphere(
            MASS_SUN,
            0f, 0f, 0.0f, skyRadius,
            sunVisibleRadius,
            floatArrayOf(0.9f, 0.9f, 0.2f, 1f)
        )

        val moonVisibleRadius = sunVisibleRadius
        val mMoon = Sphere(
            MASS_MOON,
            0f, 0f, 0.0f, skyRadius,
            moonVisibleRadius,
            floatArrayOf(0.9f, 0.9f, 0.9f, 1f), isThisMoon = true,
            sunRealX = mSun.sphereX,
            sunRealY = mSun.sphereY,
            sunRealZ = mSun.sphereZ,
        )

        masses.add(mSun)
        masses.add(mMoon)

        // add random masses
        for (m in 2 until 8) {
            val massAzimuth = (Random.nextFloat() * Math.PI * 2).toFloat()
            val massAltitude = (Random.nextFloat() * Math.PI - Math.PI / 2).toFloat()
            val massR = sunVisibleRadius * 0.5f
            val mMass = Sphere(
                m,
                0f, 0f, 0.0f, skyRadius,
                massR,
                floatArrayOf(0.9f, 0.9f, 0.9f, 1f)
            )

            mMass.setAzimuthAltitude(massAzimuth, massAltitude)

            masses.add(mMass)
        }

        onSurfaceCreatedListener?.let { it() }
    }

    private val modelMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        val skyLightness: Float = when {
            masses[MASS_SUN].altitude > 0 -> 1f
            masses[MASS_SUN].altitude > -18 * (Math.PI / 180f) -> {
                (masses[MASS_SUN].altitude * (180f / Math.PI).toFloat() + 18f) / 18f
            }
            else -> 0f
        }
        labelsView.skyLightness = skyLightness

        GLES20.glClearColor(
            skyLightness * 153f / 255f,
            skyLightness * 204f / 255f,
            skyLightness * 1f,
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        val localSkyRadius = skyRadius * cos(panAltitude)
        val eyeTargetX = localSkyRadius * cos(-panAzimuth)
        val eyeTargetY = localSkyRadius * sin(-panAzimuth)
        val eyeTargetZ = skyRadius * (sin(panAltitude))
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 1f,
            eyeTargetX, eyeTargetY, eyeTargetZ,
            0f, 0.0f, 1.0f
        )

        // Create a rotation transformation for the triangle
        val angle = 0f//panAzimuth+ 0.010f * time.toInt()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setRotateM(modelMatrix, 0, angle, 0f, 0f, 1f)

        Matrix.setIdentityM(modelViewMatrix, 0)
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)


        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        val scratch = FloatArray(16)
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, modelMatrix, 0)

        // Draw triangle
//        mTriangle.draw(scratch)

        val viewArray = intArrayOf(0, 0, width, height)
        mSkyGrid.draw(scratch, modelViewMatrix, viewArray, projectionMatrix)
        mHorizon.draw(scratch)
        for (m in masses) {
            m.draw(scratch, modelViewMatrix, viewArray, projectionMatrix)
        }

    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        updateViewport(width, height)
    }

    private fun updateViewport(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        Matrix.perspectiveM(
            projectionMatrix, 0,
            zoom, width.toFloat() / height.toFloat(), 2f, 14f
        )
    }

    fun setCenter(lookAtAzimuth: Float, lookAtAltitude: Float) {
        val startAz = panAzimuth
        val startAl = panAltitude
        val fracAz = (lookAtAzimuth - panAzimuth) / 100f
        val fracAl = ((-lookAtAltitude) - panAltitude) / 100f

        ValueAnimator.ofFloat(0f, 100f).apply {
            duration = 1000
            start()
        }.addUpdateListener {
            panAzimuth = startAz + (it.animatedValue as Float) * fracAz
            panAltitude = startAl + (it.animatedValue as Float) * fracAl
            surfaceView.requestRender()
        }
    }

    fun setMassAzimuthAltitude(massId: Int, azimuth: Float, altitude: Float) {
        if (massId < 0 || massId > masses.size - 1)
            return
        masses[massId].setAzimuthAltitude(azimuth, altitude)
        surfaceView.requestRender()
        if (massId == lockedMass) {
            panAzimuth = masses[lockedMass].azimuth
            panAltitude = masses[lockedMass].altitude
        }
        if (massId == MASS_SUN) {
            // sun moved? then recalc moon shadow
            masses[MASS_MOON].sunRealX = masses[MASS_SUN].sphereX
            masses[MASS_MOON].sunRealY = masses[MASS_SUN].sphereY
            masses[MASS_MOON].sunRealZ = masses[MASS_SUN].sphereZ
            masses[MASS_MOON].recreateSphere()
        }
    }

    fun getMass(massId: Int): Sphere? {
        if (massId < 0 || massId > masses.size - 1)
            return null
        return masses[massId]
    }

    fun findMassAt2DXY(x: Float, y: Float): Int {
        val yH = height - y
        for (m in 0 until masses.size) {
            val mass = masses[m]
            if (abs(x - mass.last2Dx) < 10 && abs(yH - mass.last2Dy) < 10)
                return m
        }
        return -1
    }

}