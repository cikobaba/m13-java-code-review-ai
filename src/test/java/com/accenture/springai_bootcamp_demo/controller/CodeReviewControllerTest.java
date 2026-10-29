package com.accenture.springai_bootcamp_demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CodeReviewControllerTest {

    private CodeReviewController controller;

    @BeforeEach
    void setUp() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient =
                mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);

        when(builder.build()).thenReturn(chatClient);

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(
                        "Potential bug detected",
                        "Use equals() for String comparison"
                );

        controller = new CodeReviewController(builder);
    }

    @Test
    void shouldDetectStringComparisonIssue() {
        String code = """
                public class Test {
                    public boolean check(String name) {
                        return name == "Admin";
                    }
                }
                """;

        var result = controller.review(
                new CodeReviewController.ReviewRequest(code));

        assertTrue(result.staticChecks().stream()
                .anyMatch(check -> check.contains("equals()")));

        assertEquals("Potential bug detected",
                result.bugReport());

        assertEquals("Use equals() for String comparison",
                result.improvements());
    }

    @Test
    void shouldDetectConsolePrinting() {
        String code = """
                public class Test {
                    public void hello() {
                        System.out.println("Hello");
                    }
                }
                """;

        var result = controller.review(
                new CodeReviewController.ReviewRequest(code));

        assertTrue(result.staticChecks().stream()
                .anyMatch(check -> check.contains("console printing")));

        assertEquals(2, result.staticChecks().size());
    }
}
