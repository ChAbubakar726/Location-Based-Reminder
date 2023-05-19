package com.nexton.locationbasedreminder.repository;

import android.app.Application;

import com.nexton.locationbasedreminder.model.Note;
import com.nexton.locationbasedreminder.persistence.AppDatabase;

public class NoteRepository extends BaseRepository<Note>{

    public NoteRepository(Application application) {
        super();
        AppDatabase database = AppDatabase.getInstance(application);
        dao = database.noteDao();
        allItems = dao.getAll();
    }
}
