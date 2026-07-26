package com.tabi.ai.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    long insert(ChatMessageEntity message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    LiveData<List<ChatMessageEntity>> getAllMessagesLive();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessageEntity> getAllMessages();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    List<ChatMessageEntity> getRecentMessages(int limit);

    @Query("DELETE FROM chat_messages")
    void clearAll();
}
