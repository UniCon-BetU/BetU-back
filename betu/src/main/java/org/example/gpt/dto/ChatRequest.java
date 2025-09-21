package org.example.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    private Integer max_tokens;
}
