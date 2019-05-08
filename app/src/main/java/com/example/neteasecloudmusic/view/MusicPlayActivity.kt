package com.example.neteasecloudmusic.view

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.neteasecloudmusic.R
import com.example.neteasecloudmusic.model.Status
import com.example.neteasecloudmusic.model.SongDetailBean
import com.example.neteasecloudmusic.netservice.LoginService
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.music_play_layout.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch

class MusicPlayActivity : AppCompatActivity() {
    internal lateinit var id: String
    internal var name: String? = null
    private var artist: String? = null
    private var songDetail = SongDetailBean()

    private var musicPlayService: MusicPlayService ? = null

    private var tag1 = false
    private var tag2 = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_play_layout)
        supportActionBar?.hide()

        val i = intent
        val b = i.extras
        if (b != null) {
            id = b.getString("id")
            name = b.getString("name")
            artist = b.getString("artist")
        }

        Hawk.init(this)
            .build()

        initLayout()

        getMusicPlayed(id)
    }

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            musicPlayService = (service as MusicPlayService.MyBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicPlayService = null
        }
    }

    private fun PlayMusic(){
        val intent = Intent(this@MusicPlayActivity, MusicPlayService::class.java)
        val url = Hawk.get("musicurl$id", "")
        if (url != "") {
            intent.putExtra("url", url)
            startService(intent)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
//            rotateView.play()
        } else {
            Toast.makeText(this, "获取歌曲url过程中出现了问题", Toast.LENGTH_SHORT)
        }
    }

    private fun getMusicPlayed(id: String?) {
        LoginService.getMusicURL(id) { status, data ->
            launch(UI) {
                when (status) {
                    Status.SUCCESS -> {
                        Hawk.put("musicurl$id", data?.data?.get(0)?.url)
                        Toast.makeText(
                            this@MusicPlayActivity,
                            "成功获取歌曲",
                            Toast.LENGTH_SHORT
                        ).show()
                        PlayMusic()
                        myListener()
                    }
                    Status.UNMATCHED -> Toast.makeText(
                        this@MusicPlayActivity,
                        "未获取歌曲详情",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> Toast.makeText(
                        this@MusicPlayActivity,
                        "出现了问题T_T  $id",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun myListener(){
        play_button.setOnClickListener {
            musicPlayService?.let {
                it.playOrPause()
            }
        }

//            val animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360.0f)
//            animator.duration = 10000
//            animator.interpolator = LinearInterpolator()
//            animator.repeatCount = -1


//        stop_button.setOnClickListener {
//            play_button.text = "PLAY"
//            musicPlayService!!.stop()
////            animator.pause()
//            musicPlayService!!.tag = false
//        }

    }

    fun initLayout(){
        LoginService.getSongDetail(id) { status, data ->
            launch(UI){
                when(status) {
                    Status.SUCCESS-> {
                        Hawk.put("data",data)
                        Hawk.put("musicpic$id", data?.songs?.get(0)?.al?.picUrl)
                        val pic = Hawk.get("musicpic$id", "")
//                        Glide.with(this@MusicPlayActivity)
//                            .load(pic)
//                            .into(rotateView)
                        songDetail = Hawk.get("data")
                        songName.text = name
                        singerName.text = artist
                    }
                    Status.ERROR-> {
                        Toast.makeText(this@MusicPlayActivity,"ERROR", Toast.LENGTH_SHORT)
                    }
                    Status.UNMATCHED-> {
                        Toast.makeText(this@MusicPlayActivity,"UNMATCHED", Toast.LENGTH_SHORT)
                    }
                }
            }
        }
    }
}