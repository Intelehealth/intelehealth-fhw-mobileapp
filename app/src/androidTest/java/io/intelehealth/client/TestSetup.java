package io.intelehealth.client;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.azimolabs.conditionwatcher.ConditionWatcher;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.intelehealth.client.activities.setup_activity.SetupActivity;
import io.intelehealth.client.instruction.ServerListLoadingInstruction;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;

/**
 * Created by Dexter Barretto on 8/12/17.
 * Github : @dbarretto
 */


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestSetup {

    @Rule
    public ActivityTestRule<SetupActivity> mActivityRule = new ActivityTestRule<>(
            SetupActivity.class);
    private String mUserName;
    private String mPassword;
    private String mServerIp;
    private String mPrefix;
    private String mAdminPassword;

    private String mSelectionText;

    @Before
    public void initValidString() {
        // Specify a valid string.
        mUserName = "nurse";
        mPassword = "Nurse123";
        mServerIp = "139.59.17.206";
        mPrefix = "DTB";
        mAdminPassword = "admin";
        mSelectionText = "Telemedicine clinic 1";
    }

    @Test
    public void testSetup() throws Exception {

        onView(withId(android.R.id.button1)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isEnabled(); // no constraints, they are checked above
            }

            @Override
            public String getDescription() {
                return "click plus button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });

        onView(withId(R.id.editText_URL))
                .perform(typeText(mServerIp), pressImeActionButton());

        onView(withId(R.id.editText_prefix))
                .perform(click())
                .perform(typeText(mPrefix), pressImeActionButton());

        onView(withId(R.id.email))
                .perform(scrollTo(), typeText(mUserName), pressImeActionButton());

        onView(withId(R.id.password))
                .perform(typeText(mPassword), pressImeActionButton());

        onView(withId(R.id.admin_password))
                .perform(typeText(mAdminPassword));

        //ConditionWatcher.waitForCondition(new ServerListLoadingInstruction());

        onView(withId(R.id.spinner_location))
                .perform(scrollTo(),click());

        onData(allOf(is(instanceOf(String.class)), is(mSelectionText))).perform(click());

        onView(withId(R.id.setup_submit_button))
                .perform(scrollTo(), click());
    }
}
