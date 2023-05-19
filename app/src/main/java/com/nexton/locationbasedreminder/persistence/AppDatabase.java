package com.nexton.locationbasedreminder.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.nexton.locationbasedreminder.model.AlarmAttribute;
import com.nexton.locationbasedreminder.model.Note;
import com.nexton.locationbasedreminder.model.Place;
import com.nexton.locationbasedreminder.model.PlaceGroup;
import com.nexton.locationbasedreminder.model.PlaceGroupPlaceCrossRef;
import com.nexton.locationbasedreminder.model.Reminder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {
        Reminder.class, Note.class, AlarmAttribute.class,
        Place.class, PlaceGroup.class, PlaceGroupPlaceCrossRef.class
}, version = 20)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ReminderDao reminderDao();

    public abstract NoteDao noteDao();

    public abstract PlaceDao placeDao();

    public abstract PlaceGroupDao placeGroupDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "loclarm_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallBack)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallBack = new RoomDatabase.Callback() {
//        @Override
//        public void onOpen(@NonNull SupportSQLiteDatabase db) {
//            super.onOpen(db);
//            onCreate(db);
//        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
            });
        }
    };
}
