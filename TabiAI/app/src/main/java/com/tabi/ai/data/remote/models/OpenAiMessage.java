package com.tabi.ai.data.remote.models;

import com.google.gson.annotations.SerializedName;

/**
 * A single message in the OpenAI Chat Completions "messages" array.
 */
public class OpenAiMessage {

    @SerializedName("role")
    private String role; // "system" | "user" | "assistant"

    @SerializedName("content")
    private String content;

    public OpenAiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
