package com.nexton.locationbasedreminder.repository;

import android.app.Application;

import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.persistence.AppDatabase;

public class PlaceRepository extends BaseRepository<Place> {

    public PlaceRepository(Application application) {
        super();
        AppDatabase database = AppDatabase.getInstance(application);
        dao = database.placeDao();
        allItems = dao.getAll();
    }
}
