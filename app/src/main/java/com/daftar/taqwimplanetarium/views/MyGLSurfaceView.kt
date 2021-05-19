package com.daftar.taqwimplanetarium.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.daftar.taqwimplanetarium.objects.Sphere
import kotlin.math.abs
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
    onMassLockedOrUnlocked: ((massID: Int) -> Unit)? = null,
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
        private var scaleDistance = 0f
        private var initialZoom = 0f
        override fun onScale(arg0: ScaleGestureDetector): Boolean {
            lastScaleTime = System.currentTimeMillis()
            if (scaleDistance > 0) {
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

    class LocalGestureDetector(
        private val myGLSurfaceView: MyGLSurfaceView,
        private val renderer: MyGLRenderer
    ) : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent?) {

        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            val r0 =
                ((Math.PI * myGLSurfaceView.zoom / 180.0f) * (1.0f / myGLSurfaceView.height)).toFloat()

            val targetAzimuth = renderer.panAzimuth + distanceX * r0
            var targetAltitude = renderer.panAltitude - distanceY * r0

            targetAltitude = max(
                -(Math.PI / 2).toFloat(),
                min(targetAltitude, (Math.PI / 2).toFloat())
            )

            renderer.panAzimuth = targetAzimuth
            renderer.panAltitude = targetAltitude
            myGLSurfaceView.requestRender()

            return false
        }

        override fun onLongPress(e: MotionEvent?) {

        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val x: Float = e2!!.x
            val y: Float = e2.y
            var dx: Float = x - e1!!.x
            var dy: Float = y - e1.y

            if (abs(velocityX) < 1500)
                dx = 0f
            if (abs(velocityY) < 1500)
                dy = 0f

            if (dx == 0f && dy == 0f)
                return false
            dx *= abs(velocityX) / 5000f
            dy *= abs(velocityY) / 5000f
            val r0 =
                ((Math.PI * myGLSurfaceView.zoom / 180.0f) * (1.0f / myGLSurfaceView.height)).toFloat()

            val startAzimuth = renderer.panAzimuth
            val startAltitude = renderer.panAltitude
            val targetAzimuth = renderer.panAzimuth - dx * r0
            var targetAltitude = renderer.panAltitude + dy * r0

            targetAltitude = max(
                -(Math.PI / 2).toFloat(),
                min(targetAltitude, (Math.PI / 2).toFloat())
            )

            val deltaAz = (targetAzimuth - renderer.panAzimuth) / 100f
            val deltaAt = (targetAltitude - renderer.panAltitude) / 100f

            ValueAnimator.ofFloat(0f, 100f).apply {
                duration = 500
                start()
            }.addUpdateListener {
                renderer.panAzimuth = startAzimuth + deltaAz * it.animatedValue as Float
                renderer.panAltitude = startAltitude + deltaAt * it.animatedValue as Float
                myGLSurfaceView.requestRender()
            }



            return false
        }

    }

    private var scaleGestureDetector = ScaleGestureDetector(
        context,
        ScaleDetectorListener(this, renderer).apply {

        }
    )

    private var gestureDetector =
        GestureDetector(context, LocalGestureDetector(this, renderer).apply { })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(e)
        gestureDetector.onTouchEvent(e)
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