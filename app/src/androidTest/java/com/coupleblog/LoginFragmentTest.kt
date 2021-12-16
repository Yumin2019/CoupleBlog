package com.coupleblog

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.base.MainThread
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.coupleblog.fragment.CB_LoginFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.util.TestFragmentUtil
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class LoginFragmentTest
{

    private lateinit var navController: TestNavHostController

    private fun launchLoginFragmentScenario(bundle: Bundle?): FragmentScenario<CB_LoginFragment>
            = TestFragmentUtil.launchFragmentScenario(bundle, CB_LoginFragment(), navController)

    @Before
    fun setUp()
    {
        ActivityScenario.launch(CB_MainActivity::class.java)

        // create a testNavController
        // set a graph on the TestNavController and available for findNavController() APIs
        runOnUiThread {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            navController.setGraph(R.navigation.nav_graph)
        }
    }

    @Test
    fun checkLoginValidValue()
    {
        // create a graphical FragmentScenario
       // val loginScenario = launchLoginFragmentScenario(null)

        // not auto login
        if(CB_AppFunc.getAuth().currentUser == null)
        {
            // input email and password
            onView(withId(R.id.email_edit_text))
                .perform(ViewActions.typeText("test3@gmail.com"), ViewActions.closeSoftKeyboard())

            onView(withId(R.id.password_edit_text))
                .perform(ViewActions.typeText("rladbals82^^*"), ViewActions.closeSoftKeyboard())

            // move to MainFragment
            onView(withId(R.id.login_button)).perform(ViewActions.click())
        }

        // animation loading
        Thread.sleep(5000)
        // 공부할 것 : testing-samples/ui/espresso/IdlingResourceSample/

        // verify it's mainFragment
        onView(withId(R.id.main_fragment_parent)).check(matches(isDisplayed()))

       /* assertThat("Expecting to go to loginFragment",
            navController.currentDestination?.id, equalTo(R.id.CB_MainFragment))*/
    }
}