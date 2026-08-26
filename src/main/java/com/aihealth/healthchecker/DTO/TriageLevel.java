package com.aihealth.healthchecker.DTO;

public enum TriageLevel {
    EMERGENCY_RED_FLAG("Critical Emergency", "Seek immediate emergency medical care (ER or call emergency services)."),
    URGENT_MEDICAL_ATTENTION("Urgent Medical Attention", "Medical consultation recommended within 24 hours."),
    ROUTINE_CONSULTATION("Routine Consultation", "Schedule an appointment with a healthcare professional."),
    SELF_CARE_OTC("Self-Care & Monitoring", "Safe for home monitoring and OTC relief. Consult doctor if symptoms worsen.");

    private final String displayName;
    private final String description;

    TriageLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
