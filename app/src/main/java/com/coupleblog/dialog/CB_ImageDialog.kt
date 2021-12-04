package com.coupleblog.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel

class CB_ImageDialog(context: Context) : Dialog(context)
{
    init
    {
        val binding: ImageBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_cb_image, null, false)
        binding.viewModel = CB_ViewModel.Companion
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)

        window!!.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // margin 적용이 안 되어서 프로그래밍으로 처리
        val layoutParams: FrameLayout.LayoutParams = binding.floatingLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.apply {
            marginEnd = CB_AppFunc.convertDpToPixel(8.0f).toInt()
            marginStart = CB_AppFunc.convertDpToPixel(8.0f).toInt()
        }

        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.IMAGE)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.IMAGE)
    }
}