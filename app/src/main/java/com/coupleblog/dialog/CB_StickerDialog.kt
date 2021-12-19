package com.coupleblog.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coupleblog.R
import com.coupleblog.a100photo.StickerBSFragment.StickerAdapter
import com.coupleblog.adapter.CB_StickerAdapter
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_AppFunc.Companion.stickerList
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel

class CB_StickerDialog(context: Context) : Dialog(context)
{
    init
    {
        val binding: StickerBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_cb_sticker, null, false)
        binding.apply {
            adapter = CB_StickerAdapter { position ->
                val strResName = CB_AppFunc.getResourceName(stickerList[position])
                CB_ViewModel.strDaysIconRes.value = strResName
                Log.i("stickerDialog", "strResName:$strResName")
                cancel()
            }

            layoutManager = GridLayoutManager(CB_AppFunc.application, 3)
            iStickerSize = stickerList.size
        }
        setContentView(binding.root)

        window!!.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        setCanceledOnTouchOutside(false)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.STICKER)
        show()
    }

    fun exitButton() = onBackPressed()

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.STICKER)
    }
}