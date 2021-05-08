package com.daftar.taqwimplanetarium

import COORDS_PER_VERTEX
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class SkyGrid(skyRadius: Float) {
    private var vertexCount: Int = 0
    private var vertexBuffer: FloatBuffer

    val stpV = 10
    val stpH = 10
    val stpL = 4

    var lineCoords: MutableList<Float> = mutableListOf();
    var lineColors: MutableList<Int> = mutableListOf();

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

        // horizontal lines

        for (b in -90..90 step stpV)
            for (a in 0..360 step stpL) {
                val alphaV = Math.PI * b / 180.0f
                val alphaVN = Math.PI * (b + stpV) / 180.0f

                val r = skyRadius * cos(alphaV).toFloat()
                val rN = skyRadius * cos(alphaVN).toFloat()

                val z = skyRadius * sin(alphaV).toFloat()
                val zN = skyRadius * sin(alphaVN).toFloat()

                val alpha = Math.PI * a / 180.0
                val x = r * cos(alpha).toFloat()
                val y = r * sin(alpha).toFloat()
                val p1 = arrayOf(x, y, z)

                val alpha2 = Math.PI * (a + stpL) / 180.0
                val x2 = r * cos(alpha2).toFloat()
                val y2 = r * sin(alpha2).toFloat()
                val p2 = arrayOf(x2, y2, z)

                val xn = rN * cos(alpha).toFloat()
                val yn = rN * sin(alpha).toFloat()
                val pNextRing = arrayOf(xn, yn, zN)

                // horizontal
                lineCoords.addAll(p1)
                lineCoords.addAll(p2)
                lineColors.add(1)

            }

        for (b in -90..90 step stpL)
            for (a in 0..360 step stpH) {
                val alphaV = Math.PI * b / 180.0f
                val alphaVN = Math.PI * (b + stpL) / 180.0f

                val r = skyRadius * cos(alphaV).toFloat()
                val rN = skyRadius * cos(alphaVN).toFloat()

                val z = skyRadius * sin(alphaV).toFloat()
                val zN = skyRadius * sin(alphaVN).toFloat()

                val alpha = Math.PI * a / 180.0
                val x = r * cos(alpha).toFloat()
                val y = r * sin(alpha).toFloat()
                val p1 = arrayOf(x, y, z)

                val alpha2 = Math.PI * (a + stpH) / 180.0
                val x2 = r * cos(alpha2).toFloat()
                val y2 = r * sin(alpha2).toFloat()
                val p2 = arrayOf(x2, y2, z)

                val xn = rN * cos(alpha).toFloat()
                val yn = rN * sin(alpha).toFloat()
                val pNextRing = arrayOf(xn, yn, zN)

                // horizontal
                lineCoords.addAll(p1)
                lineCoords.addAll(pNextRing)
                lineColors.add(2)

            }

        vertexCount = lineCoords.size / COORDS_PER_VERTEX;
        vertexBuffer =
                // (number of coordinate values * 4 bytes per float)
                ByteBuffer.allocateDirect(lineCoords.size * 4).run {
                    // use the device hardware's native byte order
                    order(ByteOrder.nativeOrder())

                    // create a floating point buffer from the ByteBuffer
                    asFloatBuffer().apply {
                        // add the coordinates to the FloatBuffer
                        put(lineCoords.toFloatArray())
                        // set the buffer to read the first coordinate
                        position(0)
                    }
                }
    }

    // Set color with red, green, blue and alpha (opacity) values
    val colorHLines = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)
    val colorVLines = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)


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



            GLES20.glLineWidth(3f)
            for (v in 0 until (vertexCount / 2)) {
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                    if (lineColors[v] == 1)
                        GLES20.glUniform4fv(colorHandle, 1,
                                floatArrayOf(0.8f, 0.2f, 0.2f, 1f), 0)
                    else
                        GLES20.glUniform4fv(colorHandle, 1,
                                floatArrayOf(0.2f, 0.6f, 0.2f, 1f), 0)
                }

                GLES20.glDrawArrays(GLES20.GL_LINES, v * 2, 2)
            }


            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}