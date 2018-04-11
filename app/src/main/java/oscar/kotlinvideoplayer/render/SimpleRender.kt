package oscar.kotlinvideoplayer.render

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class SimpleRender : GLSurfaceView.Renderer{

    override fun onDrawFrame(gl: GL10?) {
        //每帧都需要调用该方法进行绘制。绘制时通常先调用glClear来清空framebuffer。
        //然后调用OpenGL ES其他接口进行绘制
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //当surface的尺寸发生改变时，该方法被调用，。往往在这里设置ViewPort。或者Camara等。
        gl?.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

}