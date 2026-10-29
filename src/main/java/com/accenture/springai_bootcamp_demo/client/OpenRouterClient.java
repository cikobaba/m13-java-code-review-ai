
package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenRouterClient {

    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String complete(List<ChatMessage> history) {
        String reply = call(history);

        if (!StringUtils.hasText(reply)) {
            throw new OpenRouterException(
                    "Ollama returned an empty response");
        }

        return reply.trim();
    }

    private String call(List<ChatMessage> history) {
        try {
            return chatClient.prompt()
                    .messages(toMessages(history))
                    .call()
                    .content();

        } catch (RuntimeException ex) {
            log.error("Ollama request failed", ex);
            throw new OpenRouterException(
                    "Failed to reach local Ollama model", ex);
        }
    }

    private List<Message> toMessages(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(
                "You are a helpful, concise assistant."));

        for (ChatMessage message : history) {
            switch (message.getRole()) {
                case SYSTEM -> messages.add(
                        new SystemMessage(message.getContent()));

                case USER -> messages.add(
                        new UserMessage(message.getContent()));

                case ASSISTANT -> messages.add(
                        new AssistantMessage(message.getContent()));
            }
        }

        return messages;
    }
}
