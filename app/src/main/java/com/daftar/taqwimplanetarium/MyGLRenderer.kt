import android.animation.ValueAnimator
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.widget.ImageView
import com.daftar.taqwimplanetarium.*
import kotlin.math.*
import kotlin.random.Random

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3


class MyGLRenderer(private val mainActivity: OpenGLES20Activity, private val surfaceView: MyGLSurfaceView, private val sunView: ImageView, private val moonView: ImageView, private val listOfMasses: ArrayList<ImageView>) : GLSurfaceView.Renderer {

    private var width: Int = 100
    private var height: Int = 100

    private lateinit var mSkyGrid: SkyGrid
    private lateinit var mHorizon: Horizon

    var masses = arrayListOf<Sphere>()

    @Volatile
    var panAzimuth: Float = 0.1f
    var panAltitude: Float = -0.55f
    var zoom: Float = 1f
        set(value) {
            field = value
            updateViewport(this.width, this.height)
        }


    private val skyRadius = 10f


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.2f, 0.0f, 0.0f, 1.0f)

        mSkyGrid = SkyGrid(skyRadius)

        mHorizon = Horizon(skyRadius, floatArrayOf(1f, 1f, 1f, 0.2f))

        val sunAzimuth = 0 * (Math.PI / 2f).toFloat()
        val sunAltitude = (Math.PI / 4f).toFloat()
        val sunR = 0.1f
        val mSun = Sphere(
                mainActivity,
                "sun",
                sunView, 12,
                0f, 0f, 0.0f, skyRadius,
                sunAzimuth, sunAltitude, sunR,
                floatArrayOf(0.9f, 0.9f, 0.2f, 1f))
        mainActivity.runOnUiThread {
            sunView.setOnClickListener {
                val startAz = panAzimuth
                val startAl = panAltitude
                val fracAz = (sunAzimuth - panAzimuth) / 100f
                val fracAl = ((-sunAltitude + Math.PI.toFloat() / 8f) - panAltitude) / 100f
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

        val moonAzimuth = sunAzimuth + 0.5f
        val moonAltitude = sunAltitude + 0.0f
        val moonR = sunR
        val mMoon = Sphere(
                mainActivity,
                "moon",
                moonView, 12,
                0f, 0f, 0.0f, skyRadius,
                moonAzimuth, moonAltitude, moonR,
                floatArrayOf(0.9f, 0.9f, 0.9f, 1f), isThisMoon = true,
                sunRealX = mSun.sphereX,
                sunRealY = mSun.sphereY,
                sunRealZ = mSun.sphereZ,
        )

        mainActivity.runOnUiThread {
            moonView.setOnClickListener {
                val startAz = panAzimuth
                val startAl = panAltitude
                val fracAz = (moonAzimuth - panAzimuth) / 100f
                val fracAl = ((-moonAltitude + Math.PI.toFloat() / 8f) - panAltitude) / 100f
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

        masses.add(mSun)
        masses.add(mMoon)

        for (m in 0..5) {
            val massView = listOfMasses[m]
            val massAzimuth = (Random.nextFloat() * Math.PI * 2).toFloat();
            val massAltitude = (Random.nextFloat() * Math.PI - Math.PI / 2).toFloat()
            val massR = sunR
            val mMass = Sphere(
                    mainActivity,
                    "mass",
                    massView, 18,
                    0f, 0f, 0.0f, skyRadius,
                    massAzimuth, massAltitude, massR,
                    floatArrayOf(0.9f, 0.9f, 0.9f, 1f))

            mainActivity.runOnUiThread {
                massView.setOnClickListener {
                    val startAz = panAzimuth
                    val startAl = panAltitude
                    val fracAz = (massAzimuth - panAzimuth) / 100f
                    val fracAl = ((-massAltitude + Math.PI.toFloat() / 8f) - panAltitude) / 100f
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
            masses.add(mMass)
        }
    }

    private val modelMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        val localSkyRadius = skyRadius * cos(-panAltitude)
        val eyeTargetX = localSkyRadius * cos(panAzimuth)
        val eyeTargetY = localSkyRadius * sin(panAzimuth)
        val eyeTargetZ = skyRadius * (sin(-panAltitude))
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

        mSkyGrid.draw(scratch)
        val viewArray = intArrayOf(0, 0, width, height)
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
//        Log.d("tqpt", String.format("width/height : %d/%d", width, height))
        val ratio: Float = zoom * width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method


        val b = -0.1f
        Matrix.frustumM(projectionMatrix, 0,
                -ratio, ratio, zoom * b, zoom * (b + 2f), 5f, 14f)
    }

}