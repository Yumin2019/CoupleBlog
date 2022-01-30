package com.coupleblog.fragment.days

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.adapter.CB_DaysAdapter
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.fragment.DaysBinding
import com.coupleblog.fragment.post.CB_PostDetailFragment
import com.coupleblog.model.CB_Days
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.util.CB_DaysItemClickListener
import com.firebase.ui.database.FirebaseRecyclerOptions

class CB_DaysFragment : CB_BaseFragment(), CB_DaysItemClickListener
{
    private var _binding            : DaysBinding? = null
    private val binding get() = _binding!!

    private var eventAdapters: ArrayList<CB_DaysAdapter> = arrayListOf()
    // past-event-list   / eventKey 1 / event
    // future-event-list / eventKey 1 / event
    // annual-event-list / eventKey 1 / event

    lateinit var strDaysKey: String
    lateinit var strEvent: String

    private val coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DaysBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner      = viewLifecycleOwner
            viewModel           = CB_ViewModel.Companion
            pastEventAdapter    = eventAdapters[0]
            futureEventAdapter  = eventAdapters[1]
            annualEventAdapter  = eventAdapters[2]

            pastEventLayoutManager = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            futureEventLayoutManager = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

            annualEventLayoutManager = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd  = true
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val queries = arrayListOf(coupleRef.child("past-event-list"),
            coupleRef.child("future-event-list"),
            coupleRef.child("annual-event-list"))

        for(i: Int in 0 until 3)
        {
            val options = FirebaseRecyclerOptions.Builder<CB_Days>()
                .setQuery(queries[i].orderByChild("iOrderIdx"), CB_Days::class.java)
                .build()

            eventAdapters.add(CB_DaysAdapter(this@CB_DaysFragment, options))
        }
    }

    override fun onStart()
    {
        super.onStart()
        for(i: Int in eventAdapters.indices)
        {
            eventAdapters[i].startListening()

            // update the size of days item
            CB_AppFunc.postDelayedUI(1000, null, {
                val count = eventAdapters[i].itemCount
                when(i)
                {
                    0 -> CB_ViewModel.iPastEventCount.postValue(count)
                    1 -> CB_ViewModel.iFutureEventCount.postValue(count)
                    2 -> CB_ViewModel.iAnnualEventCount.postValue(count)
                }
            })

        }
    }

    override fun onStop()
    {
        super.onStop()
        for(i: Int in eventAdapters.indices)
        {
            eventAdapters[i].apply {
                stopListening()
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    override fun clickedDaysItem(tDays: CB_Days, strDaysKey: String)
    {
        val args = bundleOf(
            CB_DaysDetailFragment.DAYS_KEY to strDaysKey,
            CB_DaysDetailFragment.DAYS_EVENT_TYPE to tDays.getEventTypeString(),
        )
        beginAction(R.id.action_CB_DaysFragment_to_CB_DaysDetailFragment, R.id.CB_DaysFragment, args)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_calendar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_add_item ->
            {
                beginAction(R.id.action_CB_DaysFragment_to_CB_NewDaysFragment, R.id.CB_DaysFragment)
            }

            else -> { super.onOptionsItemSelected(item) }
        }

        return true
    }

    // recyclerview adapter with realtime DB
    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}