package com.myTools.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.myTools.fragments.CTL_ACTION
import com.myTools.fragments.PACKAGE_NAME
import com.myTools.fragments.UPDATE_ACTION

class MusicService : Service()
{
	private lateinit var serviceReceiver: MyReceiver
	private lateinit var am: AssetManager
	private val musics = arrayOf("wish.mp3", "promise.mp3", "beautiful.mp3")
	private lateinit var mPlayer: MediaPlayer
	// 当前的状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
	private var status = 0x11
	// 记录当前正在播放的音乐
	private var current = 0

	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	override fun onCreate()
	{
		super.onCreate()
		Log.i("aaa","service启动成功！")
		am = assets
		// 创建BroadcastReceiver
		serviceReceiver = MyReceiver()
		// 创建IntentFilter
		val filter = IntentFilter()
		filter.addAction(CTL_ACTION)
		registerReceiver(serviceReceiver, filter)
		// 创建MediaPlayer
		mPlayer = MediaPlayer()
		// 为MediaPlayer播放完成事件绑定监听器
		mPlayer.setOnCompletionListener {
			current++
			if (current >= 3)
			{
				current = 0
			}
			// 发送广播通知Activity更改文本框
			val sendIntent = Intent(UPDATE_ACTION)
			sendIntent.`package` = PACKAGE_NAME
			sendIntent.putExtra("current", current)
			// 发送广播，将被Activity组件中的BroadcastReceiver接收到
			sendBroadcast(sendIntent)
			// 准备并播放音乐
			prepareAndPlay(musics[current])
		}
	}

	inner class MyReceiver : BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent)
		{
			Log.i("aaa","接收到放音乐的广播")
			val control = intent.getIntExtra("control", -1)
			when (control)
			{
				// 播放或暂停
				1 ->
					when (status)
					{
						// 原来处于没有播放状态
						0x11 ->
						{
							// 准备并播放音乐
							prepareAndPlay(musics[current])
							status = 0x12
						}
						// 原来处于暂停状态
						0x12 ->
						{
							// 暂停
							mPlayer.pause()
							// 改变为暂停状态
							status = 0x13
						}
						// 原来处于播放状态
						0x13 ->
						{
							// 播放
							mPlayer.start()
							// 改变状态
							status = 0x12
						}
					}
				// 停止声音
				2 ->
					// 如果原来正在播放或暂停
					if (status == 0x12 || status == 0x13)
					{
						// 停止播放
						mPlayer.stop()
						status = 0x11
					}
			}
			// 广播通知Activity更改图标、文本框
			val sendIntent = Intent(UPDATE_ACTION)
			sendIntent.`package` = PACKAGE_NAME
			sendIntent.putExtra("update", status)
			sendIntent.putExtra("current", current)
			// 发送广播，将被Activity组件中的BroadcastReceiver接收到
			sendBroadcast(sendIntent)
		}
	}
	private fun prepareAndPlay(music: String)
	{
		// 打开指定音乐文件
		val afd = am.openFd(music)
		mPlayer.reset()
		// 使用MediaPlayer加载指定的声音文件。
		mPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
		// 准备声音
		mPlayer.prepare()
		// 播放
		mPlayer.start()
	}
}
