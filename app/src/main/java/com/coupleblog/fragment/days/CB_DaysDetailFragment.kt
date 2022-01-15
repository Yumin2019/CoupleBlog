package com.coupleblog.fragment.days

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_ItemListDialog
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.DialogItem
import com.coupleblog.fragment.post.CB_PostDetailFragment
import com.coupleblog.fragment.profile.CB_ProfileInfoFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.firebase.FirebaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CB_DaysDetailFragment : CB_BaseFragment()
{
    private var _binding            : DaysDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DaysDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_DaysDetailFragment
        }
        return binding.root
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }

    fun profileButton(strUid: String)
    {
        beginAction(R.id.action_CB_DaysDetailFragment_to_CB_ProfileInfoFragment,
            R.id.CB_DaysDetailFragment, bundleOf(CB_ProfileInfoFragment.ARGU_UID to strUid))
    }

    fun menuButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.ITEM_LIST_DIALOG))
            return

        val listItem = arrayListOf(
            DialogItem(getString(R.string.str_edit_days), R.drawable.pencil,
                callback = { editDays() }),

            DialogItem(getString(R.string.str_delete_days), R.drawable.trash_can,
                callback = { deleteDays() })
        )

        CB_ItemListDialog(requireActivity(), getString(R.string.str_mail_menu), listItem, true)
    }

    private fun editDays()
    {
        // move to NewPostFragment for editing
        //val arguments = bundleOf(CB_DaysDetailFragment.ARGU to postKey)
        //beginAction(R.id.action_CB_DaysFragment_to_CB_NewDaysFragment, R.id.CB_DaysDetailFragment, arguments)
    }

    private fun deleteDays()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        // we ask the user if you really want to delete post
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning, R.string.str_delete_msg,
            R.drawable.warning_icon, true,
            R.string.str_delete,
            yesListener = { _, _ ->

                // if user really want to delete, delete days
                val dialog = CB_LoadingDialog(requireActivity()).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        // delete days
                   /*     val imgPath = CB_ViewModel.tPost.value!!.strImgPath
                        Log.d(strTag, "strImgPath:$imgPath")
                        if(!imgPath.isNullOrEmpty())
                        {
                            // find upper folder for post
                            CB_AppFunc.deleteFileFromStorage(imgPath, strTag,
                                "post image deleted path:$imgPath",
                                "post image delete failed path:$imgPath")
                        }

                        postRef.setValue(null).await()
                        commentRef.setValue(null).await()
                        Log.d(strTag, "postRef, commentRef deleted")*/

                        launch(Dispatchers.Main)
                        {
                            // if success, move to MainFragment
                            dialog.cancel()
                            CB_SingleSystemMgr.showToast(R.string.str_post_deleted)
                            backPressed()
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        // if fail, error dialog
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                                R.string.str_post_delete_failed, R.drawable.error_icon, true)
                        }
                    }
                }

            }, R.string.str_cancel, null)
    }
}