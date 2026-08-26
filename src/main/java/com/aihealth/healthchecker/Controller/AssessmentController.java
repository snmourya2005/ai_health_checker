package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.DTO.ConversationRequest;
import com.aihealth.healthchecker.DTO.ConversationResponse;
import com.aihealth.healthchecker.DTO.SymptomAssessmentRequest;
import com.aihealth.healthchecker.DTO.SymptomAssessmentResponse;
import com.aihealth.healthchecker.Service.HealthAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assessment")
@Tag(name = "AI Health Assessment API", description = "AI-assisted clinical triage, symptom extraction, deterministic rule evaluation, and conversational advice")
public class AssessmentController {

    private final HealthAssessmentService assessmentService;

    public AssessmentController(HealthAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze natural language symptoms", description = "Extracts structured symptom data, evaluates via deterministic Java clinical rules, and generates a safe triage explanation")
    public ResponseEntity<SymptomAssessmentResponse> analyzeSymptoms(@Valid @RequestBody SymptomAssessmentRequest request) {
        SymptomAssessmentResponse response = assessmentService.assessSymptoms(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    @Operation(summary = "Contextual follow-up conversation", description = "Answers clarifying questions regarding symptoms, home-care recommendations, and specialist navigation")
    public ResponseEntity<ConversationResponse> chat(@Valid @RequestBody ConversationRequest request) {
        ConversationResponse response = assessmentService.handleConversation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get triage rules metadata", description = "Returns supported medical specialties and clinical triage levels")
    public ResponseEntity<Map<String, Object>> getRulesMetadata() {
        return ResponseEntity.ok(assessmentService.getSupportedRulesMetadata());
    }
}
