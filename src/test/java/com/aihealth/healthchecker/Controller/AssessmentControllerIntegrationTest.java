package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.DTO.SymptomAssessmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/assessment/analyze should return structured assessment response")
    void testAnalyzeSymptomsEndpoint() throws Exception {
        SymptomAssessmentRequest request = SymptomAssessmentRequest.builder()
                .userInput("I have a fever, cough, and runny nose for 2 days.")
                .build();

        mockMvc.perform(post("/api/assessment/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.triageLevel").exists())
                .andExpect(jsonPath("$.detectedSymptoms").isArray())
                .andExpect(jsonPath("$.safeExplanation").isString())
                .andExpect(jsonPath("$.disclaimer").isString());
    }

    @Test
    @DisplayName("POST /api/assessment/analyze with blank input should return 400 Bad Request")
    void testValidationFailure() throws Exception {
        SymptomAssessmentRequest request = SymptomAssessmentRequest.builder()
                .userInput("")
                .build();

        mockMvc.perform(post("/api/assessment/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userInput").exists());
    }

    @Test
    @DisplayName("GET /api/assessment/rules should return metadata")
    void testRulesMetadataEndpoint() throws Exception {
        mockMvc.perform(get("/api/assessment/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedSpecialties").isArray())
                .andExpect(jsonPath("$.triageLevels").isArray());
    }
}
