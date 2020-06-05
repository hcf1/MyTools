package com.myTools.utility

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.myTools.DownUtil
import java.io.RandomAccessFile

/**
 * 断点下载的工具类，提供记录断点位置，管理下载的文件的功能
 * */
class BreakPointFileManager(
    var context: FragmentActivity?,
    var downUtil: DownUtil,
    var targetFile: String,
    var threadNum: Int,
    var threads: Array<DownUtil.DownThread?>
) {
    fun saveProgress(context: FragmentActivity?) {
        var preferences = context?.getSharedPreferences("downProgress", Context.MODE_PRIVATE)
        var editor = preferences?.edit()
        editor?.clear()
        if (threads.isNotEmpty()) {
            for (i in threads) {
                if (i != null) {
                    editor?.putInt(i.name, i.length)
                    editor?.apply()
                }
            }
        }
    }

    private fun readProgress(context: FragmentActivity?): HashMap<String, Int> {
        var preferences = context?.getSharedPreferences("downProgress", Context.MODE_PRIVATE)
        return preferences?.all as HashMap<String, Int>
    }

    fun setThreadDownloadContent(fileSize: Long) {
        val currentPartSize = fileSize / threadNum + 1
        val file = RandomAccessFile(targetFile, "rw")
        // 设置本地文件的大小
        file.setLength(fileSize)
        file.close()
        //判断是继续下载还是新建下载
        if (downUtil.ispause) {
            downUtil.ispause=false
            var map = readProgress(context)
            for ((i, length) in map.entries) {
                // 每条线程使用一个RandomAccessFile进行下载
                val currentPart = RandomAccessFile(targetFile, "rw")
                // 定位该线程的下载位置
                currentPart.seek(length + i.toInt() * currentPartSize)
                // 创建下载线程
                threads[i.toInt()] = downUtil.DownThread(
                    length + i.toInt() * currentPartSize,
                    currentPartSize,
                    currentPart,
                    length,
                    i
                )
                // 启动下载线程
                threads[i.toInt()]?.start()
            }
        } else {
            for (i in 0 until threadNum) {
                var length = 0
                // 计算每条线程下载的开始位置
                val startPos = i * currentPartSize
                // 每条线程使用一个RandomAccessFile进行下载
                val currentPart = RandomAccessFile(targetFile, "rw")
                // 定位该线程的下载位置
                currentPart.seek(startPos)
                // 创建下载线程
                threads[i] = downUtil.DownThread(
                    startPos,
                    currentPartSize,
                    currentPart,
                    length,
                    i.toString()
                )
                // 启动下载线程
                threads[i]?.start()
            }
        }
    }
}