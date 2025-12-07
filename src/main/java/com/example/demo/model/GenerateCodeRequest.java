package com.example.demo.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GenerateCodeRequest {
    private String changjing;
    private String user_info;
    private String map_data;

    public GenerateCodeRequest() {}

    public GenerateCodeRequest(String changjing, String user_info, String map_data) {
        this.changjing = changjing;
        this.user_info = user_info;
        this.map_data = map_data;
    }

    // getter & setter
}