package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ChatMessage;
import com.clinicalpsychology.app.repository.ChatMessageRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository){this.chatMessageRepository=chatMessageRepository;}

    public CommonResponse<ChatMessage> saveMessage(ChatMessage chatMessage) throws UnexpectedServerException {

        try {

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            return CommonResponse.<ChatMessage>builder()
                    .message(SUCCESSFULLY_ADDED)
                    .status(STATUS_TRUE)
                    .data(savedMessage)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_ADDING_CHAT_MESSAGE + e.getMessage());
        }

    }

    public CommonResponse<List<ChatMessage>> getChatHistory(Long senderId, Long recipientId) throws UnexpectedServerException {

        try {

            List<ChatMessage> chatMessages = chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);

            if(chatMessages.isEmpty()){
                return CommonResponse.<List<ChatMessage>>builder()
                        .message(NO_CHAT_MESSAGES_AVAILABLE)
                        .status(STATUS_TRUE)
                        .data(chatMessages)
                        .statusCode(SUCCESS_CODE)
                        .build();
            }

            return CommonResponse.<List<ChatMessage>>builder()
                    .message(LOADED_ALL_CHAT_MESSAGES)
                    .status(STATUS_TRUE)
                    .data(chatMessages)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_FETCHING_CHAT_MESSAGES + e.getMessage());
        }
    }

    public CommonResponse<List<ChatMessage>> getUnreadMessages(Long senderId, Long recipientId) throws UnexpectedServerException {

        try {

            List<ChatMessage> chatMessages = chatMessageRepository.findBySenderIdAndRecipientIdAndRead(senderId, recipientId, false);

            if(chatMessages.isEmpty()){
                return CommonResponse.<List<ChatMessage>>builder()
                        .message(NO_UNREAD_MESSAGES_AVAILABLE)
                        .status(STATUS_TRUE)
                        .data(chatMessages)
                        .statusCode(SUCCESS_CODE)
                        .build();
            }

            return CommonResponse.<List<ChatMessage>>builder()
                    .message(LOADED_ALL_UNREAD_MESSAGES)
                    .status(STATUS_TRUE)
                    .data(chatMessages)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_FETCHING_UNREAD_MESSAGES + e.getMessage());
        }
    }

    public CommonResponse<String> markMessagesAsRead(Long senderId, Long recipientId) throws UnexpectedServerException {
        try {
            int updatedCount = chatMessageRepository.markAllAsRead(senderId, recipientId);

            String message = (updatedCount == 0) ? NO_CHAT_UNREAD_MESSAGES_AVAILABLE : MESSAGES_MARKED_AS_READ;

            return CommonResponse.<String>builder()
                    .message(message)
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_MARKING_MESSAGES_AS_READ + e.getMessage());
        }
    }


}

