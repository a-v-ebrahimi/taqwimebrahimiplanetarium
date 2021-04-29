package com.daftar.taqwimplanetarium

import COORDS_PER_VERTEX
import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class SkyGrid(skyRadius: Float) {
    private var vertexCount: Int = 0
    private var vertexBuffer: FloatBuffer
    val stp = 10

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

    fun loadShader(type: Int, shaderCode: String): Int {

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

        var triangleCoords: MutableList<Float> = mutableListOf();

        for (b in 0..90 step stp)
        for (a in 0..360 step stp) {
            val alphaV = Math.PI * b / 180.0f
            val alphaVN = Math.PI * (b + stp) / 180.0f

            val r = skyRadius * cos(alphaV).toFloat()
            val rN = skyRadius * cos(alphaVN).toFloat()

            val z = skyRadius * sin(alphaV).toFloat()
            val zN = skyRadius * sin(alphaVN).toFloat()

            val alpha = Math.PI * a / 180.0
            val x = r * cos(alpha).toFloat()
            val y = r * sin(alpha).toFloat()
            val p1 = arrayOf(x, y, z)

            val alpha2 = Math.PI * (a + stp) / 180.0
            val x2 = r * cos(alpha2).toFloat()
            val y2 = r * sin(alpha2).toFloat()
            val p2 = arrayOf(x2, y2, z)

            val xn=rN*cos(alpha).toFloat()
            val yn=rN*sin(alpha).toFloat()
            val pNextRing=arrayOf(xn,yn,zN)

            // horizontal
            triangleCoords.addAll(p1)
            triangleCoords.addAll(p2)

            // vertical
            triangleCoords.addAll(p1)
            triangleCoords.addAll(pNextRing)
        }

        vertexCount = triangleCoords.size / COORDS_PER_VERTEX;
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

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.13671875f, 0.76953125f, 0.22265625f, 1.0f)


    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)


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

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount)


            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}