package com.coupleblog.util

import com.coupleblog.model.CB_Days

interface CB_DaysItemClickListener
{
    fun clickedDaysItem(tDays: CB_Days, strDaysKey: String)
}