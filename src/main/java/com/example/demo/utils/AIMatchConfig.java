package com.example.demo.utils;

import com.example.demo.llm.LLMService;
import com.example.demo.utils.AIScenarioMatchUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIMatchConfig {

    @Bean
    public AIScenarioMatchUtils aiScenarioMatchUtils(LLMService llmService) {
        return new AIScenarioMatchUtils(llmService);
    }
}