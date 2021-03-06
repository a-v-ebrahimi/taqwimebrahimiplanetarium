package com.daftar.taqwimplanetarium.objects

import android.opengl.GLES20
import android.opengl.GLU
import android.util.Log
import com.daftar.taqwimplanetarium.views.COORDS_PER_VERTEX
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Sphere(
    val massID: Int,
    private val spaceX: Float, private val spaceY: Float, private val spaceZ: Float,
    private val spaceR: Float,
    private val sphereR: Float,
    private val sphereColor: FloatArray,
    private val isThisMoon: Boolean = false,
    var sunRealX: Float = 0f,
    var sunRealY: Float = 0f,
    var sunRealZ: Float = 0f,
) {
    private var flagRecreationAfterDraw: Boolean = false
    private var vertexCount: Int = 0
    private var vertexBuffer: FloatBuffer
    var triangleCoords: MutableList<Float> = mutableListOf()
    var triangleColors: MutableList<Float> = mutableListOf()

    var azimuth: Float = 0f
    var altitude: Float = 0f


    var sphereX = 0f
    var sphereY = 0f
    var sphereZ = 0f
    private val stp = 9

    var last2Dx = -1f
    var last2Dy = -1f

    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
        vertexBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(triangleCoords.toFloatArray())
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }
        createSphere()
    }

    private fun createSphere() {
        triangleCoords.clear()
        triangleColors.clear()

        val localR = spaceR * cos(altitude)
        sphereX = spaceX + localR * cos(-azimuth)
        sphereY = spaceY + localR * sin(-azimuth)
        sphereZ = spaceZ + spaceR * sin(altitude)

        val distanceToSun = sqrt(
            ((sunRealX - sphereX) * (sunRealX - sphereX) +
                    (sunRealY - sphereY) * (sunRealY - sphereY) +
                    (sunRealZ - sphereZ) * (sunRealZ - sphereZ))
        )
        val minDistanceToSun = abs(distanceToSun - sphereR)


        for (b in -90..90 step stp)
            for (a in 0 until 360 step stp) {
                val alphaV = Math.PI * b / 180.0f
                val alphaVN = Math.PI * (b + stp) / 180.0f

                val r = sphereR * cos(alphaV).toFloat()
                val rN = sphereR * cos(alphaVN).toFloat()

                val z = sphereZ + sphereR * sin(alphaV).toFloat()
                val zN = sphereZ + sphereR * sin(alphaVN).toFloat()

                val alpha = Math.PI * a / 180.0
                val x = sphereX + r * cos(alpha).toFloat()
                val y = sphereY + r * sin(alpha).toFloat()
                val p1 = arrayOf(x, y, z)

                val alpha2 = Math.PI * (a + stp) / 180.0
                val x2 = sphereX + r * cos(alpha2).toFloat()
                val y2 = sphereY + r * sin(alpha2).toFloat()
                val p2 = arrayOf(x2, y2, z)

                val xn = sphereX + rN * cos(alpha).toFloat()
                val yn = sphereY + rN * sin(alpha).toFloat()
                val p1NextRing = arrayOf(xn, yn, zN)

                val xn2 = sphereX + rN * cos(alpha2).toFloat()
                val yn2 = sphereY + rN * sin(alpha2).toFloat()
                val p2NextRing = arrayOf(xn2, yn2, zN)


                // first face
                triangleCoords.addAll(p1)
                triangleCoords.addAll(p2)
                triangleCoords.addAll(p1NextRing)

                if (isThisMoon) {
                    val ds = 1 - (distanceFromPointToSun(p1) - minDistanceToSun) / (1.1f * sphereR)
                    if (b > 80) {
                        if (a > 1500) {
                            triangleColors.add(0f)
                        }
                    }
                    triangleColors.add(ds)
                } else
                    triangleColors.add(sphereColor[0])


                // second face
                triangleCoords.addAll(p2)
                triangleCoords.addAll(p1NextRing)
                triangleCoords.addAll(p2NextRing)

                if (isThisMoon) {
                    val ds2 = 1 - (distanceFromPointToSun(p2) - minDistanceToSun) / (1.1f * sphereR)
                    triangleColors.add(ds2)
                } else
                    triangleColors.add(sphereColor[0])
            }

        vertexCount = triangleCoords.size / COORDS_PER_VERTEX
        vertexBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(triangleCoords.toFloatArray())
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }

    }

    private fun distanceFromPointToSun(p1: Array<Float>): Float {
        return sqrt(
            ((sunRealX - p1[0]) * (sunRealX - p1[0]) +
                    (sunRealY - p1[1]) * (sunRealY - p1[1]) +
                    (sunRealZ - p1[2]) * (sunRealZ - p1[2]))
        )
    }

    // Set color with red, green, blue and alpha (opacity) values

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(
        mvpMatrix: FloatArray,
        modelViewMatrix: FloatArray,
        view: IntArray,
        projectionMatrix: FloatArray
    ) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        if (vertexBuffer == null)
            return
        // get handle to shape's
        // transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        val output = floatArrayOf(0f, 0f, 0f)
        GLU.gluProject(
            sphereX, sphereY, sphereZ, modelViewMatrix, 0, projectionMatrix, 0, view, 0,
            output, 0
        )
        last2Dx = output[0]
        last2Dy = output[1]

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            if (isThisMoon) {
                for (v in 0 until (vertexCount / 3)) {
                    mColorHandle =
                        GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                            // Set color for drawing the triangle
                            GLES20.glUniform4fv(
                                colorHandle, 1,
                                floatArrayOf(
                                    triangleColors[v],
                                    triangleColors[v],
                                    triangleColors[v],
                                    1f
                                ), 0
                            )
                        }

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, v * 3, 3)
                }
            } else {
                // get handle to fragment shader's vColor member
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                    // Set color for drawing the triangle
                    GLES20.glUniform4fv(colorHandle, 1, sphereColor, 0)
                }

                // Draw the triangle
                //           if (imageOn2dScreen == null)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
            }

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
        if (flagRecreationAfterDraw) {
            flagRecreationAfterDraw = false
            createSphere()
        }
    }

    fun setAzimuthAltitude(azimuth: Float, altitude: Float) {
        this.azimuth = azimuth
        this.altitude = altitude
        val localR = spaceR * cos(altitude)
        sphereX = spaceX + localR * cos(-azimuth)
        sphereY = spaceY + localR * sin(-azimuth)
        sphereZ = spaceZ + spaceR * sin(altitude)
        flagRecreationAfterDraw = true
    }

    fun recreateSphere() {
        flagRecreationAfterDraw = true
    }


}