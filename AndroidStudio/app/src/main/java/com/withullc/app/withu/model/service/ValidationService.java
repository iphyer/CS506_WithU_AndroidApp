package com.withullc.app.withu.model.service;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.withullc.app.withu.model.dataObjects.User;

/**
 * Service class used to sign in, sign out, and sign up for an account
 */

public class ValidationService {

    private FirebaseAuth auth;

    private static FirebaseUser fireUser;
    private static final ValidationService VALIDATION_SERVICE = new ValidationService();

    public interface SignInHandler {
        void successCallback(FirebaseUser user);
        void failureCallback();
    }

    public interface SignUpHandler {
        void successCallback(FirebaseUser user);
        void failureCallback(FirebaseUser user);
    }

    ValidationService() {
        auth = FirebaseAuth.getInstance();
        setFireUser(auth.getCurrentUser());

    }

    public static ValidationService getInstance() {
        return VALIDATION_SERVICE;
    }

    /**
     * Method to create a new user in the authentication system
     * @param newUser The information about the new user to be created
     * @param password The password of the new user
     * operation fails
     */
    public void signUp(final User newUser, String password, final SignUpHandler handler) {
        final ValidationService that = this;
        auth.createUserWithEmailAndPassword(newUser.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("SignUp", "signUpWithEmail: success");
                    setFireUser(auth.getCurrentUser());
                    // update the display name and the photo url of the FirebaseUser object
                    updateUserName(newUser.getName());
                    if(newUser.getPictureRef() != null) {
                        updateUserPicUrl(newUser.getPictureRef());
                    }

                    handler.successCallback(auth.getCurrentUser());
                }
                else {
                    Log.w("SignUp", "createUserWithEmail: failed", task.getException());
                    handler.failureCallback(auth.getCurrentUser());
                    setFireUser(null);
                }
            }
        });
    }

    /**
     * Method used to sign into an existing user account
     * @param email The email associated to the existing user account
     * @param password The password associated with a user account
     * if the operation fails
     */
    public void signIn(String email, String password, final SignInHandler handler) {
        // If on the creation of the authentication service the user is
        // already in existence return the user

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("SignIn","SUCCESS");
                    setFireUser(auth.getCurrentUser());
                    handler.successCallback(auth.getCurrentUser());
                }
                else {
                    Log.w("SignIn","SignInWithEmail: failed", task.getException());
                    setFireUser(null);
                    handler.failureCallback();
                }
            }
        });
    }

    /**
     * This method signs the current user out of their account,
     * and restricts access to the application
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Method to update the name of the user.
     * @param name The new name of the user.
     */
    private void updateUserName(String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        updateProfile(profileUpdates);
    }

    /**
     * Method to update the user's profile picture
     * @param url The url of the user's profile picture
     */
    private void updateUserPicUrl(String url) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(url))
                .build();

        updateProfile(profileUpdates);
    }

    /**
     * Private method which updates the fireUser object with new information
     * @param profileUpdates Update to be made to the FirebaseUser user
     */
    private void updateProfile(UserProfileChangeRequest profileUpdates) {
        fireUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("UpdateProfile", "User profile updated.");
                        }
                        else {
                            Log.w("UpdateProfile", "Update failed", task.getException());
                        }
                    }
                });
    }

    private static void setFireUser(FirebaseUser fireUser) {
        ValidationService.fireUser = fireUser;
    }

    public FirebaseUser getFireUser() {
        return fireUser;
    }
}
