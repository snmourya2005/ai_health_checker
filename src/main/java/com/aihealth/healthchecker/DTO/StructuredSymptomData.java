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
public class StructuredSymptomData {
    @Builder.Default
    private List<String> symptoms = new ArrayList<>();

    @Builder.Default
    private String duration = "unspecified";

    @Builder.Default
    private int severityScore = 3;

    @Builder.Default
    private List<String> emergencyFlags = new ArrayList<>();

    @Builder.Default
    private boolean isEmergency = false;

    @Builder.Default
    private List<String> bodyParts = new ArrayList<>();

    @Builder.Default
    private List<String> missingContext = new ArrayList<>();
}
