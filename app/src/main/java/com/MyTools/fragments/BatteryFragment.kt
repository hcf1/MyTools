package com.myTools.fragments

//import android.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import com.myTools.R

class BatteryFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView=inflater.inflate(R.layout.fragment_battery, container, false)
        var listView=rootView.findViewById<ListView>(R.id.battery)
        var names= arrayOf("电压(mV)","温度(℃)")
        val details= arrayOf(arguments!!.getIntArray("battery")!!.get(4),arguments!!.getIntArray("battery")!!.get(5))
        var listItems=ArrayList<Map<String,Any>>()
        for (i in names.indices){
            var item=HashMap<String,Any>()
            item["name"]=names[i];
            item["detail"]=details[i]
            listItems.add(item)
        }
        var sp=SimpleAdapter(context,listItems,
            R.layout.listview, arrayOf("name","detail"),
            intArrayOf(
                R.id.textView,
                R.id.textView2
            ))
        listView.adapter=sp
        return rootView
    }
}
