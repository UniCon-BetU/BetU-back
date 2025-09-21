package org.example.gpt.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.gpt.service.MotivationService;
import org.example.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/motivation")
@RequiredArgsConstructor
public class MotivationController {

    private final UserService userService;
    private final MotivationService motivationService;

    /**
     * 예) GET /motivation/quote?tone=담백하게&topic=코딩면접
     */
    @GetMapping("/quote")
    public ResponseEntity<String> getDailyQuote(
            HttpServletRequest request,
            @RequestParam(required = false) String prompt
    ) {
        Long userId = userService.getUserIdFromToken(request);
        String quote = motivationService.getDailyQuote(prompt);
        return ResponseEntity.ok(quote);
    }
}
