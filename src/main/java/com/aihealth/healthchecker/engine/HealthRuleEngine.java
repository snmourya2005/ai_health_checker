package com.aihealth.healthchecker.engine;

import com.aihealth.healthchecker.DTO.StructuredSymptomData;
import com.aihealth.healthchecker.DTO.TriageLevel;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HealthRuleEngine {

    private final TriageEvaluator triageEvaluator;

    public HealthRuleEngine(TriageEvaluator triageEvaluator) {
        this.triageEvaluator = triageEvaluator;
    }

    public RuleAssessmentResult evaluate(StructuredSymptomData symptomData) {
        if (symptomData == null || symptomData.getSymptoms() == null || symptomData.getSymptoms().isEmpty()) {
            return RuleAssessmentResult.builder()
                    .triageLevel(TriageLevel.SELF_CARE_OTC)
                    .primaryConditionCategory("General Wellness")
                    .recommendedSpecialty("General Physician")
                    .clinicalAdvice(List.of("Maintain good hydration, nutrition, and rest. If specific symptoms develop, please reassess."))
                    .clinicalSummary("No specific symptoms were identified.")
                    .build();
        }

        List<String> emergencyWarnings = new ArrayList<>();
        TriageLevel triageLevel = triageEvaluator.evaluateTriage(symptomData, emergencyWarnings);

        Set<String> verifiedRemedies = new LinkedHashSet<>();
        Set<String> clinicalAdvices = new LinkedHashSet<>();
        Set<String> specialties = new LinkedHashSet<>();
        List<String> conditionCategories = new ArrayList<>();

        for (String rawSymptom : symptomData.getSymptoms()) {
            String norm = normalizeSymptom(rawSymptom);

            switch (norm) {
                case "headache", "migraine" -> {
                    conditionCategories.add("Cephalea / Neurological");
                    specialties.add("Neurology");
                    verifiedRemedies.addAll(List.of("Paracetamol (500mg)", "Ibuprofen (200-400mg) with food"));
                    clinicalAdvices.add("Rest in a quiet, dimly lit environment and maintain adequate hydration.");
                    clinicalAdvices.add("Avoid screen exposure and monitor for visual disturbances.");
                }
                case "fever", "chills" -> {
                    conditionCategories.add("Febrile / Systemic");
                    specialties.add("General Physician");
                    verifiedRemedies.add("Paracetamol (Acetaminophen) for temperature reduction");
                    clinicalAdvices.add("Monitor body temperature regularly; seek medical evaluation if fever exceeds 102°F (38.9°C) or lasts > 3 days.");
                    clinicalAdvices.add("Drink plenty of fluids (water, soups, oral rehydration).");
                }
                case "cold", "congestion", "sinus" -> {
                    conditionCategories.add("Upper Respiratory / Rhinological");
                    specialties.add("ENT");
                    verifiedRemedies.addAll(List.of("Cetirizine (10mg)", "Saline Nasal Spray", "Decongestant"));
                    clinicalAdvices.add("Perform warm steam inhalation 2-3 times daily.");
                    clinicalAdvices.add("Avoid chilled beverages and ensure humidified air.");
                }
                case "cough", "sore throat" -> {
                    conditionCategories.add("Upper/Lower Respiratory");
                    specialties.add("ENT");
                    verifiedRemedies.addAll(List.of("Dextromethorphan (Dry cough)", "Guaifenesin (Wet cough)", "Throat lozenges"));
                    clinicalAdvices.add("Gargle with warm salt water 3 times daily.");
                    clinicalAdvices.add("Drink warm herbal tea with honey.");
                }
                case "chest pain", "chest tightness", "palpitations" -> {
                    conditionCategories.add("Cardiovascular / Thoracic");
                    specialties.add("Cardiology");
                    emergencyWarnings.add("Chest discomfort requires high clinical vigilance to rule out cardiac causes.");
                    clinicalAdvices.add("Rest immediately. Do NOT exert yourself physically.");
                }
                case "bodypain", "body ache", "muscle pain", "joint pain" -> {
                    conditionCategories.add("Musculoskeletal / Somatic");
                    specialties.add("General Physician");
                    verifiedRemedies.addAll(List.of("Paracetamol", "Topical Analgesic Gel / Patch"));
                    clinicalAdvices.add("Apply warm compresses to painful areas and avoid strenuous lifting.");
                }
                case "weakness", "fatigue" -> {
                    conditionCategories.add("Systemic / Metabolic");
                    specialties.add("General Physician");
                    verifiedRemedies.addAll(List.of("Oral Rehydration Salts (ORS)", "B-Complex multivitamins"));
                    clinicalAdvices.add("Ensure 8+ hours of restful sleep and balanced electrolyte-rich meals.");
                }
                case "stomach pain", "nausea", "vomiting", "diarrhea", "acidity" -> {
                    conditionCategories.add("Gastrointestinal");
                    specialties.add("Gastroenterology");
                    verifiedRemedies.addAll(List.of("Antacid (Gelusil / Digene)", "ORS Hydration", "Probiotics"));
                    clinicalAdvices.add("Follow a bland diet (BRAT: Bananas, Rice, Applesauce, Toast).");
                    clinicalAdvices.add("Avoid spicy, greasy, or caffeinated food.");
                }
                case "cuts", "wound", "skin rash", "itching" -> {
                    conditionCategories.add("Dermatological / Trauma");
                    specialties.add("Dermatology");
                    verifiedRemedies.addAll(List.of("Betadine antiseptic solution", "Hydrocortisone 1% cream", "Sterile dressing"));
                    clinicalAdvices.add("Keep the affected area clean and dry. Avoid scratching.");
                }
                default -> {
                    conditionCategories.add("General Health Symptom");
                    specialties.add("General Physician");
                    clinicalAdvices.add("Monitor symptom progression and record duration and frequency.");
                }
            }
        }

        String primaryCategory = conditionCategories.isEmpty() ? "General Health" : conditionCategories.get(0);
        String primarySpecialty = specialties.isEmpty() ? "General Physician" : specialties.iterator().next();

        boolean immediateCare = (triageLevel == TriageLevel.EMERGENCY_RED_FLAG);

        return RuleAssessmentResult.builder()
                .triageLevel(triageLevel)
                .primaryConditionCategory(primaryCategory)
                .recommendedSpecialty(primarySpecialty)
                .verifiedOtcRemedies(new ArrayList<>(verifiedRemedies))
                .clinicalAdvice(new ArrayList<>(clinicalAdvices))
                .emergencyWarnings(emergencyWarnings)
                .requiresImmediateCare(immediateCare)
                .clinicalSummary("Deterministic rule evaluation matched " + symptomData.getSymptoms().size() + " symptom(s) to " + primarySpecialty + ".")
                .build();
    }

    private String normalizeSymptom(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT).trim();
        if (lower.contains("headache") || lower.contains("migraine") || lower.contains("head pain")) return "headache";
        if (lower.contains("fever") || lower.contains("high temp") || lower.contains("temperature")) return "fever";
        if (lower.contains("chest")) return "chest pain";
        if (lower.contains("cold") || lower.contains("runny nose") || lower.contains("congestion")) return "cold";
        if (lower.contains("cough") || lower.contains("throat")) return "cough";
        if (lower.contains("body pain") || lower.contains("bodypain") || lower.contains("muscle") || lower.contains("joint")) return "bodypain";
        if (lower.contains("weakness") || lower.contains("tired") || lower.contains("fatigue")) return "weakness";
        if (lower.contains("stomach") || lower.contains("belly") || lower.contains("acidity") || lower.contains("nausea") || lower.contains("vomit") || lower.contains("diarrhea")) return "stomach pain";
        if (lower.contains("cut") || lower.contains("wound") || lower.contains("rash") || lower.contains("skin")) return "cuts";
        return lower;
    }
}
