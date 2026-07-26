package com.tabi.ai.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tabi.ai.data.local.ChatMessageEntity;
import com.tabi.ai.data.repository.ChatRepository;
import com.tabi.ai.utils.Constants;

import java.util.List;

/**
 * MVVM ViewModel mediating between the UI (MainActivity) and the
 * ChatRepository. Owns UI state such as "is Tabi thinking" and the
 * last error message, both exposed as LiveData.
 */
public class MainViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final LiveData<List<ChatMessageEntity>> chatHistory;

    private final MutableLiveData<Boolean> isThinking = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> lastAiReply = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        chatHistory = chatRepository.getAllMessagesLive();
    }

    public LiveData<List<ChatMessageEntity>> getChatHistory() {
        return chatHistory;
    }

    public LiveData<Boolean> getIsThinking() {
        return isThinking;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getLastAiReply() {
        return lastAiReply;
    }

    public void saveUserMessage(String text) {
        chatRepository.saveMessage(text, Constants.SENDER_USER);
    }

    public void saveAiMessage(String text) {
        chatRepository.saveMessage(text, Constants.SENDER_AI);
    }

    public void clearHistory() {
        chatRepository.clearHistory();
    }

    /** Sends the message to OpenAI and persists both the user prompt and the AI reply. */
    public void sendToAi(String userMessage) {
        isThinking.postValue(true);
        chatRepository.sendMessageToAi(userMessage, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(String aiReply) {
                isThinking.postValue(false);
                saveAiMessage(aiReply);
                lastAiReply.postValue(aiReply);
            }

            @Override
            public void onError(String errorMsg) {
                isThinking.postValue(false);
                errorMessage.postValue(errorMsg);
            }
        });
    }
}
