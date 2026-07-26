//package com.budgetwise.controller;
//
//import com.budgetwise.service.GeminiClient;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/ai")
//public class AITestController {
//
//    private final GeminiClient geminiClient;
//
//    public AITestController(GeminiClient geminiClient) {
//        this.geminiClient = geminiClient;
//    }
//    @GetMapping("/ping")
//    public String ping(){
//        return "Ai Controller working";
//
//
//    }
//
//    @PostMapping("/test")
//    public String test(@RequestParam String message) {
//        System.out.println("TEST endpoint reached: " + message);
//        return "Received: " + message;
//    }
//
//}
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