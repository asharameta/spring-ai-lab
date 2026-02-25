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
import org.springframework.ai.vectorstore.SearchRequest;
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
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.4).build()).build())
                .defaultSystem("""
                    You are a helpful assistant for a Barbershop.
                    
                    Opening hours: Monday-Friday 8:00-21:00 | Saturday-Sunday 08:00-15:00

                    IMPORTANT INSTRUCTIONS:
                    - Only answer what the user specifically asks about
                    - Be concise and relevant - don't list everything you know
                    - If asked about staff, only mention staff who can help with their specific needs
                    - If asked about services, only mention relevant services
                    - If pricing information is not in the context, ask for clarification rather than saying prices aren't available
                    - When the sayHello tool returns a greeting, return it exactly as provided

                    You have access to MCP tools and barbershop information. Use them wisely.
                    
                    If you don't have answer just say it, never send empty response back.
                    """)
                .build();
    }

    @Bean
    CommandLineRunner ingestDocuments(VectorStore vectorStore) {
        return args -> {
            try {
                System.out.println("Start process of reading documents into vector store...");

                PathMatchingResourcePatternResolver resolver =
                        new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath:/docs/*");

                System.out.println("Found " + resources.length + " files");  // ← ADD THIS

                if (resources.length == 0) {
                    System.err.println("⚠️ No documents found in /docs/ directory!");
                    return;
                }

                List<Document> allChunks = new ArrayList<>();
                TokenTextSplitter splitter = new TokenTextSplitter();

                for (Resource resource : resources) {
                    System.out.println("  currently splitting: " + resource.getFilename());
                    TikaDocumentReader reader = new TikaDocumentReader(resource);
                    List<Document> documents = reader.read();
                    List<Document> chunks = splitter.split(documents);
                    allChunks.addAll(chunks);
                }

                vectorStore.add(allChunks);
                System.out.println("✅ Parsed " + allChunks.size() + " chunks into vector store.");

            } catch (Exception e) {
                System.err.println("❌ Error loading documents: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

}
