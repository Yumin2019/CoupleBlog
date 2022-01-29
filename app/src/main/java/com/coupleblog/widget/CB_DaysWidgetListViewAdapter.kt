package com.coupleblog.widget

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.coupleblog.R
import com.coupleblog.fragment.days.DaysItemBinding
import com.coupleblog.model.CB_Days
import com.coupleblog.util.CB_DaysItemClickListener

class CB_DaysWidgetListViewAdapter(var listener: CB_DaysItemClickListener,
                                   daysList_: ArrayList<CB_Days>, keyList_: ArrayList<String>): BaseAdapter()
{
    private var binding: DaysItemBinding? = null
    private val daysList = daysList_
    private val keyList = keyList_

    fun bind(position: Int)
    {
        binding!!.apply {
            listener    = this@CB_DaysWidgetListViewAdapter.listener
            tDays       = daysList[position]
            strDaysKey  = keyList[position]
            executePendingBindings()
        }
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent?.context),
            R.layout.cb_days_item, parent, false)
        bind(position)
        return binding!!.root
    }

    override fun getCount() = daysList.size
    override fun getItem(position: Int) = daysList[position]
    override fun getItemId(position: Int) = position.toLong()
}