package com.aihealth.healthchecker.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {

    private String conversationId;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    @Builder.Default
    private List<ConversationMessage> history = new ArrayList<>();
}
