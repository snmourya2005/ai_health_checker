package com.aihealth.healthchecker.engine;

import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.DTO.TriageLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthRuleEngineTest {

    private HealthRuleEngine ruleEngine;
    private TriageEvaluator triageEvaluator;

    @BeforeEach
    void setUp() {
        triageEvaluator = new TriageEvaluator();
        ruleEngine = new HealthRuleEngine(triageEvaluator);
    }

    @Test
    @DisplayName("Should evaluate headache symptoms to Neurology and verified OTC remedies")
    void testHeadacheRuleEvaluation() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("headache"))
                .duration("1 day")
                .severityScore(3)
                .build();

        RuleAssessmentResult result = ruleEngine.evaluate(data);

        assertNotNull(result);
        assertEquals(TriageLevel.SELF_CARE_OTC, result.getTriageLevel());
        assertEquals("Neurology", result.getRecommendedSpecialty());
        assertTrue(result.getVerifiedOtcRemedies().stream().anyMatch(r -> r.contains("Paracetamol")));
        assertFalse(result.isRequiresImmediateCare());
    }

    @Test
    @DisplayName("Should evaluate cold and cough symptoms to ENT specialty")
    void testRespiratoryRuleEvaluation() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("cold", "cough"))
                .duration("2 days")
                .severityScore(4)
                .build();

        RuleAssessmentResult result = ruleEngine.evaluate(data);

        assertNotNull(result);
        assertEquals("ENT", result.getRecommendedSpecialty());
        assertTrue(result.getVerifiedOtcRemedies().stream().anyMatch(r -> r.contains("Cetirizine") || r.contains("Dextromethorphan")));
    }

    @Test
    @DisplayName("Should escalate to Emergency Red Flag when chest pain is detected")
    void testChestPainEmergencyRule() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("chest pain"))
                .duration("30 minutes")
                .severityScore(9)
                .isEmergency(true)
                .build();

        RuleAssessmentResult result = ruleEngine.evaluate(data);

        assertNotNull(result);
        assertEquals(TriageLevel.EMERGENCY_RED_FLAG, result.getTriageLevel());
        assertEquals("Cardiology", result.getRecommendedSpecialty());
        assertTrue(result.isRequiresImmediateCare());
        assertFalse(result.getEmergencyWarnings().isEmpty());
    }
}
