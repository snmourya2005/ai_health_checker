package com.aihealth.healthchecker.DTO;

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
public class SymptomAssessmentResponse {

    private TriageLevel triageLevel;

    private String summary;

    @Builder.Default
    private List<String> detectedSymptoms = new ArrayList<>();

    private String duration;

    private int severityScore;

    private String riskLevelDescription;

    private String recommendedSpecialty;

    @Builder.Default
    private List<String> otcRemedies = new ArrayList<>();

    @Builder.Default
    private List<String> lifestyleAdvice = new ArrayList<>();

    @Builder.Default
    private List<String> emergencyWarnings = new ArrayList<>();

    private String safeExplanation;

    @Builder.Default
    private List<String> followUpQuestions = new ArrayList<>();

    private String disclaimer;

    private boolean requiresImmediateCare;

    private String conversationId;
}
