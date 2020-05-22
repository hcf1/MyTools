package com.example.batterydetails

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var intent = Intent()
        var com = ComponentName(context, MainActivity::class.java)
        intent.component = com
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//如果启动的是非本应用activity，则启动一个新的任务栈
        Toast.makeText(context,"启动完成！",Toast.LENGTH_LONG)
        context.startActivity(intent)
    }
}
