package com.withullc.app.withu.model.service;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.withullc.app.withu.model.dataObjects.User;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.withullc.app.withu.utils.TestUtils.MockUser;
import static com.withullc.app.withu.utils.TestUtils.getMockUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Created by shantanusinghal on 30/10/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({UserService.class, FirebaseDatabase.class, Log.class})
public class UserServiceTest {

    private UserService userService;

    @Mock private DatabaseReference mokUserRef;
    @Mock private DatabaseReference mokDatabaseRef;
    @Mock private DatabaseReference mokDummyUserRef;
    @Mock private FirebaseDatabase mokFirebaseDatabase;

    @Before
    public void setUp() throws Exception {
        mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenCallRealMethod();

        spy(Log.class);
        mockStatic(FirebaseDatabase.class);
        when(FirebaseDatabase.getInstance()).thenReturn(mokFirebaseDatabase);
        when(mokFirebaseDatabase.getReference()).thenReturn(mokDatabaseRef);
        when(mokDatabaseRef.child("users")).thenReturn(mokUserRef);

        userService = new UserService();
    }

    /**
     * Note that we're verifying double invocations for the calls within
     * the constructor, that's because we've creating an extra test instance
     * besides the static final instance field that's already present.
     *
     * @throws Exception
     */
    @Test
    public void itShouldCreateUserReferenceOnInitialization() throws Exception {
        // When
        UserService.getInstance();

        // Then
        verifyStatic(FirebaseDatabase.class, times(2));
        FirebaseDatabase.getInstance();
        verify(mokFirebaseDatabase, times(2)).getReference();
        verify(mokDatabaseRef, times(2)).child("users");
    }

    @Test
    public void itShouldCreateUserInDatabase() throws Exception {
        //Given
        User mokUser = getMockUser();
        when(mokUserRef.child(MockUser.id)).thenReturn(mokDummyUserRef);

        // When
        userService.createUser(mokUser, MockUser.id);

        // Then
        verify(mokUserRef).child(MockUser.id);
        verify(mokDummyUserRef).setValue(mokUser);
    }

    @Ignore
    @Test
    public void itShouldRetrieveUserIfPresent() throws Exception {
        //Given
        User mokUser = getMockUser();
        when(mokUserRef.child(MockUser.id)).thenReturn(mokDummyUserRef);

        DataSnapshot mokDataSnapshot = mock(DataSnapshot.class);
        when(mokDataSnapshot.getValue(User.class)).thenReturn(mokUser);
        when(mokUser.getName()).thenReturn(MockUser.name);

        UserService.RetrievalHandler mokHandler = mock(UserService.RetrievalHandler.class);
        ArgumentCaptor<ValueEventListener> capValueListener = ArgumentCaptor.forClass(ValueEventListener.class);

        // When
        userService.retrieveUserOnce(MockUser.id, mokHandler);

        // Then
        verify(mokUserRef).child(MockUser.id);
        verify(mokDummyUserRef, times(1)).addValueEventListener(capValueListener.capture());
        capValueListener.getValue().onDataChange(mokDataSnapshot); // perform async callback
        verify(mokDataSnapshot).getValue(User.class);
        verify(mokHandler).retrieved(mokUser);
    }

//    /**
//     * User Reflection to clear the singleton between test cases.
//     *
//     * @throws Exception
//     */
//    @After
//    public void tearDown() throws Exception {
//        Field USER_REF = UserService.class.getDeclaredField("USER_REF");
//        USER_REF.setAccessible(true);
//        USER_REF.set(UserService.class, null);
//        System.out.printf("here");
//    }

}
