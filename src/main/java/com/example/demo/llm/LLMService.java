package com.example.demo.llm;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface LLMService {
    /**
     * 大模型提问
     * @param query
     * @return
     * @throws JsonProcessingException
     */
    String getMessage(String query) throws JsonProcessingException;

}