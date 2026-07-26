package com.tabi.ai.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.tabi.ai.data.local.AppDatabase;
import com.tabi.ai.data.local.ChatMessageDao;
import com.tabi.ai.data.local.ChatMessageEntity;
import com.tabi.ai.data.remote.OpenAiApiService;
import com.tabi.ai.data.remote.RetrofitClient;
import com.tabi.ai.data.remote.models.ChatCompletionRequest;
import com.tabi.ai.data.remote.models.ChatCompletionResponse;
import com.tabi.ai.data.remote.models.OpenAiMessage;
import com.tabi.ai.utils.Constants;
import com.tabi.ai.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single source of truth for chat data: persists history in Room and
 * talks to the OpenAI Chat Completions API for new AI replies.
 */
public class ChatRepository {

    /** Callback used to deliver an async AI response back to the ViewModel. */
    public interface ChatCallback {
        void onSuccess(String aiReply);
        void onError(String errorMessage);
    }

    private final ChatMessageDao chatMessageDao;
    private final OpenAiApiService openAiApiService;
    private final ExecutorService executorService;
    private final Context appContext;

    public ChatRepository(Context context) {
        this.appContext = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(appContext);
        this.chatMessageDao = db.chatMessageDao();
        this.openAiApiService = RetrofitClient.getOpenAiService();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<ChatMessageEntity>> getAllMessagesLive() {
        return chatMessageDao.getAllMessagesLive();
    }

    public void saveMessage(String text, int sender) {
        executorService.execute(() -> {
            ChatMessageEntity entity = new ChatMessageEntity(text, sender, System.currentTimeMillis());
            chatMessageDao.insert(entity);
        });
    }

    public void clearHistory() {
        executorService.execute(chatMessageDao::clearAll);
    }

    /**
     * Sends the conversation (last N messages for context) plus the new user
     * message to OpenAI and returns the reply through the callback.
     */
    public void sendMessageToAi(String userMessage, ChatCallback callback) {
        executorService.execute(() -> {
            String apiKey = resolveApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                callback.onError("Missing OpenAI API key. Please add it in Settings.");
                return;
            }

            List<ChatMessageEntity> recentHistory = chatMessageDao.getRecentMessages(10);

            List<OpenAiMessage> messages = new ArrayList<>();
            messages.add(new OpenAiMessage("system", Constants.SYSTEM_PROMPT));

            // recentHistory comes back newest-first; reverse for chronological order
            for (int i = recentHistory.size() - 1; i >= 0; i--) {
                ChatMessageEntity entity = recentHistory.get(i);
                String role = entity.getSender() == Constants.SENDER_USER ? "user" : "assistant";
                messages.add(new OpenAiMessage(role, entity.getMessage()));
            }
            messages.add(new OpenAiMessage("user", userMessage));

            ChatCompletionRequest request = new ChatCompletionRequest(
                    Constants.OPENAI_CHAT_MODEL, messages, 0.7, 350);

            openAiApiService.getChatCompletion("Bearer " + apiKey, request)
                    .enqueue(new Callback<ChatCompletionResponse>() {
                        @Override
                        public void onResponse(Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ChatCompletionResponse body = response.body();
                                if (body.hasError()) {
                                    callback.onError(body.getError().getMessage());
                                    return;
                                }
                                String reply = body.firstMessageContent();
                                if (reply == null || reply.trim().isEmpty()) {
                                    callback.onError("Tabi didn't return a response. Please try again.");
                                } else {
                                    callback.onSuccess(reply.trim());
                                }
                            } else {
                                callback.onError("OpenAI request failed (HTTP " + response.code() + ")");
                            }
                        }

                        @Override
                        public void onFailure(Call<ChatCompletionResponse> call, Throwable t) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    });
        });
    }

    private String resolveApiKey() {
        String userSuppliedKey = PreferenceHelper.getOpenAiKey(appContext);
        if (userSuppliedKey != null && !userSuppliedKey.trim().isEmpty()) {
            return userSuppliedKey.trim();
        }
        String buildConfigKey = com.tabi.ai.BuildConfig.OPENAI_API_KEY;
        return buildConfigKey == null ? "" : buildConfigKey.trim();
    }
}
