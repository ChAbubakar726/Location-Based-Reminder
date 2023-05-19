package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.nexton.locationbasedreminder.model.Note;
import com.nexton.locationbasedreminder.repository.NoteRepository;

public class AddEditNoteFragmentViewModel extends BaseViewModel<Note> {

    public AddEditNoteFragmentViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allItems = repository.getAll();
    }
}
