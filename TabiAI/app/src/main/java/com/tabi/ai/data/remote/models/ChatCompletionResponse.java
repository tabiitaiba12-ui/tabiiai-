package com.tabi.ai.data.remote.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response body for POST /v1/chat/completions
 */
public class ChatCompletionResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("error")
    private ApiError error;

    public List<Choice> getChoices() {
        return choices;
    }

    public ApiError getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

    /** Convenience accessor for the first reply's text content, or null. */
    public String firstMessageContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        OpenAiMessage message = choices.get(0).getMessage();
        return message == null ? null : message.getContent();
    }

    public static class Choice {
        @SerializedName("index")
        private int index;

        @SerializedName("message")
        private OpenAiMessage message;

        @SerializedName("finish_reason")
        private String finishReason;

        public OpenAiMessage getMessage() {
            return message;
        }
    }

    public static class ApiError {
        @SerializedName("message")
        private String message;

        @SerializedName("type")
        private String type;

        public String getMessage() {
            return message;
        }
    }
}
