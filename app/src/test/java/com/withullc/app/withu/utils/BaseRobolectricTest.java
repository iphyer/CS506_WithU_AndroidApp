package com.withullc.app.withu.utils;

import android.app.Application;

import com.withullc.app.withu.BuildConfig;
import com.withullc.app.withu.TestWithUApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by shantanusinghal on 31/10/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        application = TestWithUApplication.class,
        sdk = 21)
public class BaseRobolectricTest {

    public Application getApplication() {
        return RuntimeEnvironment.application;
    }

    /**
     * stupid hack to ensure this class doesn't fail to initialize
     *
     * @throws Exception
     */
    @Test
    public void itShouldBeSomeone() throws Exception {
        assertThat("someone", is("someone"));
    }
}
