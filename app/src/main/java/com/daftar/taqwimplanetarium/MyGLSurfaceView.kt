import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.ImageView
import com.daftar.taqwimplanetarium.LabelsView
import com.daftar.taqwimplanetarium.OpenGLES20Activity
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min


private const val TOUCH_SCALE_FACTOR: Float = 0.0006f

class MyGLSurfaceView(mainActivity: OpenGLES20Activity, sunView: ImageView, moonView: ImageView,
                      listOfMasses: ArrayList<ImageView>, labelsView: LabelsView) : GLSurfaceView(mainActivity) {


    private val renderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(mainActivity,
                this,
                sunView, moonView, listOfMasses, labelsView)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

    }

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    class ScaleDetectorListener(val myGLSurfaceView: MyGLSurfaceView, val renderer: MyGLRenderer) : OnScaleGestureListener {
        private var sizeCoef: Float = 1f
        var scaleFocusX = 0f
        var scaleFocusY = 0f
        override fun onScale(arg0: ScaleGestureDetector): Boolean {
            val scale: Float = arg0.scaleFactor * sizeCoef
            sizeCoef = scale
            renderer.zoom = 1 / scale;
            myGLSurfaceView.requestRender()
            return true
        }

        override fun onScaleBegin(arg0: ScaleGestureDetector): Boolean {
            myGLSurfaceView.invalidate()
            scaleFocusX = arg0.focusX
            scaleFocusY = arg0.focusY
            return true
        }

        override fun onScaleEnd(arg0: ScaleGestureDetector) {
            scaleFocusX = 0f
            scaleFocusY = 0f
        }
    }

    var mDetector = ScaleGestureDetector(getContext(),
            ScaleDetectorListener(this, renderer))

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (mDetector.onTouchEvent(e)) {
//            return true;
        }
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Float = e.x
        val y: Float = e.y

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                val buffer: ByteBuffer = ByteBuffer.allocate(4) // 4 = (1 width) * (1 height) * (4 as per RGBA)

                GLES20.glReadPixels(x.toInt(), y.toInt(), 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
            }
            MotionEvent.ACTION_MOVE -> {

                var dx: Float = x - previousX
                var dy: Float = y - previousY


                if (e.pointerCount == 1) {
//                    // reverse direction of rotation above the mid-line
//                    if (y > height / 2) {
//                        dx *= -1
//                    }
//
//                    // reverse direction of rotation to left of the mid-line
//                    if (x < width / 2) {
//                        dy *= -1
//                    }

                    renderer.panAzimuth += dx * TOUCH_SCALE_FACTOR * renderer.zoom
                    renderer.panAltitude -= dy * TOUCH_SCALE_FACTOR * renderer.zoom

                    renderer.panAltitude = max(-(Math.PI / 2).toFloat(), min(renderer.panAltitude, (Math.PI / 2).toFloat()))
                } else if (e.pointerCount == 2) {
                }
                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun setSunAzimth(az: Float) {
        renderer.sunAzimuth = az
        invalidate()
    }

    fun setSunAltitude(al: Float) {
        renderer.sunAltitude = al
        invalidate()
    }

}