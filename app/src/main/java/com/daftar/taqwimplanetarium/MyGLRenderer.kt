import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.daftar.taqwimplanetarium.SkyGrid
import com.daftar.taqwimplanetarium.Square
import com.daftar.taqwimplanetarium.Triangle
import kotlin.math.cos
import kotlin.math.sin

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3


class MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square
    private lateinit var mSkyGrid:SkyGrid

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.2f, 0.0f, 0.0f, 1.0f)
        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        mSquare = Square()

        mSkyGrid = SkyGrid()
    }

    private val rotationMatrix = FloatArray(16)
    private val translationMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        val time = SystemClock.uptimeMillis() % 12400
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0,
                1f,1f,0f,
                0f, 0f, 0f,
                0f, 0.0f, 1.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        // Create a rotation transformation for the triangle
        val angle = 0.010f * time.toInt()
        Matrix.setIdentityM(rotationMatrix,0)
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, angle)

        Matrix.setIdentityM(translationMatrix,0)
        Matrix.translateM(translationMatrix,0,0f,0f,-1f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        val scratch = FloatArray(16)
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw triangle
//        mTriangle.draw(scratch)

        mSkyGrid.draw(scratch)
//        mSkyGrid.draw(vPMatrix)
    }

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        var b=-0.5f
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, b, b+2f, 1f, 5f)
    }
}