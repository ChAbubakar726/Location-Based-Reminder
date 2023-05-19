package com.nexton.locationbasedreminder.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nexton.locationbasedreminder.model.ReminderWithNotePlacePlaceGroup;
import com.nexton.locationbasedreminder.repository.ReminderRepository;

import java.util.List;

public class RemindersFragmentViewModel extends AndroidViewModel {

    private ReminderRepository repository;
    private LiveData<List<ReminderWithNotePlacePlaceGroup>> allReminders;

    public RemindersFragmentViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository(application);
        allReminders = repository.getAllRemindersWithNotePlacePlaceGroup();
    }

    public void insert(ReminderWithNotePlacePlaceGroup reminder) {
        repository.insert(reminder);
    }

    public void delete(ReminderWithNotePlacePlaceGroup reminder) {
        repository.delete(reminder);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<List<ReminderWithNotePlacePlaceGroup>> getAllReminders() {
        return allReminders;
    }
}
