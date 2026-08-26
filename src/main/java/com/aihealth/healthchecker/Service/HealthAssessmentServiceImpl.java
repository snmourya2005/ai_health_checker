package com.aihealth.healthchecker.Service;

import com.aihealth.healthchecker.DTO.*;
import com.aihealth.healthchecker.ai.AiService;
import com.aihealth.healthchecker.engine.HealthRuleEngine;
import com.aihealth.healthchecker.engine.RuleAssessmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HealthAssessmentServiceImpl implements HealthAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(HealthAssessmentServiceImpl.class);

    private static final String MEDICAL_DISCLAIMER =
            "DISCLAIMER: This assessment is an automated, AI-assisted health triage guide and does NOT constitute a formal medical diagnosis or prescription. If you are experiencing an emergency or symptoms worsen, contact emergency medical services or consult a licensed healthcare practitioner immediately.";

    private final AiService aiService;
    private final HealthRuleEngine healthRuleEngine;

    public HealthAssessmentServiceImpl(AiService aiService, HealthRuleEngine healthRuleEngine) {
        this.aiService = aiService;
        this.healthRuleEngine = healthRuleEngine;
    }

    @Override
    public SymptomAssessmentResponse assessSymptoms(SymptomAssessmentRequest request) {
        log.info("Starting health assessment for user input length: {}", request.getUserInput().length());

        String convId = request.getConversationId() != null && !request.getConversationId().trim().isEmpty()
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        // 1. AI/NLP Symptom Extraction
        StructuredSymptomData symptomData = aiService.extractSymptoms(request.getUserInput());
        log.info("Extracted symptoms: {}, severity: {}, duration: {}",
                symptomData.getSymptoms(), symptomData.getSeverityScore(), symptomData.getDuration());

        // 2. Deterministic Java Rule Engine & Triage Evaluation
        RuleAssessmentResult ruleResult = healthRuleEngine.evaluate(symptomData);
        log.info("Rule evaluation complete. Triage: {}, Specialty: {}",
                ruleResult.getTriageLevel(), ruleResult.getRecommendedSpecialty());

        // 3. AI Safe Explanation Generation
        String safeExplanation = aiService.generateSafeExplanation(symptomData, ruleResult);

        // 4. Generate helpful contextual follow-up questions
        List<String> followUps = generateFollowUpQuestions(symptomData, ruleResult);

        // 5. Build and return structured response
        return SymptomAssessmentResponse.builder()
                .triageLevel(ruleResult.getTriageLevel())
                .summary(ruleResult.getClinicalSummary())
                .detectedSymptoms(symptomData.getSymptoms())
                .duration(symptomData.getDuration())
                .severityScore(symptomData.getSeverityScore())
                .riskLevelDescription(ruleResult.getTriageLevel().getDescription())
                .recommendedSpecialty(ruleResult.getRecommendedSpecialty())
                .otcRemedies(ruleResult.getVerifiedOtcRemedies())
                .lifestyleAdvice(ruleResult.getClinicalAdvice())
                .emergencyWarnings(ruleResult.getEmergencyWarnings())
                .safeExplanation(safeExplanation)
                .followUpQuestions(followUps)
                .disclaimer(MEDICAL_DISCLAIMER)
                .requiresImmediateCare(ruleResult.isRequiresImmediateCare())
                .conversationId(convId)
                .build();
    }

    @Override
    public ConversationResponse handleConversation(ConversationRequest request) {
        log.info("Processing conversation message for convId: {}", request.getConversationId());
        String convId = request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString();
        request.setConversationId(convId);
        return aiService.generateFollowUpResponse(request);
    }

    @Override
    public Map<String, Object> getSupportedRulesMetadata() {
        return Map.of(
                "supportedSpecialties", List.of("Cardiology", "Neurology", "ENT", "General Physician", "Dermatology", "Gastroenterology"),
                "triageLevels", List.of(TriageLevel.values()),
                "disclaimer", MEDICAL_DISCLAIMER
        );
    }

    private List<String> generateFollowUpQuestions(StructuredSymptomData data, RuleAssessmentResult ruleResult) {
        List<String> questions = new ArrayList<>();
        if (data.getSymptoms().contains("fever")) {
            questions.add("Have you measured your exact body temperature with a thermometer?");
        }
        if (data.getSymptoms().contains("headache")) {
            questions.add("Are you experiencing any sensitivity to light or nausea with the headache?");
        }
        if (data.getSymptoms().contains("cough")) {
            questions.add("Is your cough dry or producing phlegm/mucus?");
        }
        if (data.getSeverityScore() >= 6) {
            questions.add("Would you like to review available doctors in " + ruleResult.getRecommendedSpecialty() + " to book an appointment?");
        }
        if (questions.isEmpty()) {
            questions.add("Do you have any known medical conditions or drug allergies?");
        }
        return questions;
    }
}
