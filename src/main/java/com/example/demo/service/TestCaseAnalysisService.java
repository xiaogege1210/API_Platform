package com.example.demo.service;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;


public interface TestCaseAnalysisService {
    //拼接提示词，调用ai
    //每次执行完后可以看到执行结果，
    public String generateAnalysisAndSuggestions(String api, String environment, String dependency, List<String> testname) throws JsonProcessingException;
}