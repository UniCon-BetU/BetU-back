package org.example.gpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MotivationService {

    private final GptService gptService;

    /** 동기부여 문구 생성 (GptService.generateQuote 위임) */
    public String getDailyQuote(String prompt) {
        return gptService.generateQuote(prompt);
    }
}