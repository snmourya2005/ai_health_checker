package com.aihealth.healthchecker.engine;

import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.DTO.TriageLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriageEvaluatorTest {

    private TriageEvaluator triageEvaluator;

    @BeforeEach
    void setUp() {
        triageEvaluator = new TriageEvaluator();
    }

    @Test
    @DisplayName("Should detect emergency for severe shortness of breath")
    void testShortnessOfBreathEmergency() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("shortness of breath", "fever"))
                .duration("1 hour")
                .severityScore(7)
                .build();

        List<String> warnings = new ArrayList<>();
        TriageLevel level = triageEvaluator.evaluateTriage(data, warnings);

        assertEquals(TriageLevel.EMERGENCY_RED_FLAG, level);
        assertFalse(warnings.isEmpty());
    }

    @Test
    @DisplayName("Should categorize mild symptoms as Self-Care OTC")
    void testMildSymptomsSelfCare() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("cold", "mild headache"))
                .duration("1 day")
                .severityScore(2)
                .build();

        List<String> warnings = new ArrayList<>();
        TriageLevel level = triageEvaluator.evaluateTriage(data, warnings);

        assertEquals(TriageLevel.SELF_CARE_OTC, level);
        assertTrue(warnings.isEmpty());
    }

    @Test
    @DisplayName("Should categorize severity 6 as Urgent Medical Attention")
    void testUrgentAttentionSeverity() {
        StructuredSymptomData data = StructuredSymptomData.builder()
                .symptoms(List.of("stomach pain"))
                .duration("1 day")
                .severityScore(6)
                .build();

        List<String> warnings = new ArrayList<>();
        TriageLevel level = triageEvaluator.evaluateTriage(data, warnings);

        assertEquals(TriageLevel.URGENT_MEDICAL_ATTENTION, level);
    }
}
