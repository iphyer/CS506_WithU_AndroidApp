package com.withullc.app.withu.utils;

import com.withullc.app.withu.model.dataObjects.User;

import static com.withullc.app.withu.utils.TestUtils.MockUser.email;
import static com.withullc.app.withu.utils.TestUtils.MockUser.picRef;
import static com.withullc.app.withu.utils.TestUtils.MockUser.name;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by shantanusinghal on 31/10/17.
 */

public class TestUtils {

    public static class MockUser {
        public static final String id = "jd1";
        public static final String fName = "John Doe";
        public static final String lName = "John Doe";
        public static final String name = "John Doe";
        public static final boolean isMale = true;
        public static final boolean isFemale = false;
        public static final String password = "johndoe";
        public static final String email = "john@doe.com";
        public static final String picRef = "jq9w45qpwcqwp4fj58";
        public static final String phone = "1234567890";
        public static final boolean isWalker = false;
    }

    public static User getMockUser() {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getPictureRef()).thenReturn(picRef);
        return user;
    }

}
