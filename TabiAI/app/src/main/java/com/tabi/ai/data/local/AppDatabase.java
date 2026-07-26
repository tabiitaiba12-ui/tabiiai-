package com.tabi.ai.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.tabi.ai.utils.Constants;

/**
 * Singleton Room database holding the chat history table.
 */
@Database(entities = {ChatMessageEntity.class}, version = Constants.DATABASE_VERSION, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ChatMessageDao chatMessageDao();

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    Constants.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
