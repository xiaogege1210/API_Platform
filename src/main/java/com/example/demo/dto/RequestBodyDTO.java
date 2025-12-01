package com.example.demo.dto;

public class RequestBodyDTO {
    private String model;
    //模型
    private MessageDTO[] messages;
    //信息
    private int max_tokens;
    //最大token数
    private double temperature;
    private boolean stream;

    // Getter & Setter
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public MessageDTO[] getMessages() { return messages; }
    public void setMessages(MessageDTO[] messages) { this.messages = messages; }
    public int getMax_tokens() { return max_tokens; }
    public void setMax_tokens(int max_tokens) { this.max_tokens = max_tokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
}
