package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.repository.PlaceRepository;

public class PlacesFragmentViewModel extends BaseViewModel<Place> {

    public PlacesFragmentViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
        allItems = repository.getAll();
    }
}
