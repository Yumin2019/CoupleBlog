package com.coupleblog.util

import android.os.Bundle
import android.view.View
import androidx.core.content.MimeTypeFilter.matches
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.platform.ui.UiController
import com.coupleblog.R
import junit.framework.AssertionFailedError

class TestFragmentUtil
{
    companion object
    {

        inline fun <reified F : Fragment> launchFragmentScenario(
            bundle: Bundle?, fragment: F, navController: NavController
        ): FragmentScenario<F>
        {
            return launchFragmentInContainer(bundle, R.style.Theme_MaterialComponents_DayNight)
            {
                fragment.also { fragment ->
                    fragment.viewLifecycleOwnerLiveData.observeForever { lifeCycleOwner ->
                        if (lifeCycleOwner != null)
                        {
                            // The fragment’s view has just been created
                            Navigation.setViewNavController(fragment.requireView(), navController)
                        }
                    }
                }
            }
        }
    }
}