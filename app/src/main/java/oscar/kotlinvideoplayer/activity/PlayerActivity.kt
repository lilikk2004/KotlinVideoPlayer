package oscar.kotlinvideoplayer.activity

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import kotlinx.android.synthetic.main.activity_player.*
import java.io.IOException
import java.util.*
import android.widget.Toast
import android.media.AudioManager
import android.view.*
import org.jetbrains.anko.*
import oscar.kotlinvideoplayer.R
import oscar.kotlinvideoplayer.render.SimpleRender
import oscar.kotlinvideoplayer.render.VideoRender
import java.io.File


class PlayerActivity : Activity() {


    val videoFolder = Environment.getExternalStorageDirectory().path + "/Anime/"

    val videoName1 = "[KissSub&FZSD][Kono_Subarashii_Sekai_ni_Shukufuku_o!_2][08][GB][720P][x264_AAC].mp4"

    val videoName2 = "[KissSub&FZSD][Kono_Subarashii_Sekai_ni_Shukufuku_o!_2][10][GB][720P][x264_AAC].mp4"

    var mediaPlayer = MediaPlayer()

    var timer = Timer()

    var timeTask: TimerTask? = null

    var savedPosition = 0

    var lastTouchTime = System.currentTimeMillis()

    val HIDE_TOOL_TIME = 4000L

    var videoRender: VideoRender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)


        //设置全屏显示
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //隐藏底部虚拟按键
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        initMediaPlayer()

        initGlSurfaceView()

        play_seek_bar.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->  }
            onStartTrackingTouch { seekBar ->  }
            onStopTrackingTouch { seekBar ->
                if(seekBar != null) {
                    mediaPlayer.seekTo(seekBar.progress)
                }
            }
        }

        danma_btn.onClick { openVideoFile() }

        play_btn.onClick { playOrPause() }

        back_btn.onClick { finish() }

        gl_surface_view.onTouch { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    setToolBarVisible(true)
                }
            }
            return@onTouch false
        }
    }

    private fun initMediaPlayer(){
        mediaPlayer.setOnCompletionListener( {mp -> mediaPlayer.reset()})

        mediaPlayer.setOnPreparedListener({mp ->
            mediaPlayer.start()
            //=============
            mediaPlayer.seekTo(savedPosition)//开始时是从0开始播放，恢复时是从指定位置开始播放
            play_seek_bar.max = mediaPlayer.duration//将进度条的最大值设为文件的总时长
            total_time.text = formatTime(mediaPlayer.duration)
            timer = Timer()
            timeTask = object : TimerTask() {
                override fun run() {
                    play_seek_bar.progress = mediaPlayer.currentPosition//将媒体播放器当前播放的位置赋值给进度条的进度
                    runOnUiThread {
                        current_time.text = formatTime(mediaPlayer.currentPosition)
                        val currentTime = System.currentTimeMillis()
                        if(currentTime - lastTouchTime > HIDE_TOOL_TIME){
                            setToolBarVisible(false)
                        }
                    }
                }
            }
            timer.schedule(timeTask, 0, 100)//0秒后执行，每隔100ms执行一次
        })

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            toast("播放器错误")
            return@setOnErrorListener false
        }
    }

    private fun initGlSurfaceView(){
        gl_surface_view.setEGLContextClientVersion(2)
        videoRender = VideoRender()
        videoRender!!.setContext(this)
        gl_surface_view.setRenderer(videoRender)
    }

    fun getSurface(): Surface{
        var holder = gl_surface_view.holder

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {//holder被销毁时回调。最小化时都会回调
                Log.i("bqt", "销毁了--surfaceDestroyed" + "--" + mediaPlayer.currentPosition)
                savedPosition = mediaPlayer.currentPosition//当前播放位置
                mediaPlayer.stop()
                timer.cancel()
                if (timeTask != null) {
                    timeTask!!.cancel()
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (savedPosition > 0) {//如果记录的数据有播放进度。
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(videoFolder + videoName1)
                        mediaPlayer.setDisplay(holder)
                        mediaPlayer.prepare()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

        })


        return Surface(videoRender!!.videoTexture)
    }


    /**
     * 播放本地多媒体
     */
    private fun openVideoFile() {
        lastTouchTime = System.currentTimeMillis()
        val file = File(videoFolder + videoName1)
        if (file.exists()) {
            try {
                mediaPlayer.setDataSource(videoFolder + videoName1)
                //mediaPlayer.setDisplay(video_surface.holder)//****************在哪个容器里显示内容
                mediaPlayer.setSurface(getSurface())
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.prepare()
                play_btn.backgroundResource = R.drawable.bili_player_play_can_pause
            } catch (e: Exception) {
                e.printStackTrace()
                //Toast.makeText(this, "请检查是否有写SD卡权限", 0).show()
                toast("请检查是否有写SD卡权限")
            }

        } else {
            toast("文件不存在")
        }
    }

    /**
     * 播放网络多媒体
     */
/*    fun playUrl() {
        val filepath = et_Url.getText().toString().trim()
        if (!TextUtils.isEmpty(filepath)) {
            try {
                savedFilepath = filepath
                mediaPlayer.setDataSource(filepath)
                mediaPlayer.setDisplay(holder)
                mediaPlayer.prepareAsync()//异步准备
                bt_playUrl.setEnabled(false)
                bt_play.setEnabled(false)
                Toast.makeText(this@MainActivity, "准备中，可能需要点时间……", 1).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "播放失败，请检查是否有网络权限", 0).show()
            }

        } else {
            Toast.makeText(this, "路径不能为空", 0).show()
        }
    }*/

    /**
     * 暂停
     */
    fun playOrPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            play_btn.backgroundResource = R.drawable.bili_player_play_can_play
        } else {
            mediaPlayer.start()
            play_btn.backgroundResource = R.drawable.bili_player_play_can_pause
        }
    }

    /**
     * 停止
     */
    fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
    }

    /**
     * 重播
     */
    fun replay() {
        mediaPlayer.start()
        mediaPlayer.seekTo(0)
    }

    override fun onResume() {
        /**
         * 设置为横屏
         */
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        super.onResume()
    }



    private fun formatTime(progress: Int): String{
        var secondCount = progress / 1000
        var second = secondCount % 60
        var minute = secondCount / 60
        return String.format("%02d:%02d", minute, second)
    }

    private fun setToolBarVisible(isVisible: Boolean){
        if(isVisible){
            lastTouchTime = System.currentTimeMillis()
            top_tool_layout.visibility = View.VISIBLE
            bottom_tool_layout.visibility = View.VISIBLE
        }else{
            top_tool_layout.visibility = View.GONE
            bottom_tool_layout.visibility = View.GONE
        }
    }
}
