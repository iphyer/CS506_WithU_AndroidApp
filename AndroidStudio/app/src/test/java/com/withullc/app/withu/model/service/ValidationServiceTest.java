package com.withullc.app.withu.model.service;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.service.ValidationService.SignInHandler;
import com.withullc.app.withu.model.service.ValidationService.SignUpHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.withullc.app.withu.utils.TestUtils.MockUser;
import static com.withullc.app.withu.utils.TestUtils.getMockUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by shantanusinghal on 31/10/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ValidationService.class, FirebaseAuth.class, Log.class})
public class ValidationServiceTest {

    private ValidationService validationService;

    @Mock private User mokUser;
    @Mock private FirebaseUser mokFirebaseUser;
    @Mock private FirebaseAuth mokFirebaseAuth;

    @Before
    public void setUp() throws Exception {
        mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenCallRealMethod();

        spy(Log.class);
        mockStatic(FirebaseAuth.class);
        when(FirebaseAuth.getInstance()).thenReturn(mokFirebaseAuth);
        when(mokFirebaseAuth.getCurrentUser()).thenReturn(mokFirebaseUser);

        validationService = new ValidationService();
    }

    @Test
    public void itShouldTryCreatingAndSetAsFirebaseUserIfSuccessful() throws Exception {
        // Given
        Task<Void> mokUpdateTask = mock(Task.class);
        Task<AuthResult> mokCreateTask = mock(Task.class);
        SignUpHandler mokHandler = mock(SignUpHandler.class);
        final Task<Void> mokUpdateTaskResult = mock(Task.class);
        final Task<AuthResult> mokCreateTaskResult = mock(Task.class);

        when(mokFirebaseAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mokCreateTask);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((OnCompleteListener<AuthResult>) invocation.getArguments()[0]).onComplete(mokCreateTaskResult);
                return null;
            }}).when(mokCreateTask).addOnCompleteListener(any(OnCompleteListener.class));
        when(mokCreateTaskResult.isSuccessful()).thenReturn(true);
        when(mokFirebaseAuth.getCurrentUser()).thenReturn(mokFirebaseUser);
        when(mokFirebaseUser.updateProfile(any(UserProfileChangeRequest.class))).thenReturn(mokUpdateTask);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((OnCompleteListener<Void>) invocation.getArguments()[0]).onComplete(mokUpdateTaskResult);
                return null;
            }}).when(mokUpdateTask).addOnCompleteListener(any(OnCompleteListener.class));
        when(mokUpdateTaskResult.isSuccessful()).thenReturn(true);

        // When
        validationService.signUp(getMockUser(), MockUser.password, mokHandler);

        // Then
        assertThat(validationService.getFireUser(), is(mokFirebaseUser));
        verify(mokFirebaseAuth).createUserWithEmailAndPassword(MockUser.email, MockUser.password);
        verify(mokCreateTask).addOnCompleteListener(any(OnCompleteListener.class));
        verify(mokFirebaseUser, times(2)).updateProfile(any(UserProfileChangeRequest.class));
        verify(mokUpdateTask, times(2)).addOnCompleteListener(any(OnCompleteListener.class));
        verify(mokHandler).successCallback(mokFirebaseUser);
    }

    @Test
    public void itShouldTryCreatingAndSetNullFirebaseUserIfUnsuccessful() throws Exception {
        // Given
        Task<AuthResult> mokCreateTask = mock(Task.class);
        SignUpHandler mokHandler = mock(SignUpHandler.class);
        final Task<AuthResult> mokCreateTaskResult = mock(Task.class);

        when(mokFirebaseAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mokCreateTask);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((OnCompleteListener<AuthResult>) invocation.getArguments()[0]).onComplete(mokCreateTaskResult);
                return null;
            }}).when(mokCreateTask).addOnCompleteListener(any(OnCompleteListener.class));
        when(mokCreateTaskResult.isSuccessful()).thenReturn(false);

        // When
        validationService.signUp(getMockUser(), MockUser.password, mokHandler);

        // Then
        assertThat(validationService.getFireUser(), is(nullValue()));
        verify(mokFirebaseAuth).createUserWithEmailAndPassword(MockUser.email, MockUser.password);
        verify(mokCreateTask).addOnCompleteListener(any(OnCompleteListener.class));
    }

    @Test
    public void itShouldTryAuthenticatingAndSetAsFirebaseUserIfSuccessful() throws Exception {
        // Given
        Task<AuthResult> mokAuthTask = mock(Task.class);
        SignInHandler mokHandler = mock(SignInHandler.class);
        final Task<AuthResult> mokAuthTaskResult = mock(Task.class);

        when(mokFirebaseAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mokAuthTask);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((OnCompleteListener<AuthResult>) invocation.getArguments()[0]).onComplete(mokAuthTaskResult);
                return null;
            }}).when(mokAuthTask).addOnCompleteListener(any(OnCompleteListener.class));
        when(mokAuthTaskResult.isSuccessful()).thenReturn(true);
        when(mokFirebaseAuth.getCurrentUser()).thenReturn(mokFirebaseUser);

        // When
        validationService.signIn(MockUser.email, MockUser.password, mokHandler);

        // Then
        assertThat(validationService.getFireUser(), is(mokFirebaseUser));
        verify(mokFirebaseAuth).signInWithEmailAndPassword(MockUser.email, MockUser.password);
        verify(mokAuthTask).addOnCompleteListener(any(OnCompleteListener.class));
        verify(mokHandler).successCallback(mokFirebaseUser);
    }

    @Test
    public void itShouldTryAuthenticatingAndSetNullFirebaseUserIfUnsuccessful() throws Exception {
        // Given
        Task<AuthResult> mokAuthTask = mock(Task.class);
        SignInHandler mokHandler = mock(SignInHandler.class);
        final Task<AuthResult> mokAuthTaskResult = mock(Task.class);

        when(mokFirebaseAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mokAuthTask);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((OnCompleteListener<AuthResult>) invocation.getArguments()[0]).onComplete(mokAuthTaskResult);
                return null;
            }}).when(mokAuthTask).addOnCompleteListener(any(OnCompleteListener.class));
        when(mokAuthTaskResult.isSuccessful()).thenReturn(false);

        // When
        validationService.signIn(MockUser.email, MockUser.password, mokHandler);

        // Then
        assertThat(validationService.getFireUser(), is(nullValue()));
        verify(mokFirebaseAuth).signInWithEmailAndPassword(MockUser.email, MockUser.password);
        verify(mokAuthTask).addOnCompleteListener(any(OnCompleteListener.class));
    }

    @Test
    public void itShouldSignOutCurrentUser() throws Exception {
        // When
        validationService.signOut();

        // Then
        verify(mokFirebaseAuth).signOut();
    }
}