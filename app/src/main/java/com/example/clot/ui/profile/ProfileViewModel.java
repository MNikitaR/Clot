package com.example.clot.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.clot.models.Profile;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<Profile> userProfile = new MutableLiveData<>();

    public void setUserProfile(Profile profile) {
        userProfile.setValue(profile);
    }

    public LiveData<Profile> getUserProfile() {
        return userProfile;
    }
}