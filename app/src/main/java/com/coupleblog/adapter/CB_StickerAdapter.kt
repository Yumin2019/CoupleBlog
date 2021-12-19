package com.coupleblog.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.dialog.StickerItemBinding
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_AppFunc.Companion.stickerList
import com.coupleblog.singleton.CB_AppFunc.Companion.stickerOptions

class CB_StickerAdapter(private val clickEvent: (Int)->Unit)
    : RecyclerView.Adapter<CB_StickerAdapter.ViewHolder>()
{
    class ViewHolder(private val binding: StickerItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(position: Int, clickEvent: (Int)->Unit)
        {
            binding.imgSticker.apply {

                setImageBitmap(
                    BitmapFactory.decodeResource(CB_AppFunc.application.resources,
                        stickerList[position], stickerOptions)
                )

                setOnClickListener { clickEvent.invoke(position) }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<StickerItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.photo_row_sticker, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(position, clickEvent)
    }

    override fun getItemCount() = stickerList.size
}