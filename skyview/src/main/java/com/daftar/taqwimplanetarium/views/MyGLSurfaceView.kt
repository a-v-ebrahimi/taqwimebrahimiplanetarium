package com.daftar.taqwimplanetarium.views

import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.daftar.taqwimplanetarium.objects.Sphere
import com.daftar.taqwimplanetarium.touch.DragRotateZoomGestureDetector
import com.daftar.taqwimplanetarium.touch.GestureInterpreter
import com.daftar.taqwimplanetarium.touch.MapMover
import kotlin.math.max
import kotlin.math.min


// https://github.com/sky-map-team/stardroid/blob/be8f23cc7af6966cb9eb29a91e8c669ba874a528/app/src/main/java/com/google/android/stardroid/touch/Flinger.java
// https://github.com/sky-map-team/stardroid/blob/be8f23cc7af6966cb9eb29a91e8c669ba874a528/app/src/main/java/com/google/android/stardroid/touch/GestureInterpreter.java

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


    private var gestureDetector: GestureDetector
    private var dragZoomRotateDetector: DragRotateZoomGestureDetector
    var onSurfaceCreated: (() -> Unit)? = null

    var zoom: Float
        get() {
            return renderer.zoom
        }
        set(value) {
            val target = min(45f, max(1f, value))
            renderer.zoom = target
            requestRender()
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

        val mapMover = MapMover(renderer, this, context)

        gestureDetector = GestureDetector(
            context, GestureInterpreter(
                mapMover
            )
        )
        dragZoomRotateDetector = DragRotateZoomGestureDetector(mapMover)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var eventAbsorbed = false
        if (gestureDetector.onTouchEvent(event)) {
            eventAbsorbed = true
            if (event.action == MotionEvent.ACTION_DOWN) {
                val selected = renderer.findMassAt2DXY(event.x, event.y)
                onMassClicked?.let { it(selected) }
            }
        }
        if (dragZoomRotateDetector.onTouchEvent(event)) {
            eventAbsorbed = true
        }
        return eventAbsorbed
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