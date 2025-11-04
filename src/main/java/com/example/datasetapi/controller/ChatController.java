package com.example.datasetapi.controller;

import com.example.datasetapi.service.ChatService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    @Autowired private ChatService chatService;
    @PostMapping("/chat")
    public ChatResponse chat(@RequestParam String message){
        return chatService.response(message);
    }
}
