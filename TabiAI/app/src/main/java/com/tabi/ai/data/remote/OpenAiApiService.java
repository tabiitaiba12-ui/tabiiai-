package com.tabi.ai.data.remote;

import com.tabi.ai.data.remote.models.ChatCompletionRequest;
import com.tabi.ai.data.remote.models.ChatCompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Retrofit interface describing the OpenAI Chat Completions endpoint.
 */
public interface OpenAiApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<ChatCompletionResponse> getChatCompletion(
            @Header("Authorization") String bearerToken,
            @Body ChatCompletionRequest request
    );
}
