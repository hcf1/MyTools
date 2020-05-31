package com.myTools.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.myTools.services.MusicService
import com.myTools.R


const val CTL_ACTION = "com.myTools.fragments.CTL_ACTION"
const val UPDATE_ACTION = "com.myTools.fragments.UPDATE_ACTION"
const val PACKAGE_NAME="com.myTools"

class MusicFragment : Fragment() {
    private lateinit var co: Context
    // 获取界面中的显示歌曲标题、作者文本框
    private lateinit var title: TextView
    private lateinit var author: TextView
    // 播放/暂停、停止按钮
    private lateinit var play: ImageButton
    private lateinit var stop: ImageButton
    private lateinit var activityReceiver: ActivityReceiver
    // 定义音乐的播放状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    internal var status = 0x11
    internal var titleStrs = arrayOf("心愿", "约定", "美丽新世界")
    internal var authorStrs = arrayOf("未知艺术家", "周蕙", "伍佰")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.startService(Intent(context, MusicService::class.java))
        activityReceiver = ActivityReceiver()
        // 创建IntentFilter
        val filter = IntentFilter()
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION)
        // 注册BroadcastReceiver
        context.registerReceiver(activityReceiver, filter)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_music, container, false)
        // 获取程序界面中的两个按钮
        play = rootView.findViewById(R.id.play)
        stop = rootView.findViewById(R.id.stop)
        title = rootView.findViewById(R.id.title)
        author = rootView.findViewById(R.id.author)
        val listener = View.OnClickListener { source ->
            // 创建Intent
            val intent = Intent(CTL_ACTION)
            intent.`package` = PACKAGE_NAME
            when (source.id) {
                // 按下播放/暂停按钮
                R.id.play -> intent.putExtra("control", 1)
                // 按下停止按钮
                R.id.stop -> intent.putExtra("control", 2)
            }
            // 发送广播，将被Service组件中的BroadcastReceiver接收到
            Log.i("aaa","检测到按钮事件")
            activity?.sendBroadcast(intent)
        }
        // 为两个按钮的单击事件添加监听器
        play.setOnClickListener(listener)
        stop.setOnClickListener(listener)
        return rootView
    }
    // 自定义的BroadcastReceiver，负责监听从Service传回来的广播
    inner class ActivityReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            Log.i("aaa","接收到改界面的广播")
            // 获取Intent中的update消息，update代表播放状态
            val update = intent.getIntExtra("update", -1)
            // 获取Intent中的current消息，current代表当前正在播放的歌曲
            val current = intent.getIntExtra("current", -1)
            if (current >= 0)
            {
                title.text = titleStrs[current]
                author.text = authorStrs[current]
            }
            when (update)
            {
                0x11 ->
                {
                    play.setImageResource(R.drawable.play)
                    status = 0x11
                }
                // 控制系统进入播放状态
                0x12 ->
                {
                    // 在播放状态下设置使用暂停图标
                    play.setImageResource(R.drawable.pause)
                    // 设置当前状态
                    status = 0x12
                }
                // 控制系统进入暂停状态
                0x13 ->
                {
                    // 在暂停状态下设置使用播放图标
                    play.setImageResource(R.drawable.play)
                    // 设置当前状态
                    status = 0x13
                }
            }
        }
    }
}
