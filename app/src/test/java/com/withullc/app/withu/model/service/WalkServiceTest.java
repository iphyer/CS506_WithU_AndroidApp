package com.withullc.app.withu.model.service;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Created by shantanusinghal on 31/10/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({UserService.class, FirebaseDatabase.class, Log.class})
public class WalkServiceTest {

    private WalkService walkService;

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
        when(mokDatabaseRef.child("walks")).thenReturn(mokUserRef);

        walkService = new WalkService();
    }

    /**
     * Note that we're verifying double invocations for the calls within
     * the constructor, that's because we've creating an extra test instance
     * besides the static final instance field that's already present.
     *
     * @throws Exception
     */
    @Test
    public void itShouldCreateWalkerReferenceOnInitialization() throws Exception {
        // When
        WalkService.getInstance();

        // Then
        verifyStatic(FirebaseDatabase.class, times(2));
        FirebaseDatabase.getInstance();
        verify(mokFirebaseDatabase, times(2)).getReference();
        verify(mokDatabaseRef, times(2)).child("walks");
    }

}