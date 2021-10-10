package com.coupleblog.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseActivity

data class DialogItem(val strText: String, val iIconRes: Int, val callback:(()->Unit))

/** Item List를 출력하기 위한 Dialog이다. DialogItem ArrayList를 넣어주면
 *  해당 리스트를 가지고 dialog에 출력해준다
 *
 *  @param context activity
 *  @param strTitle title string
 *  @param itemList DialogItem(String, iIconRes, callback) List
 *  @param bCancelable setCanceledOnTouchOutside
 */

class CB_ItemListDialog(context: Activity, strTitle: String,
                        itemList: ArrayList<DialogItem>, bCancelable: Boolean) : Dialog(context)
{
    init
    {
        val binding: ItemListBinding = DataBindingUtil.setContentView(context, R.layout.dialog_cb_list_item)
        binding.apply {
            this.strTitle   = strTitle
            adapter         = ItemListAdapter(this@CB_ItemListDialog, itemList)
            itemRecyclerView.hasFixedSize()
        }

        window!!.apply{
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        setCanceledOnTouchOutside(bCancelable)
        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG)
        show()
    }

    override fun dismiss()
    {
        super.dismiss()
        CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG)
    }
}

class ItemListAdapter(private val dialog: Dialog, private val itemList: ArrayList<DialogItem>)
    : RecyclerView.Adapter<ItemListAdapter.ViewHolder>()
{
    class ViewHolder(private val binding: DialogItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(dialogItem: DialogItem, dialog: Dialog)
        {
            binding.apply {
                strText     = dialogItem.strText
                iIconRes    = dialogItem.iIconRes

                // if you click one item, dismiss dialog
                container.setOnClickListener {
                    dialog.dismiss()
                    dialogItem.callback.invoke()
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<DialogItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.comment_item, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(itemList[position], dialog)

    override fun getItemCount(): Int = itemList.size
}