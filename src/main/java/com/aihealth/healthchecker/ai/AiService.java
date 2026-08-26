package com.aihealth.healthchecker.ai;

import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.engine.RuleAssessmentResult;

public interface AiService {

    StructuredSymptomData extractSymptoms(String userInput);

    String generateSafeExplanation(StructuredSymptomData symptomData, RuleAssessmentResult ruleResult);

    ConversationResponse generateFollowUpResponse(ConversationRequest request);
}
