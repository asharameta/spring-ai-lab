package com.MCPServer.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class BarberService {

    @Tool(description = "Say hello to a person by name. Returns a greeting message.")
    public String sayHello(
            @ToolParam(description = "The name of the person to greet") String name) {
        return "Hello, " + name + "! Welcome to the MCP world! 🌍";
    }
}
