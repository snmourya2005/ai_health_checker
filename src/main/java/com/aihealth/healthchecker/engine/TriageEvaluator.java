package com.aihealth.healthchecker.engine;

import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.DTO.TriageLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TriageEvaluator {

    private static final List<String> CRITICAL_EMERGENCY_KEYWORDS = List.of(
            "chest pain", "heart attack", "crushing chest", "chest pressure",
            "shortness of breath", "difficulty breathing", "cannot breathe",
            "sudden numbness", "facial drooping", "slurred speech", "stroke",
            "coughing blood", "blood in vomit", "severe allergic reaction", "anaphylaxis",
            "stiff neck with fever", "seizure", "unconscious", "fainting"
    );

    public TriageLevel evaluateTriage(StructuredSymptomData symptomData, List<String> emergencyWarnings) {
        if (symptomData == null) {
            return TriageLevel.SELF_CARE_OTC;
        }

        // 1. Check explicit emergency flags or keyword matches
        List<String> detectedRedFlags = scanForEmergencyKeywords(symptomData);
        if (!detectedRedFlags.isEmpty() || symptomData.isEmergency()) {
            for (String flag : detectedRedFlags) {
                emergencyWarnings.add("CRITICAL WARNING: Potential emergency symptom detected (" + flag + "). Seek immediate emergency medical care (ER or call emergency services).");
            }
            return TriageLevel.EMERGENCY_RED_FLAG;
        }

        // 2. Evaluate Severity Score
        int score = symptomData.getSeverityScore();
        if (score >= 8) {
            emergencyWarnings.add("High symptom severity reported (Level " + score + "/10). Prompt medical evaluation is required.");
            return TriageLevel.EMERGENCY_RED_FLAG;
        } else if (score >= 6) {
            return TriageLevel.URGENT_MEDICAL_ATTENTION;
        } else if (score >= 4) {
            return TriageLevel.ROUTINE_CONSULTATION;
        }

        // 3. Duration-based Escalation
        String duration = symptomData.getDuration().toLowerCase(Locale.ROOT);
        if (duration.contains("week") || duration.contains("month") || duration.contains("persistent") || duration.contains("10 day")) {
            return TriageLevel.ROUTINE_CONSULTATION;
        }

        return TriageLevel.SELF_CARE_OTC;
    }

    private List<String> scanForEmergencyKeywords(StructuredSymptomData data) {
        List<String> matched = new ArrayList<>();
        if (data.getSymptoms() == null) return matched;

        for (String symptom : data.getSymptoms()) {
            String lower = symptom.toLowerCase(Locale.ROOT);
            for (String critical : CRITICAL_EMERGENCY_KEYWORDS) {
                if (lower.contains(critical)) {
                    matched.add(symptom);
                    break;
                }
            }
        }

        if (data.getEmergencyFlags() != null) {
            matched.addAll(data.getEmergencyFlags());
        }

        return matched;
    }
}
