package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.nexton.locationbasedreminder.model.Note;
import com.nexton.locationbasedreminder.repository.NoteRepository;

public class NotesFragmentViewModel extends BaseViewModel<Note> {

    public NotesFragmentViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allItems = repository.getAll();
    }
}
