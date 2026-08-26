package com.aihealth.healthchecker.ai;

import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.engine.RuleAssessmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CompositeAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(CompositeAiService.class);

    private final OpenAiCompatibleAiService openAiService;
    private final RuleBasedFallbackAiService fallbackService;

    public CompositeAiService(OpenAiCompatibleAiService openAiService,
                              RuleBasedFallbackAiService fallbackService) {
        this.openAiService = openAiService;
        this.fallbackService = fallbackService;
    }

    @Override
    public StructuredSymptomData extractSymptoms(String userInput) {
        if (openAiService.isConfigured()) {
            StructuredSymptomData result = openAiService.extractSymptoms(userInput);
            if (result != null && result.getSymptoms() != null && !result.getSymptoms().isEmpty()) {
                log.info("Successfully extracted symptoms via Remote LLM");
                return result;
            }
        }
        log.info("Using Rule-Based Fallback NLP extractor");
        return fallbackService.extractSymptoms(userInput);
    }

    @Override
    public String generateSafeExplanation(StructuredSymptomData symptomData, RuleAssessmentResult ruleResult) {
        if (openAiService.isConfigured()) {
            String explanation = openAiService.generateSafeExplanation(symptomData, ruleResult);
            if (explanation != null && !explanation.trim().isEmpty()) {
                log.info("Generated explanation via Remote LLM");
                return explanation;
            }
        }
        log.info("Using Rule-Based Fallback explanation generator");
        return fallbackService.generateSafeExplanation(symptomData, ruleResult);
    }

    @Override
    public ConversationResponse generateFollowUpResponse(ConversationRequest request) {
        if (openAiService.isConfigured()) {
            ConversationResponse response = openAiService.generateFollowUpResponse(request);
            if (response != null && response.getReply() != null) {
                return response;
            }
        }
        return fallbackService.generateFollowUpResponse(request);
    }
}
