package io.intelehealth.client;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.intelehealth.client.activities.splash_activity.SplashActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Dexter Barretto on 8/13/17.
 * Github : @dbarretto
 */


@RunWith(AndroidJUnit4.class)
@SmallTest
public class TestSplash {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityRule = new ActivityTestRule<>(
            SplashActivity.class);

    @Test
    public void testSplash() {
        onView(withText("Hello World"))
                .check(ViewAssertions.matches(isDisplayed()));
    }
}
