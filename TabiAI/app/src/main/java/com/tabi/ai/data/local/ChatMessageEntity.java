package com.tabi.ai.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a single chat message (either from the user or Tabi).
 */
@Entity(tableName = "chat_messages")
public class ChatMessageEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String message;

    /** 0 = user, 1 = AI. See {@link com.tabi.ai.utils.Constants#SENDER_USER}. */
    private int sender;

    private long timestamp;

    public ChatMessageEntity(@NonNull String message, int sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
