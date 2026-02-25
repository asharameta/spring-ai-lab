package com.testingSpringAI.Controller;

import com.testingSpringAI.Model.Answer;
import com.testingSpringAI.Model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BarberClientController {
    private final ChatClient chatClientMCP;

    public BarberClientController(ChatClient chatClientMCP) {
        this.chatClientMCP = chatClientMCP;
    }

    @PostMapping("/chat")
    public Answer chat(@RequestBody Question question) {
        String response = chatClientMCP.prompt()
                .user(question.question())
                .call()
                .content();
        return new Answer(response);
    }
}
