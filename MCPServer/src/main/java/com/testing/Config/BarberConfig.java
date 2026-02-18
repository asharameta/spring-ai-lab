package com.testing.Config;

import com.testing.Service.BarberService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BarberConfig {
    @Bean
    public ToolCallbackProvider barberTools(BarberService barberService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(barberService)
                .build();
    }
}
