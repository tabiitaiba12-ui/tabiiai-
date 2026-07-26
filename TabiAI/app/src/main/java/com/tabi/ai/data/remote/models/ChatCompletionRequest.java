package com.tabi.ai.data.remote.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request body for POST /v1/chat/completions
 */
public class ChatCompletionRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<OpenAiMessage> messages;

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("max_tokens")
    private int maxTokens;

    public ChatCompletionRequest(String model, List<OpenAiMessage> messages, double temperature, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public String getModel() {
        return model;
    }

    public List<OpenAiMessage> getMessages() {
        return messages;
    }
}
