package com.coupleblog.fragment.days

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_StickerDialog
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel

class CB_NewDaysFragment : CB_BaseFragment()
{
    private var _binding            : NewDaysBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = NewDaysBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_NewDaysFragment
            viewModel       = CB_ViewModel.Companion
            eventAdapter    = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view, listOf(getString(R.string.str_past_event),
                getString(R.string.str_future_event), getString(R.string.str_annual_event)))

            timeFormatAdapter = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view, listOf(getString(R.string.str_days),
                getString(R.string.str_months), getString(R.string.str_years)))

            eventAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                CB_ViewModel.iDaysEventType.postValue(position)
            }

            timeFormatAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                CB_ViewModel.iDaysTimeFormat.postValue(position)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        CB_ViewModel.resetNewDaysFragmentLiveData()
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

    fun iconButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.STICKER))
            return

        Log.i(strTag, "iconButton")
        CB_StickerDialog(requireActivity())
    }

    fun okButton()
    {
        CB_SingleSystemMgr.showToast("click OK BUTTON")
    }
}