package com.coupleblog.fragment

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.fragment.listfragments.CB_CouplePostsFragment
import com.coupleblog.fragment.listfragments.CB_MyPostsFragment
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.fragment.PAGE_TYPE.*
import com.coupleblog.fragment.mail.CB_MailBoxFragment
import com.coupleblog.fragment.profile.CB_ProfileFragment
import com.coupleblog.model.CB_Couple
import com.coupleblog.model.CB_Days
import com.coupleblog.widget.CB_DaysWidgetProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PAGE_TYPE
{
    PROFILE,
    MY_POSTS,
    COUPLE_POSTS,
    MAILBOX,

    PAGE_TYPE_NONE,
}

class CB_MainFragment : CB_BaseFragment()
{
    private var _binding            : MainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = MainBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_MainFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // get user's online status after login
        CB_AppFunc.getUserPresence()

        // ??? ????????? ????????? ??? Fragment ??? ????????? adapter ??? ????????????.
        val pagerAdapter = object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
            private val fragments = arrayOf<Fragment>(
                CB_ProfileFragment(),
                CB_MyPostsFragment(),
                CB_CouplePostsFragment(),
                CB_MailBoxFragment()
            )
            override fun createFragment(position: Int) = fragments[position]
            override fun getItemCount() = fragments.size
        }

        // defence code for new updates
       with(CB_AppFunc)
       {
           // ?????? ????????? gmtOffset ?????? ????????? ????????????.
           val gmtOffset = getGMTOffset()
           if(gmtOffset != curUser.iGmtOffset)
           {
               curUser.iGmtOffset = gmtOffset
               getUsersRoot().child(getUid()).setValue(curUser)
           }

           // if couple, didn't have couple info make.
           if(!curUser.strCoupleUid.isNullOrEmpty() && curUser.strCoupleKey.isNullOrEmpty())
           {
               val strCoupleUid = curUser.strCoupleUid!!
               val coupleInfo = CB_Couple(getUid(), strCoupleUid)
               val coupleKey = getCouplesRoot().push().key

               curUser.strCoupleKey = coupleKey
               coupleUser.strCoupleKey = coupleKey
               getUsersRoot().child(getUid()).setValue(curUser)
               getUsersRoot().child(strCoupleUid).setValue(coupleUser)
               getCouplesRoot().child(coupleKey!!).setValue(coupleInfo)
           }
       }

        // if user has no token, make one
        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(strTag, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result.toString()
            Log.d(strTag, "fcmToken:$token")
            CB_AppFunc.getUsersRoot().child(CB_AppFunc.getUid()).child("strFcmToken").setValue(token)
        })

        // if couple, update DaysItem
        if(!CB_AppFunc.curUser.strCoupleKey.isNullOrEmpty())
        {
            val daysList: ArrayList<CB_Days> = arrayListOf()
            val daysKeyList: ArrayList<String> = arrayListOf()
            val coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)
            CB_AppFunc.networkScope.launch {
                CB_AppFunc.loadDaysItem(coupleRef, "future-event-list", daysList, daysKeyList)
                CB_AppFunc.loadDaysItem(coupleRef, "annual-event-list", daysList, daysKeyList)
                delay(2000)

                val strToday = CB_AppFunc.getString(R.string.str_today)
                for(i in 0 until daysList.size)
                {
                    CB_AppFunc.requestWorker(CB_AppFunc.application, daysKeyList[i], daysList[i].strTitle,
                        strToday, CB_AppFunc.curUser.strFcmToken, daysList[i].strEventDate!!)
                }
            }

            // ????????? ???????????? ???????????? ???????????? ?????? ?????????
            val intentAction = Intent(requireContext(), CB_DaysWidgetProvider::class.java)
            intentAction.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(requireContext()).getAppWidgetIds(ComponentName(requireContext(), CB_DaysWidgetProvider::class.java))
            intentAction.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            requireContext().sendBroadcast(intentAction)

            CB_AppFunc.getSharedPref(requireActivity()).edit().apply {
                putString("strCoupleKey", CB_AppFunc.curUser.strCoupleKey)
                putString("strCoupleFcmToken", CB_AppFunc.coupleUser.strFcmToken)
            }.apply()
        }

       // ViewPager ??????
       with(binding)
       {
           container.apply {

               registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                   override fun onPageSelected(position: Int)
                   {
                       super.onPageSelected(position)
                       CB_ViewModel.iPageType.postValue(position)

                       when(position)
                       {
                           PROFILE.ordinal      ->
                           {
                               CB_ViewModel.apply {
                                   val value = coupleUser.value // refresh
                                   coupleUser.postValue(value)

                                   bAddButton.postValue(false)
                               }
                           }
                           MY_POSTS.ordinal     ->
                           {
                               CB_ViewModel.bAddButton.postValue(true)
                           }
                           COUPLE_POSTS.ordinal -> { CB_ViewModel.bAddButton.postValue(false) }
                           MAILBOX.ordinal      -> { CB_ViewModel.bAddButton.postValue(true) }
                           else -> {}
                       }
                   }
               })

               // ViewPager2 ??? ???????????? ????????????.
               adapter = pagerAdapter

               // over scroll animation
               (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
           }

           TabLayoutMediator(tabLayout, container) { tabLayout, position ->

               // ??? ????????? ????????????.
               tabLayout.text = when(position)
               {
                   // ???????????? ????????? Fragment??? backPress ???????????? ?????????.
                   PROFILE.ordinal     -> CB_AppFunc.getString(R.string.str_profile)

                   // MainFragment means this user already did login
                   MY_POSTS.ordinal     ->
                   {
                       val userName = CB_AppFunc.curUser.strUserName!!.trim()
                       // if this user isn't a couple
                       if(CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty() || userName.isEmpty())
                       {
                           CB_AppFunc.getString(R.string.str_my_posts)
                       }
                       else
                       {
                           // if couple, substring or his name
                           if(userName.length <= 8)
                               CB_AppFunc.curUser.strUserName!!
                           else
                               CB_AppFunc.curUser.strUserName!!.substring(0, 8)
                       }
                   }
                   COUPLE_POSTS.ordinal ->
                   {
                       val coupleUserName = CB_AppFunc.coupleUser.strUserName!!.trim()

                       if(CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty() || coupleUserName.isEmpty())
                       {
                           CB_AppFunc.getString(R.string.str_couple_posts)
                       }
                       else
                       {
                           if(coupleUserName.length <= 8)
                               CB_AppFunc.coupleUser.strUserName!!
                           else
                               CB_AppFunc.coupleUser.strUserName!!.substring(0, 8)
                       }
                   }
                   else -> CB_AppFunc.getString(R.string.str_mail)
               }

           }.attach()
       }
    }

    override fun backPressed()
    {
        finalBackPressed()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        CB_ViewModel.bAddButton.postValue(false)
        _binding = null
    }
}