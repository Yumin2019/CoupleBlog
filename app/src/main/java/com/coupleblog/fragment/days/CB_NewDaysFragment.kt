package com.coupleblog.fragment.days

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_StickerDialog
import com.coupleblog.model.DAYS_ITEM_TYPE
import com.coupleblog.model.DAYS_TIME_FORMAT
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

class CB_NewDaysFragment : CB_BaseFragment()
{
    private var _binding: NewDaysBinding? = null
    private val binding get() = _binding!!

    private fun getActiveTimeFormatAdapter() = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view,
        listOf(getString(R.string.str_days), getString(R.string.str_months), getString(R.string.str_years)))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewDaysBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            fragment = this@CB_NewDaysFragment
            viewModel = CB_ViewModel.Companion
            eventAdapter = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view,
                listOf(getString(R.string.str_past_event), getString(R.string.str_future_event), getString(R.string.str_annual_event)))

            eventAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

                val iPosition = position + 1
                with(timeFormatAutoCompleteTextView)
                {
                    if (iPosition == DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal)
                    {
                        // We limits ANNUAL_EVENT to select only days format
                        CB_ViewModel.iDaysTimeFormat.postValue(DAYS_TIME_FORMAT.DAYS.ordinal)
                        setText(getString(R.string.str_days))
                        isEnabled = false
                        daysTimeFormatLayout.isEnabled = false
                    }
                    else
                    {
                        isEnabled = true
                        daysTimeFormatLayout.isEnabled = true
                        setAdapter(getActiveTimeFormatAdapter())
                    }
                }

                // event 타입에 따라서 연도 조정이 필요하다.
                CB_ViewModel.strEventDate.postValue(CB_ViewModel.strEventDate.value!!)
                CB_ViewModel.iDaysEventType.postValue(iPosition)
            }

            timeFormatAdapter = getActiveTimeFormatAdapter()
            timeFormatAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                val iPosition = position + 1
                CB_ViewModel.iDaysTimeFormat.postValue(iPosition)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        CB_ViewModel.resetNewDaysFragmentLiveData()

        binding.titleEditText.apply {

            doAfterTextChanged { text ->

                if (text?.isEmpty() == true)
                {
                    binding.titleTextInputLayout.error = getString(R.string.str_input_title)
                }
                else
                {
                    binding.titleTextInputLayout.error = null
                }
            }

            // Next EditText
            setOnEditorActionListener { _, _, _ ->

                CB_AppFunc.openIME(binding.textEditText, requireActivity())
                true
            }
        }
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
        if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.STICKER))
            return

        Log.i(strTag, "iconButton")
        CB_StickerDialog(requireActivity())
    }

    fun calendarButton()
    {
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER))
            return

        CB_SingleSystemMgr.registerDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER)

        val constraintsBuilder = CalendarConstraints.Builder()
        val calendar = CB_AppFunc.getCurCalendar()

        when(CB_ViewModel.iDaysEventType.value!!)
        {
            DAYS_ITEM_TYPE.PAST_EVENT.ordinal ->
            {
                constraintsBuilder.setValidator(DateValidatorPointBackward.now())
            }

            DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal ->
            {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                constraintsBuilder.setValidator(DateValidatorPointForward.from(calendar.timeInMillis))
            }

            DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal ->
            {
                val today = MaterialDatePicker.todayInUtcMilliseconds()
                calendar.timeInMillis = today
                calendar[Calendar.MONTH] = Calendar.JANUARY
                val janThisYear = calendar.timeInMillis

                calendar.timeInMillis = today
                calendar[Calendar.MONTH] = Calendar.DECEMBER
                val decThisYear = calendar.timeInMillis

                constraintsBuilder.setStart(janThisYear).setEnd(decThisYear)
            }
        }

        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Event date")
            .setCalendarConstraints(constraintsBuilder.build())
            .build().apply {

                addOnPositiveButtonClickListener {
                    calendar.timeInMillis = it
                    val strDate = CB_AppFunc.calendarToDayString(calendar)
                    CB_ViewModel.strEventDate.postValue(strDate)
                }

                addOnDismissListener {
                    CB_SingleSystemMgr.releaseDialog(CB_SingleSystemMgr.DIALOG_TYPE.DATE_PICKER)
                }

                show(this@CB_NewDaysFragment.requireActivity().supportFragmentManager, "birthDateButton")
            }
    }

    fun okButton()
    {
        infoLog("okButton")
        if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.LOADING_DIALOG))
            return

        val strDaysTitle = CB_ViewModel.strDaysTitle.value!!.trim()
        val strDaysDesc = CB_ViewModel.strDaysDesc.value!!.trim()
        val strEventDate = CB_ViewModel.strEventDate.value!!
        val strDaysIconRes = CB_ViewModel.strDaysIconRes.value!!
        val iDaysEventType = CB_ViewModel.iDaysEventType.value!!
        val iDaysTimeFormat = CB_ViewModel.iDaysTimeFormat.value!!
        val activity = requireActivity()

        if(strDaysTitle.isEmpty())
        {
            // 초기에 빈 경우를 막는다
            CB_SingleSystemMgr.showToast(R.string.str_input_title)
            return
        }
        else if(binding.titleTextInputLayout.error != null)
        {
            CB_AppFunc.okDialog(activity, getString(R.string.str_error),
                binding.titleTextInputLayout.error.toString(), R.drawable.error_icon, true)
            return
        }
        else if(strDaysIconRes == CB_AppFunc.getResourceName(R.drawable.question))
        {
            // User didn't select icon for item
            CB_SingleSystemMgr.showToast(R.string.str_select_icon)
            return
        }
   }
        // check the size of list


       /* // we'll check if user really want to post
        CB_AppFunc.confirmDialog(activity, R.string.str_information,
            R.string.str_post_confirm, R.drawable.info_icon, true, R.string.str_post,
            yesListener = { _, _ ->

        val days = CB_Days(




                            CB_AppFunc.getDateStringForSave(),
                            CB_AppFunc.getUid(), 0
                    )





    }*/
}