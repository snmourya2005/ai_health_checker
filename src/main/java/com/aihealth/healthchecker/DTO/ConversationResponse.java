package com.aihealth.healthchecker.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String reply;
    private String conversationId;
    private boolean isEmergency;
    private String suggestedAction;
}
