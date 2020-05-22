package com.example.batterydetails

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.batterydetails.fragment.BatteryFragment
import com.example.batterydetails.fragment.MainFragment
import com.example.batterydetails.fragment.MusicFragment


class MainActivity : FragmentActivity(), MainFragment.Callbacks {
    var bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //动态注册receiver,接收电量变化消息
        var batterydetailsreceiver = BatteryDetailsReceiver()
        var filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batterydetailsreceiver, filter)
        var receiver = object : Runnable {
            override fun run() {

            }

        }
        //获取BatteryManager
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        //点击button后响应
//        botton.setOnClickListener {
//            //此处需要返回listener对象(匿名内部类)，不能使用lambda表达式(一般用来实现抽象方法)
//
//        }
//        val c = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) / 10e9//剩余电量纳瓦时
//        val c = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) / 10e6//平均电流微安培
//        val c = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 10e6//瞬时电流微安培
//        val c = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 10e6//剩余电池容量微安时
        val c =
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)//电量百分比(BATTERY_STATS只开放部分权限)
        Toast.makeText(this, "当前电量"+c.toString(), Toast.LENGTH_LONG).show()
    }

    private inner class BatteryDetailsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent?.action)) {
                var level = intent?.getIntExtra("level", 0);    ///电池剩余电量
                var scale = intent?.getIntExtra("scale", 0);  ///获取电池满电量数值
                var technology = intent?.getStringExtra("technology");  ///获取电池技术支持
                var status =
                    intent?.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN); ///获取电池状态
                var plugged = intent?.getIntExtra("plugged", 0);  ///获取电源信息
                var health = intent?.getIntExtra(
                    "health",
                    BatteryManager.BATTERY_HEALTH_UNKNOWN
                );  ///获取电池健康度
                var voltage = intent?.getIntExtra("voltage", 0);  ///获取电池电压
                var temperature = intent?.getIntExtra("temperature", 0)?.div(10);  ///获取电池温度
                var batterys =
                    intArrayOf(level!!, scale!!, plugged!!, health!!, voltage!!, temperature!!)
                bundle.putIntArray("battery", batterys)
                bundle.putString("technology", technology)
            }
        }
    }

    override fun onItemSelecte(id: Int?) {
        // Create fragment and give it an argument specifying the article it should show
        val bFragment = BatteryFragment()
        val mFragment = MusicFragment()
        Log.i("aaa","点击了mainFragment")
        when (id) {
            0 -> {
                bFragment.arguments = bundle
                var transaction = supportFragmentManager.beginTransaction().apply {
                    supportFragmentManager.popBackStack()//弹出最上层fragment
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    replace(R.id.book_detail_container, bFragment)
//                    add(R.id.book_detail_container, bFragment)
                    addToBackStack(null)//将当前事务添加到任务栈
                }
                // Commit the transaction
                transaction.commit()
            }
            1 -> {
                var transaction = supportFragmentManager.beginTransaction().apply {
                    supportFragmentManager.popBackStack()
                    replace(R.id.book_detail_container, mFragment)
//                    add(R.id.book_detail_container, mFragment)
                    addToBackStack(null)
                }
                transaction.commit()
            }
        }
    }
}
