import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import android.widget.ImageView
import com.daftar.taqwimplanetarium.*
import kotlin.math.*

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3


class MyGLRenderer(val mainActivity: OpenGLES20Activity, val sunView: ImageView, val moonView: ImageView) : GLSurfaceView.Renderer {

    private var width: Int = 100
    private var height: Int = 100
    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square
    private lateinit var mSkyGrid: SkyGrid
    private lateinit var mSun: Sphere
    private lateinit var mMoon: Sphere

    @Volatile
    var panAzimuth: Float = 0f
    var panAltitude: Float = 0f
    var zoom: Float = 1f
        set(value) {
            field = value
            updateViewport(this.width, this.height)
        }


    val skyRadius = 1f


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.2f, 0.0f, 0.0f, 1.0f)
        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        mSquare = Square()

        mSkyGrid = SkyGrid(skyRadius)

        val sunAzimuth = (Math.PI / 2f).toFloat()
        val sunAltitude = (Math.PI / 4f).toFloat()
        val sunR = 0.1f
        mSun = Sphere(
                mainActivity,
                "sun",
                sunView,
                0f, 0f, 0.0f, skyRadius,
                sunAzimuth, sunAltitude, sunR,
                floatArrayOf(0.9f, 0.9f, 0.2f, 1f))

        val moonAzimuth = (0.5f + Math.PI / 2f).toFloat()
        val moonAltitude = (-0.2f + Math.PI / 4f).toFloat()
        val moonR = 0.5f * sunR
        mMoon = Sphere(
                mainActivity,
                "moon",
                moonView,
                0f, 0f, 0.0f, skyRadius,
                moonAzimuth, moonAltitude, moonR,
                floatArrayOf(0.9f, 0.9f, 0.9f, 1f))
    }

    private val modelMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        val time = SystemClock.uptimeMillis() % 36000
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        val localSkyRadius = skyRadius * cos(-panAltitude)
        val eyeTargetX = localSkyRadius * cos(panAzimuth)
        val eyeTargetY = localSkyRadius * sin(panAzimuth)
        val eyeTargetZ = skyRadius * (sin(-panAltitude))
        Matrix.setLookAtM(viewMatrix, 0,
                eyeTargetX, eyeTargetY, eyeTargetZ,
                0f, 0f, 0f,
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
        var viewArray = intArrayOf(0, 0, width, height)
        mSun.draw(scratch, modelViewMatrix, viewArray, projectionMatrix)
        mMoon.draw(scratch, modelViewMatrix, viewArray, projectionMatrix)


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

    fun updateViewport(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.d("tqpt", String.format("width/height : %d/%d", width, height))
        val ratio: Float = zoom * width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method


        var b = -0.1f
        Matrix.frustumM(projectionMatrix, 0,
                -ratio, ratio, zoom * b, zoom * (b + 2f), 0.8f, 14f)
    }

}