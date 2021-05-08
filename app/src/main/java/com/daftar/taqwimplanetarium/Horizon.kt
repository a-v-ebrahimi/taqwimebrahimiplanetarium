package com.daftar.taqwimplanetarium

import COORDS_PER_VERTEX
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_SRC_ALPHA
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*


class Horizon(
        private val skyRadius: Float

) {
    private var vertexCount: Int = 0
    private var vertexBuffer: FloatBuffer
    var triangleCoords: MutableList<Float> = mutableListOf()
    var triangleColors: MutableList<Float> = mutableListOf()
    private val horizonColor = floatArrayOf(0f, 0f, 0f, 0.2f)

    private val stp = 9

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




        for (a in 0 until 360 step stp) {

            val alpha = Math.PI * a / 180.0
            val x = skyRadius * cos(alpha).toFloat()
            val y = skyRadius * sin(alpha).toFloat()
            val p1 = arrayOf(x, y, 0f)

            val alpha2 = Math.PI * (a + stp) / 180.0
            val x2 = skyRadius * cos(alpha2).toFloat()
            val y2 = skyRadius * sin(alpha2).toFloat()
            val p2 = arrayOf(x2, y2, 0f)

            val alpha3 = (alpha + alpha2) / 2
            val x3 = 0.01f * cos(alpha3).toFloat()
            val y3 = 0.01f * sin(alpha3).toFloat()
            val p3 = arrayOf(x3, y3, -10f)


            // first face
            triangleCoords.addAll(p1)
            triangleCoords.addAll(p2)
            triangleCoords.addAll(p3)

            triangleColors.add(1f)

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

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray, sunAltitude: Float) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        GLES20.glEnable(GL_BLEND);
        GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

                GLES20.glUniform4fv(colorHandle, 1, horizonColor, 0)
            }

            // Draw the triangle
            //           if (imageOn2dScreen == null)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}