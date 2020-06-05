package com.myTools.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.myTools.DownUtil
import com.myTools.R
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class MultiThreadDownLoadFragment : Fragment() {
    private lateinit var url: EditText
    private lateinit var target: EditText
    private lateinit var downBn: Button
    private lateinit var pause: Button
    private lateinit var cancel: Button
    private lateinit var bar: ProgressBar
    private var bars: ArrayList<ProgressBar> = arrayListOf()
    private lateinit var downUtil: DownUtil

    class MyHandler(private var dfragment: WeakReference<MultiThreadDownLoadFragment>) :
        Handler() {
        override fun handleMessage(msg: Message) {
            //定时刷新下载进度
            if (msg.what == 0x123) {
                var downUtil = dfragment.get()?.downUtil
                dfragment.get()?.bar?.progress = msg.arg1
                if (downUtil != null) {
                    for (i in downUtil.getThreadRate.keys) {
                        dfragment.get()?.bars?.get(i.toInt())?.progress =
                            downUtil.getThreadRate.get(i)!!
                    }

                }
            }
            if (msg.what == 0x111) {
                Toast.makeText(dfragment.get()?.activity, "下载完成！", Toast.LENGTH_LONG).show()
            }
            if (msg.what == 0x222) {
                Toast.makeText(dfragment.get()?.activity, "下载暂停！", Toast.LENGTH_LONG).show()
            }
            if (msg.what == 0x333) {
                dfragment.get()?.bar?.progress = 0
                for (i in dfragment.get()?.bars!!) {
                    i.progress = 0
                }
                Toast.makeText(dfragment.get()?.activity, "下载取消！", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 创建一个Handler对象
    private val handler = MyHandler(WeakReference(this))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 0x456
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//    这里attachToRoot设置为false，不然会在container里显示两个view
        var rootView = inflater.inflate(R.layout.multi_thread_download, container, false)
        // 获取程序界面中的三个界面控件
        url = rootView.findViewById(R.id.url)
        target = rootView.findViewById(R.id.target)
        downBn = rootView.findViewById(R.id.down)
        pause = rootView.findViewById(R.id.pause)
        cancel = rootView.findViewById(R.id.cancel)
        bar = rootView.findViewById(R.id.bar)
        var bar1 = rootView.findViewById(R.id.progressBar0) as ProgressBar
        bars.add(bar1)
        var bar2 = rootView.findViewById(R.id.progressBar1) as ProgressBar
        bars.add(bar2)
        var bar3 = rootView.findViewById(R.id.progressBar2) as ProgressBar
        bars.add(bar3)
        var bar4 = rootView.findViewById(R.id.progressBar3) as ProgressBar
        bars.add(bar4)
        var bar5 = rootView.findViewById(R.id.progressBar4) as ProgressBar
        bars.add(bar5)
        var bar6 = rootView.findViewById(R.id.progressBar5) as ProgressBar
        bars.add(bar6)
        return rootView
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0x456 && grantResults != null
            && grantResults.size == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // 初始化DownUtil对象（最后一个参数指定线程数）
            //在自身目录下创建文件夹
            val downloadFile = context!!.getExternalFilesDir("download")
            if (downloadFile != null) {
                downUtil = DownUtil(
                    activity,
                    url.text.toString(),
                    downloadFile.absolutePath + "/" + target.text.toString(),
                    6,
                    false,
                    false,
                    false,
                    null
                )
            }
            pause.setOnClickListener {
                if (downUtil.ispause) {
//                    doNothing
                } else {
                    Log.i("aaa", "监听到暂停事件")
                    downUtil.ispause = true
                    downUtil.isDownLoading = false
                    downUtil.breakPointDownUtil?.saveProgress(activity)
                }
            }
            cancel.setOnClickListener {
                Log.i("aaa", "监听到取消事件")
                downUtil.isCancel = true
                downUtil.isDownLoading = false
                var file =
                    File(context!!.getExternalFilesDir("download")?.absolutePath + "/" + target.text.toString())
                var preferences =
                    context?.getSharedPreferences("downProgress", Context.MODE_PRIVATE)
                if (file.exists()) {
                    //提示文件下载已取消
                    handler.sendEmptyMessage(0x333)
                    //秒后删除下载的文件
                    object : Thread() {
                        override fun run() {
                            sleep(3000)
                            preferences?.edit()?.clear()?.apply()
                            Log.i("aaa", "下载进度已清除")
                            file.delete()
                            Log.i("aaa", "文件已删除")
                        }
                    }.start()
                }
            }
            downBn.setOnClickListener {
                downUtil.isCancel = false
                downUtil.setUrl(url.text.toString())
                if (downUtil.isDownLoading) {
//                    doNothing
                } else {
                    Log.i("aaa", "监听到开始下载事件")
                    // 对象表达式，初始化下载类后开一个新线程执行下载操作
                    object : Thread() {
                        override fun run() {
                            // 开始下载
                            downUtil.isDownLoading = true
                            downUtil.download()
                            // 定义每秒调度获取一次系统的完成进度。在下载线程上再开一个新线程刷新下载进度
                            val timer = Timer()
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    // 获取下载任务的完成比例
                                    val completeRate = downUtil.completeRate
                                    Log.i("aaa", "完总成进度：$completeRate")
                                    val msg = Message()
                                    msg.what = 0x123
                                    msg.arg1 = (completeRate * 100).toInt()
                                    // 发送消息通知界面更新进度条
                                    handler.sendMessage(msg)
                                    // 下载完全后取消任务调度
                                    if (completeRate >= 1) {
                                        //开始等待新的下载任务
                                        downUtil.isDownLoading = false
                                        handler.sendEmptyMessage(0x111)
                                        timer.cancel()
                                    }
                                    if (downUtil.ispause) {
                                        handler.sendEmptyMessage(0x222)
                                        timer.cancel()
                                    }
                                    if (downUtil.isCancel) {
                                        handler.sendEmptyMessage(0x333)
                                        timer.cancel()
                                    }
                                }
                            }, 0, 100)
                        }
                    }.start()
                }
            }
        }
    }
}
