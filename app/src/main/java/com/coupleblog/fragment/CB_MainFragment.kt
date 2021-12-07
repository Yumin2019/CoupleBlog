package com.coupleblog.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.fragment.listfragments.CB_CouplePostsFragment
import com.coupleblog.fragment.listfragments.CB_MyPostsFragment
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.coupleblog.fragment.PAGE_TYPE.*

enum class PAGE_TYPE
{
    PROFILE,
    MY_POSTS,
    COUPLE_POSTS,
    MAILBOX,

    PAGE_TYPE_NONE,
}

class CB_MainFragment : CB_BaseFragment("MainFragment")
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

        // 각 부분별 처리를 할 Fragment 를 가지고 adapter 를 생성한다.
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

       // 만약 유저의 gmtOffset 값이 다르면 갱신한다.
       with(CB_AppFunc)
       {
           val gmtOffset = getGMTOffset()
           if(gmtOffset != curUser.iGmtOffset)
           {
               curUser.iGmtOffset = gmtOffset
               getUsersRoot().child(getUid()).setValue(curUser)
           }
       }

       // ViewPager 설정
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
                                   var value = coupleUser.value // refresh
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

               // ViewPager2 에 어댑터를 연결한다.
               adapter = pagerAdapter

               // over scroll animation
               (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
           }

           TabLayoutMediator(tabLayout, container) { tabLayout, position ->

               // 탭 메뉴를 설정한다.
               tabLayout.text = when(position)
               {
                   // 마지막에 추가된 Fragment가 backPress 이벤트를 받는다.
                   PROFILE.ordinal     -> "Profile"

                   // MainFragment means this user already did login
                   MY_POSTS.ordinal     ->
                   {
                       val userName = CB_AppFunc.curUser.strUserName!!.trim()
                       // if this user isn't a couple
                       if(CB_AppFunc.curUser.strCoupleUid.isNullOrEmpty() || userName.isEmpty())
                       {
                           "My Posts"
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
                           "Couple Posts"
                       }
                       else
                       {
                           if(coupleUserName.length <= 8)
                               CB_AppFunc.coupleUser.strUserName!!
                           else
                               CB_AppFunc.coupleUser.strUserName!!.substring(0, 8)
                       }
                   }
                   else -> "Mail"
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