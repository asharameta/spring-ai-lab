package com.MCPClient.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BarberClientConfig {

    @Value("${spring.ai.openai.api-key}")
    String API_KEY;

    @Value("${spring.ai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.model}")
    private String model;

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(API_KEY)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(1.0)
                .topP(1.0)
                .maxCompletionTokens(1000)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("You are a helpful assistant in Barbershop. Monday - Friday: 8:00 - 21:00 | Saturday and Sunday: 08:00 - 15:00. You have access to tools " +
                        "provided by an MCP server. Use them when appropriate to answer " +
                        "the user's questions. Always be friendly and helpful.")
                .build();
    }

    @Bean
    public ChatClient chatClientMCP(OpenAiChatModel chatModel,
                                 ToolCallbackProvider tools){
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(tools.getToolCallbacks())
                .defaultSystem("You are a helpful assistant in Barbershop. Monday - Friday: 8:00 - 21:00 | Saturday and Sunday: 08:00 - 15:00. You have access to tools " +
                        "provided by an MCP server. Use them when appropriate to answer " +
                        "the user's questions. Always be friendly and helpful.")
                .build();
    }
}
