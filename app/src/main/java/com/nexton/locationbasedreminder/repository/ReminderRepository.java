package com.nexton.locationbasedreminder.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.nexton.locationbasedreminder.model.Reminder;
import com.nexton.locationbasedreminder.model.ReminderWithNotePlacePlaceGroup;
import com.nexton.locationbasedreminder.persistence.AppDatabase;
import com.nexton.locationbasedreminder.persistence.NoteDao;
import com.nexton.locationbasedreminder.persistence.ReminderDao;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReminderRepository extends BaseRepository {

    private AppDatabase database;

    private ReminderDao reminderDao;
    private NoteDao noteDao;

    private LiveData<List<ReminderWithNotePlacePlaceGroup>> allRemindersWithNotePlacePlaceGroup;
    private LiveData<List<ReminderWithNotePlacePlaceGroup>> activeReminders;

    private Executor executor;

    public ReminderRepository(Application application) {
        database = AppDatabase.getInstance(application);
        executor = Executors.newSingleThreadExecutor();

        noteDao = database.noteDao();
        reminderDao = database.reminderDao();

        allRemindersWithNotePlacePlaceGroup = reminderDao.getAllRemindersWithNotePlacePlaceGroup();
        activeReminders = reminderDao.getActiveReminders();
    }

    public void insert(ReminderWithNotePlacePlaceGroup reminderWithNotePlacePlaceGroup) {
        executor.execute(() ->
                database.runInTransaction(() -> {
                    long noteId;
                    Long placeId = null;
                    Long placeGroupId = null;

                    noteId = noteDao.insert(reminderWithNotePlacePlaceGroup.getNote());
                    if (reminderWithNotePlacePlaceGroup.getPlace() != null) {
                        placeId = reminderWithNotePlacePlaceGroup.getPlace().getPlaceId();
                    } else if (reminderWithNotePlacePlaceGroup.getPlaceGroupWithPlaces() != null) {
                        placeGroupId = reminderWithNotePlacePlaceGroup.getPlaceGroupWithPlaces().getPlaceGroup().getPlaceGroupId();
                    }
                    if (reminderWithNotePlacePlaceGroup.getReminder() == null) {
                        reminderWithNotePlacePlaceGroup.setReminder(new Reminder(noteId, placeId, placeGroupId, true));
                    }
                    reminderDao.insert(reminderWithNotePlacePlaceGroup.getReminder());
                })
        );
    }

    public void update(ReminderWithNotePlacePlaceGroup reminderWithNotePlacePlaceGroup) {
        executor.execute(() ->
                database.runInTransaction(() -> {
                    noteDao.update(reminderWithNotePlacePlaceGroup.getNote());
                    if (reminderWithNotePlacePlaceGroup.getPlace() != null) {
                        reminderWithNotePlacePlaceGroup.getReminder().setPlaceId(
                                reminderWithNotePlacePlaceGroup.getPlace().getPlaceId());
                    } else if (reminderWithNotePlacePlaceGroup.getPlaceGroupWithPlaces() != null) {
                        reminderWithNotePlacePlaceGroup.getReminder().setPlaceGroupId(
                                reminderWithNotePlacePlaceGroup.getPlaceGroupWithPlaces().getPlaceGroup().getPlaceGroupId());
                    }
                    reminderDao.update(reminderWithNotePlacePlaceGroup.getReminder());
                })
        );
    }

    public void delete(ReminderWithNotePlacePlaceGroup reminderWithNotePlacePlaceGroup) {
        executor.execute(() ->
                database.runInTransaction(() -> {
                    noteDao.delete(reminderWithNotePlacePlaceGroup.getNote());
                    reminderDao.delete(reminderWithNotePlacePlaceGroup.getReminder());
                })
        );
    }

    public void deleteAll() {
        executor.execute(() ->
                database.runInTransaction(() -> {
                    reminderDao.deleteAllReminderNotes();
                    reminderDao.deleteAllReminders();
                })
        );
    }

    public void setActive(ReminderWithNotePlacePlaceGroup reminderWithNotePlacePlaceGroup, boolean active) {
        setActive(reminderWithNotePlacePlaceGroup.getReminder().getReminderId(), active);
    }

    public void setActive(long reminderId, boolean active) {
        executor.execute(() ->
                reminderDao.setActive(reminderId, active)
        );
    }

    public int getActiveReminderCount() {
        return activeReminders.getValue() == null ? 0 : activeReminders.getValue().size();
    }

    public LiveData<List<ReminderWithNotePlacePlaceGroup>> getActiveReminders() {
        return activeReminders;
    }

    public LiveData<List<ReminderWithNotePlacePlaceGroup>> getAllRemindersWithNotePlacePlaceGroup() {
        return allRemindersWithNotePlacePlaceGroup;
    }
}
