package com.testingSpringAI.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

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
    public VectorStore vectorStore(EmbeddingModel embeddingModel){
        return SimpleVectorStore.builder(embeddingModel).build();
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
    public ChatClient chatClientMCP(OpenAiChatModel chatModel,
                                    ToolCallbackProvider tools,
                                    VectorStore vectorStore){
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(tools.getToolCallbacks())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .defaultSystem("You are a helpful assistant in Barbershop. When the sayHello tool returns a greeting, return it exactly as provided without adding anything. Monday - Friday: 8:00 - 21:00 | Saturday and Sunday: 08:00 - 15:00. You have access to tools " +
                        "provided by an MCP server. Use them when appropriate to answer " +
                        "the user's questions. Always be friendly and helpful.")
                .build();
    }



    @Bean
    CommandLineRunner ingestDocuments(VectorStore vectorStore) {
        return args -> {
            System.out.println("Start process of reading documents into vector store...");

            // Find all files in the docs directory
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/docs/*");

            // Read and split each document
            List<Document> allChunks = new ArrayList<>();
            TokenTextSplitter splitter = new TokenTextSplitter();

            for (Resource resource : resources) {
                System.out.println("  currently splitting: " + resource.getFilename());
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.read();
                List<Document> chunks = splitter.split(documents);
                allChunks.addAll(chunks);
            }

            // Add all chunks to the vector store (this embeds them via OpenAI)
            vectorStore.add(allChunks);
            System.out.println("Parsed " + allChunks.size() + " chunks into vector store.");
        };
    }

}
