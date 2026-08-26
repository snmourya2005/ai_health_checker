package com.aihealth.healthchecker.ai;

import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.DTO.TriageLevel;
import com.aihealth.healthchecker.engine.RuleAssessmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("ruleBasedFallbackAiService")
public class RuleBasedFallbackAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedFallbackAiService.class);

    private static final Map<String, List<String>> SYMPTOM_LEXICON = Map.of(
            "headache", List.of("headache", "head ache", "migraine", "head throbbing", "pain in head"),
            "fever", List.of("fever", "high temperature", "chills", "febrile", "hot forehead"),
            "cold", List.of("cold", "runny nose", "sneezing", "congestion", "blocked nose", "sinus"),
            "cough", List.of("cough", "sore throat", "throat pain", "coughing", "dry cough", "wet cough"),
            "chest pain", List.of("chest pain", "chest pressure", "chest tightness", "pain in chest", "palpitations"),
            "bodypain", List.of("body pain", "body ache", "muscle ache", "joint pain", "back pain"),
            "weakness", List.of("weakness", "tired", "fatigue", "exhaustion", "low energy", "dizziness"),
            "stomach pain", List.of("stomach pain", "abdominal pain", "nausea", "vomiting", "diarrhea", "acidity", "indigestion"),
            "cuts", List.of("cut", "wound", "bleeding cut", "skin rash", "itching", "burn")
    );

    @Override
    public StructuredSymptomData extractSymptoms(String userInput) {
        log.debug("Executing Rule-Based Fallback NLP Extraction on input: {}", userInput);
        if (userInput == null || userInput.trim().isEmpty()) {
            return StructuredSymptomData.builder().build();
        }

        String lowerInput = userInput.toLowerCase(Locale.ROOT);
        List<String> matchedSymptoms = new ArrayList<>();
        List<String> emergencyFlags = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : SYMPTOM_LEXICON.entrySet()) {
            for (String alias : entry.getValue()) {
                if (lowerInput.contains(alias)) {
                    matchedSymptoms.add(entry.getKey());
                    break;
                }
            }
        }

        // Check for emergency keywords
        if (lowerInput.contains("chest pain") || lowerInput.contains("cannot breathe") || lowerInput.contains("shortness of breath") || lowerInput.contains("stroke") || lowerInput.contains("blood in cough")) {
            emergencyFlags.add("Critical symptom detected in natural language input");
        }

        // Parse severity
        int severity = 4; // default moderate
        if (lowerInput.contains("extreme") || lowerInput.contains("unbearable") || lowerInput.contains("excruciating") || lowerInput.contains("cannot bear")) {
            severity = 9;
        } else if (lowerInput.contains("severe") || lowerInput.contains("intense") || lowerInput.contains("high fever")) {
            severity = 7;
        } else if (lowerInput.contains("moderate") || lowerInput.contains("medium")) {
            severity = 5;
        } else if (lowerInput.contains("mild") || lowerInput.contains("slight") || lowerInput.contains("little bit") || lowerInput.contains("low")) {
            severity = 2;
        }

        // Regex check for numeric severity (e.g. 7/10 or "severity is 8")
        Pattern scorePattern = Pattern.compile("(\\b[1-9]|10)\\s*(/\\s*10|out of 10)?");
        Matcher scoreMatcher = scorePattern.matcher(lowerInput);
        if (scoreMatcher.find()) {
            try {
                severity = Integer.parseInt(scoreMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // Parse duration
        String duration = "unspecified duration";
        Pattern durationPattern = Pattern.compile("(since\\s+\\w+(\\s+\\w+)?|for\\s+\\d+\\s*(hours?|days?|weeks?|months?)|\\d+\\s*(hours?|days?|weeks?|months?)\\s*ago|yesterday|today)");
        Matcher durationMatcher = durationPattern.matcher(lowerInput);
        if (durationMatcher.find()) {
            duration = durationMatcher.group(0);
        }

        return StructuredSymptomData.builder()
                .symptoms(matchedSymptoms.isEmpty() ? List.of(userInput.substring(0, Math.min(30, userInput.length()))) : matchedSymptoms)
                .duration(duration)
                .severityScore(severity)
                .emergencyFlags(emergencyFlags)
                .isEmergency(!emergencyFlags.isEmpty() || severity >= 8)
                .build();
    }

    @Override
    public String generateSafeExplanation(StructuredSymptomData symptomData, RuleAssessmentResult ruleResult) {
        StringBuilder sb = new StringBuilder();

        if (ruleResult.getTriageLevel() == TriageLevel.EMERGENCY_RED_FLAG) {
            sb.append("⚠️ **URGENT SAFETY ADVISORY**: Based on the symptoms described (")
              .append(String.join(", ", symptomData.getSymptoms()))
              .append("), our safety engine has flagged potential high-risk indicators. ")
              .append("Please do not delay—seek immediate emergency medical care or call emergency services immediately.");
            return sb.toString();
        }

        sb.append("Based on the symptoms you reported (")
          .append(String.join(", ", symptomData.getSymptoms()))
          .append(" with duration '").append(symptomData.getDuration()).append("'), ");

        if (ruleResult.getTriageLevel() == TriageLevel.URGENT_MEDICAL_ATTENTION) {
            sb.append("your symptom intensity warrants prompt professional medical consultation with a **")
              .append(ruleResult.getRecommendedSpecialty())
              .append("** within the next 24 hours to prevent complications.");
        } else if (ruleResult.getTriageLevel() == TriageLevel.ROUTINE_CONSULTATION) {
            sb.append("we recommend scheduling a consultation with a **")
              .append(ruleResult.getRecommendedSpecialty())
              .append("** if your symptoms persist or do not improve.");
        } else {
            sb.append("these symptoms appear suitable for initial conservative home care and monitoring.");
        }

        return sb.toString();
    }

    @Override
    public ConversationResponse generateFollowUpResponse(ConversationRequest request) {
        String msg = request.getMessage().toLowerCase(Locale.ROOT);
        String reply;
        boolean emergency = false;

        if (msg.contains("emergency") || msg.contains("chest pain") || msg.contains("cannot breathe") || msg.contains("faint")) {
            reply = "⚠️ If you or the person are experiencing chest pain, difficulty breathing, or sudden weakness, please call 911 / emergency services or go to the nearest emergency room immediately.";
            emergency = true;
        } else if (msg.contains("doctor") || msg.contains("appointment") || msg.contains("specialist")) {
            reply = "You can browse certified doctors in our Doctors directory and book a consultation directly from the Doctors tab above.";
        } else if (msg.contains("medicine") || msg.contains("remedy") || msg.contains("dosage")) {
            reply = "For any medication or dosage questions, please consult a licensed pharmacist or physician before taking new medicines, especially if you have pre-existing health conditions or allergies.";
        } else {
            reply = "Thank you for sharing. Remember to stay hydrated, rest, and monitor your symptoms. If symptoms worsen or new concerning signs arise, please seek in-person medical attention.";
        }

        return ConversationResponse.builder()
                .reply(reply)
                .conversationId(request.getConversationId())
                .isEmergency(emergency)
                .build();
    }
}
