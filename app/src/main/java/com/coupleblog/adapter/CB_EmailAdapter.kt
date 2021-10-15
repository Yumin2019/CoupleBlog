package com.coupleblog.adapter

import com.coupleblog.fragment.CB_MailBoxFragment
import com.coupleblog.fragment.MailItemBinding
import com.coupleblog.model.CB_Mail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue

class CB_EmailAdapter(private val fragment: CB_MailBoxFragment,
                      private val emailRef: DatabaseReference)
    : RecyclerView.Adapter<CB_EmailAdapter.ViewHolder>()
{
    companion object
    {
        const val strTag = "EmailAdapter"
    }

    private var childEventListener: ChildEventListener? = null
    private val emailKeyList = ArrayList<String>()
    private val emailList = ArrayList<CB_Mail>()

    init
    {
        val childEventListener = object : ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {
                // 아이템이 추가된 상황
                val mail = snapshot.getValue<CB_Mail>()

                emailKeyList.add(snapshot.key!!)
                emailList.add(mail!!)
                notifyItemChanged(emailList.size - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                // 아이템이 변경이 된 경우
                // 새 정보를 받아온다.
                val newMail = snapshot.getValue<CB_Mail>()
                val mailKey = snapshot.key

                // 해당 위치 정보의 인덱스를 찾는다.
                val mailIdx = emailKeyList.indexOf(mailKey)
                if(mailIdx > -1 && newMail != null)
                {
                    // 이전에 있던 데이터이고 새로운 정보가 제대로 들어온 경우에 처리한다. 로컬 정보 갱신
                    emailList[mailIdx] = newMail
                    notifyItemChanged(mailIdx)
                }
                else
                {
                    Log.e(strTag, "onChildChanged:unknown data")
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot)
            {
                // Comment 아이템이 삭제된 경우
                val mailKey = snapshot.key

                val mailIdx = emailKeyList.indexOf(mailKey)
                if(mailIdx > -1)
                {
                    // 해당 아이템을 삭제한다.
                    emailKeyList.removeAt(mailIdx)
                    emailList.removeAt(mailIdx)
                    notifyItemRemoved(mailIdx)
                }
                else
                {
                    Log.e(strTag, "onChildRemoved:unknown data")
                }
            }

            // we don't support onChildMoved
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}

            override fun onCancelled(error: DatabaseError)
            {
                Log.e(strTag, "onCancelled:mail list")
                CB_AppFunc.okDialog(fragment.requireActivity(), fragment.getString(R.string.str_error),
                    fragment.getString(R.string.str_comment_list_load_failed), R.drawable.error_icon, true)
            }
        }

        // register childEventListener to commentRef
        emailRef.addChildEventListener(childEventListener)

        // keep it so it can be removed on app stop
        this.childEventListener = childEventListener
    }

    fun clearListener()
    {
        childEventListener?.let { emailRef.removeEventListener(it) }
    }

    class ViewHolder(private val binding: MailItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(argFragment: CB_MailBoxFragment, argMailBox: CB_Mail)
        {
            binding.apply {
                fragment    = argFragment
                mailData    = argMailBox
                checkboxImageView.setOnClickListener {
                    // add n-th index
                    layoutPosition
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
        DataBindingUtil.inflate<MailItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.mail_item, viewGroup, false
        ).let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(fragment, emailList[position])

    override fun getItemCount(): Int = emailList.size
}