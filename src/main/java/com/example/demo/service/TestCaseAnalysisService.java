package com.example.demo.service;
import com.example.demo.dto.TestCaseResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface TestCaseAnalysisService {
    //拼接提示词，调用ai
    public String generateAnalysisAndSuggestions(String api, String test) throws JsonProcessingException;
    public String generateAnalysisAndSuggestionswiths(String api, String test);



    public String OptimizedScript(String test, TestCaseResultDto testResult) throws JsonProcessingException;
}