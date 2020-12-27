package com.myTools

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.myTools.utility.BreakPointFileManager
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
多线程下载能加速”是有前提的：服务端支持区间请求（Range Requests）单线程下载因受其他瓶颈，如协议层丢包、
网关QoS设置、服务器线程最大带宽设置等限制，没有跑到应有的速度脱离以上前提，多线程下载速度没有优势。

另外多线程现在能加速也体现在另一方面：支持多线程下载的服务端会支持区间请求，一旦下载中断，
下载工具在恢复下载时可以断点续传，从而避免传输中断后又要从零开始下载的悲剧。
 */
open class DownUtil( // 定义下载资源的路径
    var context: FragmentActivity?,
    //下载路径
    private var path: String,
    // 指定所下载的文件的保存位置
    private val targetFile: String,
    // 定义需要使用多少条线程下载资源
    private val threadNum: Int,
    var ispause: Boolean = false,
    // 止重复开启线程下载
    var isDownLoading: Boolean = false,
    var isCancel: Boolean = false,
    var breakPointDownUtil: BreakPointFileManager?
) {
    // 定义下载的线程对象, 初始化threads数组
    private val threads: Array<DownThread?> = arrayOfNulls(threadNum)

    // 定义下载的文件的总大小
    private var fileSize: Long = 0
    fun setUrl(url: String) {
        this.path = url
    }
    // 获取下载的完成百分比
    val completeRate: Double
        get() {
            val sumSize = (0 until threadNum).sumBy { threads[it]!!.length }
            // 返回已经完成的百分比
            return sumSize * 1.0 / fileSize
        }
    val getThreadRate: HashMap<String, Int>
        get() {
            var map = hashMapOf<String, Int>()
            for (thread in threads) {
                if (thread != null) {
                    map.put(thread.tag, (1.0 * thread.length / fileSize * threadNum * 100).toInt())
                }
            }
            return map
        }

    /**
     * 开始下载：1.获取服务端文件大小。2.创建并给每个线程分配任务，启动所有线程
     * */
    fun download() {
        val conn = httpURLConnection()
        //创建断点下载工具类对象
        breakPointDownUtil = BreakPointFileManager(context, this, targetFile, threadNum, threads)
        // 得到文件大小
        fileSize = conn.contentLength.toLong()
        // 获取服务器文件上次修改的时间戳
        breakPointDownUtil!!.lastModified=conn.lastModified
        // 初次获取服务器文件的大小
        breakPointDownUtil!!.oldContentLength= conn.contentLength.toLong()
        conn.disconnect()
        //给每个线程设置下载的大小及下载的起始位置并启动线程
        breakPointDownUtil!!.setThreadDownloadContent(fileSize)
    }

    /**
     * 下载线程类。根据传入的参数从服务器读取文件，之后将文件写入本地RandomAccessFile
     * */
    inner class DownThread(
        private val startPos: Long, // 当前线程的下载位置
        private val currentPartSize: Long, // 定义当前线程负责下载的文件大小
        private val currentPart: RandomAccessFile,// 当前线程需要下载的文件块
        var length: Int,// 定义该线程已下载的字节数
        var tag: String
    ) : Thread(tag) {
        // 定义该线程已下载的字节数
        override fun run() {
            val conn = httpURLConnection()
            readFromConnWriteToFile(conn)
        }

        private fun readFromConnWriteToFile(conn: HttpURLConnection) {
            // 使用getIn/OutputStream（）,会自动调用connect（）
            val inStream = conn.inputStream
            // 跳过startPos个字节，表明该线程只下载自己负责的那部分文件
            //	skipFully(inStream, this.startPos)
            inStream.skip(startPos)
            //	一次读取越少，写入文件时覆盖的内容越少，但读取会更频繁
            val buffer = ByteArray(1024)
            //	无数据则一直阻塞，最多一次读取1024个字节，读到文件尾返回-1
            var hasRead = inStream.read(buffer)
            // 读取网络数据，并写入本地文件中
            while (length < currentPartSize && hasRead > 0) {
                if (isCancel) {
                    Log.i("aaa", currentThread().name + "下载取消")
                    currentPart.close()
                    inStream.close()
                    return
                }
                if (ispause) {
                    Log.i("aaa", currentThread().name + "已暂停")
                    currentPart.close()
                    inStream.close()
                    return
                }
                currentPart.write(buffer, 0, hasRead)
                // 累计该线程下载的总大小
                length += hasRead
                hasRead = inStream.read(buffer)
                Log.i("aaa", currentThread().name + "下载大小：" + this.length)
            }
            Log.i("aaa", this.name + "下载完成")
            currentPart.close()
            inStream.close()
        }
    }

    /**
     * 打开网络连
     * */
    private fun httpURLConnection(): HttpURLConnection {
        val url = URL(path)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 5 * 1000
        conn.requestMethod = "GET"
        conn.setRequestProperty(
            "Accept",
            "image/gif,image/jpeg,image/png,*/*,"
                    + "application/x-shockwave-flash, application/xaml+xml, "
                    + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                    + "application/x-ms-application, application/vnd.ms-excel, "
                    + "application/vnd.ms-powerpoint, application/msword, */*"
        )
        conn.setRequestProperty("Accept-Language", "zh-CN")
        conn.setRequestProperty("Charset", "UTF-8")
        conn.setRequestProperty("Connection", "Keep-Alive")
        //		2.2版本以上HttpURLConnection跟服务交互采用了”gzip”压缩
        //		以下要求http请求不要gzip压缩
        conn.setRequestProperty("Accept-Encoding", "identity")
        conn.connect()
        return conn
    }

    // 定义一个为InputStream跳过bytes字节的方法
    fun skipFully(`in`: InputStream, bytes: Long) {
        var remainning = bytes
        var len: Long
        while (remainning > 0) {
            len = `in`.skip(remainning)
            remainning -= len
        }
    }
}

