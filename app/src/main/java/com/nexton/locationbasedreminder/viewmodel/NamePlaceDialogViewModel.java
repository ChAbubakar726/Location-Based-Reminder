package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.repository.PlaceRepository;

public class NamePlaceDialogViewModel extends BaseViewModel<Place> {

    public NamePlaceDialogViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
        allItems = repository.getAll();
    }
}
