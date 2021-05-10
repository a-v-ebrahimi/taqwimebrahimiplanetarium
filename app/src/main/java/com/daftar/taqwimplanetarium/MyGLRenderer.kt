import android.animation.ValueAnimator
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.widget.ImageView
import com.daftar.taqwimplanetarium.*
import kotlin.math.*
import kotlin.random.Random

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3


class MyGLRenderer(
    private val mainActivity: OpenGLES20Activity, private val surfaceView: MyGLSurfaceView,
    private val massViews: ArrayList<ImageView>, private val labelsView: LabelsView
) : GLSurfaceView.Renderer {
    private val skyRadius = 10f
    private val sunVisibleRadius = skyRadius * Math.PI.toFloat() * 0.5f / 180f

    private val halfVScreenInDegrees: Float = Math.PI.toFloat() / 8
    var lockedMass: Int = -1
        set(value) {
            field = value
            if (lockedMass == -1 || lockedMass > masses.size - 1)
                return
            panAzimuth = masses[lockedMass].azimuth
            panAltitude = masses[lockedMass].altitude + halfVScreenInDegrees
        }

    private var width: Int = 100
    private var height: Int = 100

    var sunAzimuth: Float = 0f
        set(value) {
            field = value
            if (masses.size == 0) return
            masses[0].setAzimuthAltitude(value, sunAltitude)
            if (lockedMass == 0) {
                panAzimuth = sunAzimuth
                panAltitude = sunAltitude
            }
            masses[1].sunRealX = masses[0].sphereX
            masses[1].sunRealY = masses[0].sphereY
            masses[1].sunRealZ = masses[0].sphereZ
            masses[1].recreateSphere()
        }

    var sunAltitude: Float = 0f
        set(value) {
            field = value
            if (masses.size == 0) return
            masses[0].setAzimuthAltitude(sunAzimuth, value)
            if (lockedMass == 0) {
                panAzimuth = sunAzimuth
                panAltitude = sunAltitude
            }
            masses[1].sunRealX = masses[0].sphereX
            masses[1].sunRealY = masses[0].sphereY
            masses[1].sunRealZ = masses[0].sphereZ
            masses[1].recreateSphere()
        }

    private lateinit var mSkyGrid: SkyGrid
    private lateinit var mHorizon: Horizon

    var masses = arrayListOf<Sphere>()

    @Volatile
    var panAzimuth: Float = 0.1f
    var panAltitude: Float = Math.PI.toFloat() / 4f
    var zoom: Float = 45f
        set(value) {
            field = value
            updateViewport(this.width, this.height)
        }




    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color


        mSkyGrid = SkyGrid(mainActivity, skyRadius, labelsView)

        mHorizon = Horizon(skyRadius)

        val mSun = Sphere(
            mainActivity,
            "sun",
            massViews[0], 12,
            0f, 0f, 0.0f, skyRadius,
            sunVisibleRadius,
            floatArrayOf(0.9f, 0.9f, 0.2f, 1f)
        )
        mSun.setAzimuthAltitude(sunAzimuth, sunAltitude)

        val moonAzimuth = sunAzimuth + 0.3f
        val moonAltitude = sunAltitude + 0.3f
        val moonVisibleRadius = sunVisibleRadius
        val mMoon = Sphere(
            mainActivity,
            "moon",
            massViews[1], 12,
            0f, 0f, 0.0f, skyRadius,
            moonVisibleRadius,
            floatArrayOf(0.9f, 0.9f, 0.9f, 1f), isThisMoon = true,
            sunRealX = mSun!!.sphereX,
            sunRealY = mSun!!.sphereY,
            sunRealZ = mSun!!.sphereZ,
        )
        mMoon.setAzimuthAltitude(moonAzimuth, moonAltitude)

        masses.add(mSun!!)
        masses.add(mMoon)

        // add random masses
        for (m in 2 until massViews.size) {
            val massView = massViews[m]
            val massAzimuth = (Random.nextFloat() * Math.PI * 2).toFloat();
            val massAltitude = (Random.nextFloat() * Math.PI - Math.PI / 2).toFloat()
            val massR = sunVisibleRadius * 0.5f
            val mMass = Sphere(
                mainActivity,
                "mass",
                massView, 18,
                0f, 0f, 0.0f, skyRadius,
                massR,
                floatArrayOf(0.9f, 0.9f, 0.9f, 1f)
            )

            mMass.setAzimuthAltitude(massAzimuth, massAltitude)

            masses.add(mMass)
        }

        for (m in 0 until masses.size) {
            val massView = massViews[m]
            val mass = masses[m]
            mainActivity.runOnUiThread {
                massView.setOnClickListener {
                    setCenter(mass.azimuth, mass.altitude)
                }
            }
        }
    }

    private val modelMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        var v = 1f
        when {
            sunAltitude > 0 -> v = 1f
            sunAltitude > -18 * (Math.PI / 180f) -> {
                v = (sunAltitude * (180f / Math.PI).toFloat() + 18f) / 18f
            }
            else -> v = 0f
        }

        GLES20.glClearColor(v * 153f / 255f, v * 204f / 255f, v * 1f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        val localSkyRadius = skyRadius * cos(panAltitude)
        val eyeTargetX = localSkyRadius * cos(panAzimuth)
        val eyeTargetY = localSkyRadius * sin(panAzimuth)
        val eyeTargetZ = skyRadius * (sin(panAltitude))
        Matrix.setLookAtM(viewMatrix, 0,
                0f, 0f, 1f,
                eyeTargetX, eyeTargetY, eyeTargetZ,
                0f, 0.0f, 1.0f)

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
        mHorizon.draw(scratch, sunAltitude)
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

}