package com.daftar.taqwimplanetarium.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.daftar.taqwimplanetarium.objects.Sphere
import kotlin.math.max
import kotlin.math.min


const val MASS_SUN = 0
const val MASS_MOON = 1
var busyScaling = false
var lastScaleTime = 0L

@SuppressLint("ViewConstructor")
class MyGLSurfaceView(
    mainActivity: Activity,
    labelsView: LabelsView,
    private var onMassLockedOrUnlocked: ((massID: Int) -> Unit)? = null,
    private var onMassClicked: ((massID: Int) -> Unit)? = null
) : GLSurfaceView(mainActivity) {


    var onSurfaceCreated: (() -> Unit)? = null

    var zoom: Float
        get() {
            return renderer.zoom
        }
        set(value) {
            val target = min(45f, max(1f, value))
            ValueAnimator.ofFloat(zoom, target).apply {
                duration = 500
                start()
            }.addUpdateListener {
                renderer.zoom = it.animatedValue as Float
                requestRender()
            }

        }


    private val renderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(
            mainActivity,
            this,
            labelsView, onMassLockedOrUnlocked = onMassLockedOrUnlocked
        ) {
            onSurfaceCreated?.let { it() }
        }

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY

    }

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    class ScaleDetectorListener(
        private val myGLSurfaceView: MyGLSurfaceView,
        private val renderer: MyGLRenderer
    ) :
        OnScaleGestureListener {
        private var scaleFocusX = 0f
        private var scaleFocusY = 0f
        var scaleDistance = 0f
        var initialZoom = 0f
        override fun onScale(arg0: ScaleGestureDetector): Boolean {
            lastScaleTime = System.currentTimeMillis()
            if (scaleDistance > 0) {
                Log.d("tqpt", "scale2 : ${arg0.currentSpan / scaleDistance}")
                renderer.zoom = initialZoom / (arg0.currentSpan / scaleDistance)
                myGLSurfaceView.requestRender()
            }
            return true
        }

        override fun onScaleBegin(arg0: ScaleGestureDetector): Boolean {
            busyScaling = true
            myGLSurfaceView.invalidate()
            scaleFocusX = arg0.focusX
            scaleFocusY = arg0.focusY
            scaleDistance = arg0.currentSpan
            initialZoom = renderer.zoom
            return true
        }

        override fun onScaleEnd(arg0: ScaleGestureDetector) {
            busyScaling = false
            scaleFocusX = 0f
            scaleFocusY = 0f
        }
    }

    private var mDetector = ScaleGestureDetector(
        context,
        ScaleDetectorListener(this, renderer).apply {

        }
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        mDetector.onTouchEvent(e)
        if (busyScaling || System.currentTimeMillis() - lastScaleTime < 500)
            return true
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Float = e.x
        val y: Float = e.y

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                val selected = renderer.findMassAt2DXY(x, y)
                onMassClicked?.let { it(selected) }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx: Float = x - previousX
                val dy: Float = y - previousY
                if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10)
                    setLockMass(-1)



                if (e.pointerCount == 1) {
                    val r0 = ((Math.PI * zoom / 180.0f) * (1.0f / height)).toFloat()

                    renderer.panAzimuth -= dx * r0
                    renderer.panAltitude += dy * r0

                    renderer.panAltitude = max(
                        -(Math.PI / 2).toFloat(),
                        min(renderer.panAltitude, (Math.PI / 2).toFloat())
                    )
                }
                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun setCenter(lookAtAzimuth: Float, lookAtAltitude: Float) {
        renderer.setCenter(lookAtAzimuth, lookAtAltitude)
    }

    fun setLockMass(mass: Int) {
        if (renderer.lockedMass == mass)
            return
        renderer.lockedMass = mass
        requestRender()
    }

    fun getLockedMass(): Int {
        return renderer.lockedMass
    }

    fun setMassAzimuthAltitude(massId: Int, azimuth: Float, altitude: Float) {
        renderer.setMassAzimuthAltitude(massId, azimuth, altitude)
    }

    fun getMass(massId: Int): Sphere? {
        return renderer.getMass(massId)
    }

}