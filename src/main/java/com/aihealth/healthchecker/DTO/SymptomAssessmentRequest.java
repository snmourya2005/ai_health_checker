package com.aihealth.healthchecker.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAssessmentRequest {

    @NotBlank(message = "Symptom description cannot be empty")
    @Size(min = 3, max = 2000, message = "Description must be between 3 and 2000 characters")
    private String userInput;

    private String conversationId;

    private Integer userAge;

    private String userGender;
}
