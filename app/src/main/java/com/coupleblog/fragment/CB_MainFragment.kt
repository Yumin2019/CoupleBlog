package com.coupleblog.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.R
import com.coupleblog.fragment.listfragments.CB_CouplePostsFragment
import com.coupleblog.fragment.listfragments.CB_MyPostsFragment
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.coupleblog.fragment.PAGE_TYPE.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

enum class PAGE_TYPE
{
    MY_POSTS,
    COUPLE_POSTS,

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
        setHasOptionsMenu(true)

        // 각 부분별 처리를 할 Fragment 를 가지고 adapter 를 생성한다.
        val pagerAdapter = object : FragmentStateAdapter(parentFragmentManager, viewLifecycleOwner.lifecycle) {
            private val fragments = arrayOf<Fragment>(
                CB_MyPostsFragment(),
                CB_CouplePostsFragment()
            )

            override fun createFragment(position: Int) = fragments[position]
            override fun getItemCount() = fragments.size
        }

       // ViewPager 설정
       with(binding)
       {
           // ViewPager2 에 어댑터를 연결한다.
           container.apply {

               adapter = pagerAdapter
               registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                   override fun onPageSelected(position: Int)
                   {
                       super.onPageSelected(position)
                       when(position)
                       {
                           MY_POSTS.ordinal -> { CB_ViewModel.bAddButton.postValue(true) }
                           COUPLE_POSTS.ordinal -> { CB_ViewModel.bAddButton.postValue(false) }
                           else -> {}
                       }
                   }
               })
           }

           TabLayoutMediator(tabLayout, container) { tabLayout, position ->

               // 탭 메뉴를 설정한다.
               tabLayout.text = when(position)
               {
                   // 마지막에 추가된 Fragment가 backPress 이벤트를 받는다.
                   MY_POSTS.ordinal  -> "My Posts"
                   else -> "Couple Posts"
               }

           }.attach()
       }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {
            R.id.action_logout ->
            {
                // 로그아웃을 진행한다.
                CB_AppFunc.getAuth().signOut()

                // 프레그먼트를 종료시킨다.
                findNavController().popBackStack()
                true
            }

            R.id.action_mailbox ->
            {
                // move to mailBox Fragment
                beginAction(R.id.action_CB_MainFragment_to_CB_MailBoxFragment, R.id.CB_MainFragment)
                true
            }
            else -> {super.onOptionsItemSelected(item)}
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