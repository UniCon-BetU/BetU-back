package org.example.gpt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ChatResponse {
    private List<ChatChoice> choices;

    @Getter
    public static class ChatChoice {
        private ChatMessage message;
    }
}
