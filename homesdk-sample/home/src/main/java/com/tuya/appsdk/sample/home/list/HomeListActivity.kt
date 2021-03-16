/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tuya.appsdk.sample.home.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.tuya.appsdk.sample.home.list.adapter.HomeListAdapter
import com.tuya.appsdk.sample.home.list.enum.HomeListPageType
import com.tuya.appsdk.sample.user.R
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback

/**
 * Home List Example
 *
 * @author yueguang [](mailto:developer@tuya.com)
 * @since 2021/1/18 6:10 PM
 */
class HomeListActivity : AppCompatActivity() {
    var type = HomeListPageType.LIST
    lateinit var adapter: HomeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity_list)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get this page type
        type = intent.getIntExtra("type", HomeListPageType.LIST)

        // Set title
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.title =
                getString(if (type == HomeListPageType.SWITCH) R.string.home_switch_home else R.string.home_home_list)

        // Set list
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        adapter = HomeListAdapter(type)
        rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Query home list from server
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(object : ITuyaGetHomeListCallback {
            override fun onSuccess(homeBeans: MutableList<HomeBean>?) {
                adapter.data = homeBeans as ArrayList<HomeBean>
                adapter.notifyDataSetChanged()
            }

            override fun onError(errorCode: String?, error: String?) {

            }

        })
    }
}