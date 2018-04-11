package oscar.kotlinvideoplayer.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import oscar.kotlinvideoplayer.R
import oscar.kotlinvideoplayer.util.RawResourceReader
import oscar.kotlinvideoplayer.util.ShaderHelper

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lenovo on 2018-3-26.
 */

class VideoRender : GLSurfaceView.Renderer {
    private var frameAvailable = false
    internal var textureParamHandle: Int = 0
    internal var textureCoordinateHandle: Int = 0
    internal var positionHandle: Int = 0
    internal var textureTranformHandle: Int = 0

    private var context: Context? = null

    // Texture to be shown in backgrund
    private var textureBuffer: FloatBuffer? = null
    private val textureCoords = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f)
    private val textures = IntArray(1)

    private var width: Int = 0
    private var height: Int = 0

    private var shaderProgram: Int = 0
    private var vertexBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null
    private val videoTextureTransform = FloatArray(16)
    var videoTexture: SurfaceTexture? = null
        private set

    internal var surfaceListener: SurfaceListener? = null

    interface SurfaceListener {
        fun onCreate()
    }

    fun setSurfaceListener(surfaceListener: SurfaceListener) {
        this.surfaceListener = surfaceListener
    }

    fun setContext(context: Context) {
        this.context = context
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        setupGraphics()
        setupVertexBuffer()
        setupTexture()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        //playVideo();
        if (surfaceListener != null) {
            surfaceListener!!.onCreate()
        }
    }

    override fun onDrawFrame(gl: GL10) {
        synchronized(this) {
            if (frameAvailable) {
                videoTexture!!.updateTexImage()
                videoTexture!!.getTransformMatrix(videoTextureTransform)
                frameAvailable = false
            }
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glViewport(0, 0, width, height)
        this.drawTexture()

    }

    private fun setupGraphics() {
        val vertexShader = RawResourceReader.readTextFileFromRawResource(context!!, R.raw.vetext_sharder)
        val fragmentShader = RawResourceReader.readTextFileFromRawResource(context!!, R.raw.fragment_sharder)

        val vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader!!)
        val fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader!!)
        shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                arrayOf("texture", "vPosition", "vTexCoordinate", "textureTransform"))

        GLES20.glUseProgram(shaderProgram)
        textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture")
        textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate")
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram, "textureTransform")
    }

    private fun setupVertexBuffer() {
        // Draw list buffer
        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer!!.put(drawOrder)
        drawListBuffer!!.position(0)

        // Initialize the texture holder
        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer!!.put(squareCoords)
        vertexBuffer!!.position(0)
    }

    private fun setupTexture() {
        val texturebb = ByteBuffer.allocateDirect(textureCoords.size * 4)
        texturebb.order(ByteOrder.nativeOrder())

        textureBuffer = texturebb.asFloatBuffer()
        textureBuffer!!.put(textureCoords)
        textureBuffer!!.position(0)

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("Texture generate")

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        checkGlError("Texture bind")

        videoTexture = SurfaceTexture(textures[0])
        videoTexture!!.setOnFrameAvailableListener(object : SurfaceTexture.OnFrameAvailableListener {
            override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
                synchronized(this) {
                    frameAvailable = true
                }
            }
        })
    }

    private fun drawTexture() {
        // Draw texture

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(textureParamHandle, 0)

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer)

        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle)
    }

    fun checkGlError(op: String) {
        var error: Int
        while (true) {
            error = GLES20.glGetError()
            if(error == GLES20.GL_NO_ERROR) {
                break
            }
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error))
        }
    }

    companion object {

        /**
         *
         */
        private val squareSize = 1.0f
        private val squareCoords = floatArrayOf(-squareSize, squareSize, // top left
                -squareSize, -squareSize, // bottom left
                squareSize, -squareSize, // bottom right
                squareSize, squareSize) // top right

        private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
    }
}
