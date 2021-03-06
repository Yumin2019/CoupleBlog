package com.coupleblog.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.fragment.days.CB_DaysFragment
import com.coupleblog.fragment.days.DaysItemBinding
import com.coupleblog.model.CB_Days
import com.coupleblog.util.CB_DaysItemClickListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError

// FirebaseRecyclerAdapter 내부에서 데이터를 Query 값으로 가지고 있는다.
class CB_DaysAdapter(val listener: CB_DaysItemClickListener, options: FirebaseRecyclerOptions<CB_Days>)
    : FirebaseRecyclerAdapter<CB_Days, CB_DaysAdapter.ViewHolder>(options)
{
    class ViewHolder(private val binding: DaysItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(argListener: CB_DaysItemClickListener, argDays: CB_Days, arguKey: String)
        {
            binding.apply {
                listener    = argListener
                tDays       = argDays
                strDaysKey  = arguKey
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<DaysItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.cb_days_item, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: CB_Days)
    {
        val strKey = getRef(position).key!!
        holder.bind(listener, model, strKey)
    }

    override fun onError(error: DatabaseError)
    {
        super.onError(error)
        Log.e("CB_DaysAdapter", error.message)
    }
}