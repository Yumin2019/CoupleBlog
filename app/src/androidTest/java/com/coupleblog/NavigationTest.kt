package com.coupleblog

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.ActivityTestRule
import com.coupleblog.fragment.CB_LoginFragment
import com.coupleblog.fragment.CB_PermissionFragment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class NavigationTest
{
    @Before
    fun setUp()
    {
        ActivityScenario.launch(CB_MainActivity::class.java)
    }

    @Test
    fun checkLoginValidValue()
    {
        // create a testNavController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // create a graphical FragmentScenario
        val permissionScenario = launchFragmentInContainer<CB_PermissionFragment>()

        // set a graph on the TestNavController and available for findNavController() APIs.
        permissionScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
/*
        // input email and password
        onView(ViewMatchers.withId(R.id.email_edit_text))
            .perform(ViewActions.typeText("test3@gmail.com"), ViewActions.closeSoftKeyboard())

        onView(ViewMatchers.withId(R.id.password_edit_text))
            .perform(ViewActions.typeText("rladbals82^^*"), ViewActions.closeSoftKeyboard())*/

        // verify that performing a click changes the navController's state
        onView(ViewMatchers.withId(R.id.ok_button)).perform(ViewActions.click())

        assertThat("Expecting to go to loginFragment",
            navController.currentDestination?.id, equalTo(R.id.CB_LoginFragment))
    }
}