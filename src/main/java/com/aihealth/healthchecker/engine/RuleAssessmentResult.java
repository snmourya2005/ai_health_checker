package com.aihealth.healthchecker.engine;

import com.aihealth.healthchecker.DTO.TriageLevel;
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
public class RuleAssessmentResult {

    private TriageLevel triageLevel;

    private String primaryConditionCategory;

    private String recommendedSpecialty;

    @Builder.Default
    private List<String> verifiedOtcRemedies = new ArrayList<>();

    @Builder.Default
    private List<String> clinicalAdvice = new ArrayList<>();

    @Builder.Default
    private List<String> emergencyWarnings = new ArrayList<>();

    @Builder.Default
    private List<String> potentialRiskFactors = new ArrayList<>();

    private boolean requiresImmediateCare;

    private String clinicalSummary;
}
