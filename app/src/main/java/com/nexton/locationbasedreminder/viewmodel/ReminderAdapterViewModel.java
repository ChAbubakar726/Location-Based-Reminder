package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.nexton.locationbasedreminder.model.ReminderWithNotePlacePlaceGroup;
import com.nexton.locationbasedreminder.repository.ReminderRepository;

public class ReminderAdapterViewModel extends AndroidViewModel {

    private ReminderRepository repository;

    public ReminderAdapterViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository(application);
    }

    public void setActive(ReminderWithNotePlacePlaceGroup reminder, boolean active) {
        repository.setActive(reminder, active);
    }
}
