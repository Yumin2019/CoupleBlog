package com.coupleblog.fragment.days

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.dialog.CB_LoadingDialog
import com.coupleblog.dialog.CB_StickerDialog
import com.coupleblog.model.CB_Days
import com.coupleblog.model.DAYS_ITEM_TYPE
import com.coupleblog.model.DAYS_TIME_FORMAT
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_AppFunc.Companion.toCalendar
import com.coupleblog.singleton.CB_AppFunc.Companion.toDate
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CB_NewDaysFragment : CB_BaseFragment()
{
    private var _binding: NewDaysBinding? = null
    private val binding get() = _binding!!

    private val coupleRef = CB_AppFunc.getCouplesRoot().child(CB_AppFunc.curUser.strCoupleKey!!)

    // for editing
    lateinit var strDaysKey: String
    lateinit var strEvent: String
    var daysData: CB_Days? = null

    private fun getActiveTimeFormatAdapter() = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view,
        listOf(getString(R.string.str_days), getString(R.string.str_months), getString(R.string.str_years)))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        _binding = NewDaysBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            fragment = this@CB_NewDaysFragment
            viewModel = CB_ViewModel.Companion
            eventAdapter = ArrayAdapter(requireContext(), R.layout.cb_spinner_item, R.id.item_text_view,
                listOf(getString(R.string.str_past_event), getString(R.string.str_future_event), getString(R.string.str_annual_event)))

            eventAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

                val iPosition = position + 1
                var strEventDate = CB_ViewModel.strEventDate.value!!
                var year = CB_AppFunc.getCurCalendar().get(Calendar.YEAR)
                with(timeFormatAutoCompleteTextView)
                {
                    if (iPosition == DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal)
                    {
                        // We limits ANNUAL_EVENT to select only days format
                        CB_ViewModel.iDaysTimeFormat.postValue(DAYS_TIME_FORMAT.DAYS.ordinal)
                        setText(getString(R.string.str_days))
                        isEnabled = false
                        daysTimeFormatLayout.isEnabled = false

                        // We limits this year to select for annual event
                        strEventDate = year.toString() + strEventDate.substring(4)
                    }
                    else
                    {
                        isEnabled = true
                        daysTimeFormatLayout.isEnabled = true
                        setAdapter(getActiveTimeFormatAdapter())

                        if(iPosition == DAYS_ITEM_TYPE.PAST_EVENT.ordinal)
                        {
                            // if the selected date is in the future, change
                            if(strEventDate.toCalendar() > CB_AppFunc.getCurCalendar())
                            {
                                --year
                                strEventDate = year.toString() + strEventDate.substring(4)
                            }
                        }
                        else // FUTURE
                        {
                            // if the selected date is in the past, change
                            if(strEventDate.toCalendar() <= CB_AppFunc.getCurCalendar())
                            {
                                ++year
                                strEventDate = year.toString() + strEventDate.substring(4)
                            }
                        }
                    }
                }

                // event 타입에 따라서 연도 조정이 필요하다.
                CB_ViewModel.strEventDate.postValue(strEventDate)
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
        strDaysKey = requireArguments().getString(CB_DaysDetailFragment.DAYS_KEY) ?: ""
        strEvent = requireArguments().getString(CB_DaysDetailFragment.DAYS_EVENT_TYPE) ?: ""

        if(strDaysKey.isNotEmpty() and strEvent.isNotEmpty())
        {
            // if edit state, load days data with postKey
            coupleRef.child(strEvent).child(strDaysKey).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    daysData = snapshot.getValue<CB_Days>()
                    if(daysData == null)
                    {
                        Log.e(strTag, "days data load cancelled")
                        CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                            R.string.str_days_data_load_failed, R.drawable.error_icon, false,
                            listener = { _, _ ->
                                findNavController().popBackStack()
                            })
                        return
                    }

                    CB_ViewModel.apply {
                        strDaysTitle       .postValue(daysData!!.strTitle)
                        strEventDate       .postValue(daysData!!.strEventDate)
                        strDaysDesc        .postValue(daysData!!.strDesc)
                        strDaysIconRes     .postValue(daysData!!.strIconRes)
                        iDaysEventType     .postValue(daysData!!.iEventType)
                        iDaysTimeFormat    .postValue(daysData!!.iTimeFormat)

                        val strEventType = when(iDaysEventType.value!!)
                        {
                            DAYS_ITEM_TYPE.PAST_EVENT.ordinal -> getString(R.string.str_past_event)
                            DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal -> getString(R.string.str_future_event)
                            DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal -> getString(R.string.str_annual_event)
                            else -> ""
                        }

                        val strTimeFormat = when(daysData!!.iTimeFormat)
                        {
                            DAYS_TIME_FORMAT.DAYS.ordinal -> getString(R.string.str_days)
                            DAYS_TIME_FORMAT.MONTHS.ordinal -> getString(R.string.str_months)
                            DAYS_TIME_FORMAT.YEARS.ordinal -> getString(R.string.str_years)
                            else -> ""
                        }

                        binding.apply {
                            timeFormatAutoCompleteTextView.setText(strTimeFormat, false)
                            eventAutoCompleteTextView.setText(strEventType, false)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.e(strTag, "days data load cancelled", error.toException())
                    CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                        R.string.str_post_data_load_failed, R.drawable.error_icon, true)
                }
            })

            binding.okButton.setText(R.string.str_edit_event)
        }
        else
        {
            CB_ViewModel.resetNewDaysFragmentLiveData()
        }

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
        var strListName = ""
        var iDaysCount = 0

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

        when(iDaysEventType)
        {
            DAYS_ITEM_TYPE.PAST_EVENT.ordinal ->
            {
                strListName = "past-event-list"
                iDaysCount = CB_ViewModel.iPastEventCount.value!!

                // if the selected date is in the future, change
                if(strEventDate.toCalendar() > CB_AppFunc.getCurCalendar())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_invalid_date_error)
                    return
                }
            }

            DAYS_ITEM_TYPE.FUTURE_EVENT.ordinal ->
            {
                strListName = "future-event-list"
                iDaysCount = CB_ViewModel.iFutureEventCount.value!!

                if(strEventDate.toCalendar() <= CB_AppFunc.getCurCalendar())
                {
                    CB_SingleSystemMgr.showToast(R.string.str_invalid_date_error)
                    return
                }
            }

            DAYS_ITEM_TYPE.ANNUAL_EVENT.ordinal ->
            {
                strListName = "annual-event-list"
                iDaysCount = CB_ViewModel.iAnnualEventCount.value!!
            }
        }

        // check the size of list
        // we'll check if user really want to post
        CB_AppFunc.confirmDialog(activity, R.string.str_information,
            R.string.str_days_confirm, R.drawable.info_icon, true, R.string.str_write,
            yesListener = { _, _ ->

                val dialog = CB_LoadingDialog(activity).apply { show() }

                CB_AppFunc.networkScope.launch {

                    try
                    {
                        if(strDaysKey.isNotEmpty() and strEvent.isNotEmpty())
                        {
                            // 편집을 하는 경우
                            if(strListName == strEvent)
                            {
                                // 동일한 리스트에서 값만 갱신
                                coupleRef.child(strEvent).child(strDaysKey).setValue(CB_Days(strDaysTitle, strDaysDesc, strDaysIconRes,
                                    strEventDate, iDaysEventType, iDaysTimeFormat, CB_AppFunc.getDateStringForSave(),
                                    CB_AppFunc.getUid(), iDaysCount))
                                Log.d(strTag, "updated days")

                                launch(Dispatchers.Main)
                                {
                                    dialog.cancel()
                                    CB_SingleSystemMgr.showToast(R.string.str_days_edited)
                                    backPressed()
                                }
                            }
                            else
                            {
                                // 리스트가 변하는 경우에는 기존에 데이터를 없애고 새로 데이터를 만들어야 한다.
                                val daysListRoot = coupleRef.child(strListName)
                                val daysKey = daysListRoot.push().key
                                if(daysKey == null)
                                {
                                    // daysKey 가 없는 경우 dialog 처리
                                    launch(Dispatchers.Main)
                                    {
                                        dialog.cancel()
                                        CB_AppFunc.okDialog(activity, R.string.str_error,
                                            R.string.str_writing_failed, R.drawable.error_icon, true)
                                    }
                                    return@launch
                                }

                                daysListRoot.child(daysKey).setValue(CB_Days(strDaysTitle, strDaysDesc, strDaysIconRes,
                                    strEventDate, iDaysEventType, iDaysTimeFormat, CB_AppFunc.getDateStringForSave(),
                                    CB_AppFunc.getUid(), iDaysCount)).await()
                                Log.d(strTag, "added days")

                                coupleRef.child(strEvent).child(strDaysKey).setValue(null).await()
                                Log.d(strTag, "prevDays deleted")

                                launch(Dispatchers.Main)
                                {
                                    dialog.cancel()
                                    CB_SingleSystemMgr.showToast(R.string.str_days_edited)

                                    // 이전과 경로가 완전히 달라져서 리스트로 이동한다.
                                    findNavController().popBackStack(R.id.CB_DaysFragment, false)
                                }
                            }
                        }
                        else
                        {
                            // 추가를 하는 경우
                            val daysListRoot = coupleRef.child(strListName)
                            val daysKey = daysListRoot.push().key
                            if(daysKey == null)
                            {
                                // daysKey 가 없는 경우 dialog 처리
                                launch(Dispatchers.Main)
                                {
                                    dialog.cancel()
                                    CB_AppFunc.okDialog(activity, R.string.str_error,
                                        R.string.str_writing_failed, R.drawable.error_icon, true)
                                }
                                return@launch
                            }

                            daysListRoot.child(daysKey).setValue(CB_Days(strDaysTitle, strDaysDesc, strDaysIconRes,
                                strEventDate, iDaysEventType, iDaysTimeFormat, CB_AppFunc.getDateStringForSave(),
                                CB_AppFunc.getUid(), iDaysCount))
                            Log.d(strTag, "added days")

                            launch(Dispatchers.Main)
                            {
                                dialog.cancel()
                                CB_SingleSystemMgr.showToast(R.string.str_days_added)
                                backPressed()
                            }

                            // send notification to couple
                            if(!CB_AppFunc.coupleUser.strFcmToken.isNullOrEmpty())
                            {
                                CB_AppFunc.sendNotification(strDaysTitle, String.format(getString(R.string.str_days_notification),
                                    CB_AppFunc.curUser.strUserName), CB_AppFunc.coupleUser.strFcmToken!!)
                            }
                        }
                    }
                    catch(e: FirebaseException)
                    {
                        e.printStackTrace()
                        launch(Dispatchers.Main)
                        {
                            dialog.cancel()
                            CB_AppFunc.okDialog(activity, R.string.str_error,
                                R.string.str_writing_failed, R.drawable.error_icon, true)
                        }
                    }
                }
            }, R.string.str_cancel, null)
   }
}