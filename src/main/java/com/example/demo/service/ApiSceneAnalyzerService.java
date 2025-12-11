package com.example.demo.service;

import java.util.List;

public interface ApiSceneAnalyzerService {
    List<String> analyze(String apiDoc,String extraScene);
}
