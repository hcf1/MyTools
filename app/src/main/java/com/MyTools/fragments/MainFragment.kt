package com.myTools.fragments

import android.app.ListFragment
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import com.myTools.R


class MainFragment : ListFragment() {
    private var mCallbacks: Callbacks? = null
    // 定义一个回调接口，该Fragment所在Activity需要实现该接口
    // 该Fragment将通过该接口与它所在的Activity交互
    interface Callbacks {
        fun onItemSelecte(id: Int?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        listAdapter = ArrayAdapter<String>(activity,
//            android.R.layout.simple_list_item_activated_1,
//            android.R.id.text1, arrayOf("aa","bb"))
        listAdapter = ArrayAdapter(
            activity,
            android.R.layout.simple_list_item_1,
            activity!!.resources.getStringArray(R.array.menu)
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is Callbacks) {
            throw IllegalStateException("BookListFragment所在的Activity必须实现Callbacks接口!")
        }
        mCallbacks = context
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        mCallbacks?.onItemSelecte(position)
    }
}
