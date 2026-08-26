package com.aihealth.healthchecker.Service;

import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.SymptomAssessmentRequest;
import com.aihealth.healthchecker.DTO.SymptomAssessmentResponse;

import java.util.Map;

public interface HealthAssessmentService {

    SymptomAssessmentResponse assessSymptoms(SymptomAssessmentRequest request);

    ConversationResponse handleConversation(ConversationRequest request);

    Map<String, Object> getSupportedRulesMetadata();
}
