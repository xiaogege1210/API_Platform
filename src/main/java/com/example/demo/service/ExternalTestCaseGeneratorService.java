package com.example.demo.service;

import com.example.demo.model.GenerateCodeRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExternalTestCaseGeneratorService {
    public String generateTestCase(List<GenerateCodeRequest> request);
}
