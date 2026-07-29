
package com.budgetwise.controller;

import com.budgetwise.service.GroqClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AITestController {

    private final GroqClient groqClient;

    public AITestController(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    @GetMapping("/ping")
    public String ping() {
        return "AI Controller Working";
    }

    @PostMapping("/test")
    public String test(@RequestParam String message) {
        return groqClient.generateContent(message);
    }
}