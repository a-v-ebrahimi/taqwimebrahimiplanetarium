package com.daftar.taqwimplanetarium

import COORDS_PER_VERTEX
import android.opengl.GLES20
import android.opengl.GLU
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class Sphere(val name: String, val spaceX: Float, val spaceY: Float, val spaceZ: Float, spaceR: Float,
             azimuth: Float, altitude: Float,
             sphereR: Float,
             private val sphereColor: FloatArray) {
    private var vertexCount: Int = 0
    private var vertexBuffer: FloatBuffer

    var sphereX = 0f
    var sphereY = 0f
    var sphereZ = 0f
    val stp = 18

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

        val localR = spaceR * cos(altitude)
        sphereX = spaceX + localR * cos(azimuth).toFloat()
        sphereY = spaceY + localR * sin(azimuth).toFloat()
        sphereZ = spaceZ + spaceR * sin(altitude).toFloat()

        for (b in -90..90 step stp)
            for (a in 0..360 step stp) {
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

                // second face
                triangleCoords.addAll(p2)
                triangleCoords.addAll(p1NextRing)
                triangleCoords.addAll(p2NextRing)
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

    fun draw(mvpMatrix: FloatArray, modelViewMatrix: FloatArray, view: IntArray, projectionMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        var output = floatArrayOf(0f, 0f, 0f)
        GLU.gluProject(sphereX, sphereY, sphereZ, modelViewMatrix, 0, projectionMatrix, 0, view, 0,
                output, 0
        )
        Log.d("tqpt", "name : $name")
        Log.d("tqpt", String.format("space : %f/%f/%f", sphereX, sphereY, sphereZ))
        Log.d("tqpt", String.format("x/y : %f/%f/%f", output[0], output[1], output[2]))
        Log.d("tqpt", "----")


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
                GLES20.glUniform4fv(colorHandle, 1, sphereColor, 0)
            }

            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)


            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}