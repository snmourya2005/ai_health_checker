package com.aihealth.healthchecker.ai;

import com.aihealth.healthchecker.DTO.ConversationMessage;
import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.engine.RuleAssessmentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service("openAiCompatibleAiService")
public class OpenAiCompatibleAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiService.class);

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equalsIgnoreCase("none");
    }

    @Override
    public StructuredSymptomData extractSymptoms(String userInput) {
        if (!isConfigured()) {
            return null;
        }

        try {
            String systemPrompt = """
                You are a medical NLP parser. Extract structured symptom details from user input.
                Return ONLY valid JSON in this exact structure:
                {
                  "symptoms": ["symptom1", "symptom2"],
                  "duration": "duration text (e.g. 2 days)",
                  "severityScore": 1-10 number,
                  "emergencyFlags": ["flag1 if critical"],
                  "isEmergency": true/false,
                  "bodyParts": ["head", "chest"]
                }
                Do not include markdown fences or any other text.
                """;

            String responseContent = callLlm(systemPrompt, userInput);
            if (responseContent == null) return null;

            // Strip any accidental markdown formatting
            String json = responseContent.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = objectMapper.readTree(json);

            List<String> symptoms = new ArrayList<>();
            if (node.has("symptoms") && node.get("symptoms").isArray()) {
                node.get("symptoms").forEach(s -> symptoms.add(s.asText()));
            }

            List<String> flags = new ArrayList<>();
            if (node.has("emergencyFlags") && node.get("emergencyFlags").isArray()) {
                node.get("emergencyFlags").forEach(f -> flags.add(f.asText()));
            }

            return StructuredSymptomData.builder()
                    .symptoms(symptoms.isEmpty() ? List.of(userInput) : symptoms)
                    .duration(node.has("duration") ? node.get("duration").asText("unspecified") : "unspecified")
                    .severityScore(node.has("severityScore") ? node.get("severityScore").asInt(4) : 4)
                    .emergencyFlags(flags)
                    .isEmergency(node.has("isEmergency") && node.get("isEmergency").asBoolean(false))
                    .build();
        } catch (Exception e) {
            log.warn("Remote AI symptom extraction failed or timed out. Falling back to local engine. Error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String generateSafeExplanation(StructuredSymptomData symptomData, RuleAssessmentResult ruleResult) {
        if (!isConfigured()) {
            return null;
        }

        try {
            String systemPrompt = """
                You are a compassionate, medically safe AI healthcare triage assistant.
                You are explaining the findings of a deterministic clinical rule engine to a patient.
                RULES:
                1. NEVER make a definitive disease diagnosis. State 'possible health factors' or 'symptom evaluation'.
                2. Explain in simple, clear, user-friendly language.
                3. Highlight the rule engine's recommended specialist and conservative care options.
                4. Include a clear safety disclaimer.
                """;

            String userPrompt = String.format(
                    "Patient Symptoms: %s\nReported Duration: %s\nCalculated Triage Level: %s\nRecommended Specialty: %s\nVerified OTC Guidance: %s\nClinical Advice: %s\nEmergency Warnings: %s",
                    symptomData.getSymptoms(),
                    symptomData.getDuration(),
                    ruleResult.getTriageLevel(),
                    ruleResult.getRecommendedSpecialty(),
                    ruleResult.getVerifiedOtcRemedies(),
                    ruleResult.getClinicalAdvice(),
                    ruleResult.getEmergencyWarnings()
            );

            return callLlm(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.warn("Remote AI explanation generation failed. Falling back. Error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ConversationResponse generateFollowUpResponse(ConversationRequest request) {
        if (!isConfigured()) {
            return null;
        }

        try {
            String systemPrompt = """
                You are an empathetic, safe healthcare triage assistant.
                Provide safe, educational answers. If emergency symptoms arise, immediately tell the user to seek emergency medical care. Never prescribe or give confident diagnoses.
                """;

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            if (request.getHistory() != null) {
                for (ConversationMessage msg : request.getHistory()) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }
            messages.add(Map.of("role", "user", "content", request.getMessage()));

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.3
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return ConversationResponse.builder()
                        .reply(content)
                        .conversationId(request.getConversationId())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Remote AI conversation failed: {}", e.getMessage());
        }
        return null;
    }

    private String callLlm(String systemPrompt, String userPrompt) throws Exception {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        }
        return null;
    }
}
