package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ChatMessage;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService){this.chatMessageService=chatMessageService;}

    @PostMapping("/saveMessage")
    public CommonResponse<ChatMessage> saveMessage(@RequestBody ChatMessage chatMessage) throws UnexpectedServerException {
        return chatMessageService.saveMessage(chatMessage);
    }

    @GetMapping("/getChatHistory/{senderId}/{recipientId}")
    public CommonResponse<List<ChatMessage>> getChatHistory(@PathVariable Long senderId, @PathVariable Long recipientId) throws UnexpectedServerException {
        return chatMessageService.getChatHistory(senderId, recipientId);
    }

    @GetMapping("/getUnreadMessages/{senderId}/{recipientId}")
    public CommonResponse<List<ChatMessage>> getUnreadMessages(@PathVariable Long senderId, @PathVariable Long recipientId) throws UnexpectedServerException {
        return chatMessageService.getUnreadMessages(senderId, recipientId);
    }

    @PutMapping("/markMessageAsRead/{senderId}/{recipientId}")
    public CommonResponse<String> markMessagesAsRead(@PathVariable Long senderId, @PathVariable Long recipientId) throws UnexpectedServerException {
        return chatMessageService.markMessagesAsRead(senderId, recipientId);
    }
}
