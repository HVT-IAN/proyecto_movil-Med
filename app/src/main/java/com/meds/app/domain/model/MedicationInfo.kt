package com.meds.app.domain.model

data class MedicationInfo(
    val registry: String = "",
    val name: String = "",
    val laboratory: String = "",
    val activeSubstance: String = "",
    val dose: String = "",
    val form: String = "",
    val prescription: String = "",
    val commercialized: Boolean = false,
    val generic: Boolean = false,
    val docs: List<MedicationDocument> = emptyList()
) {
    val displayName: String
        get() = name.ifBlank { activeSubstance }
}

data class MedicationInfoDetail(
    val info: MedicationInfo,
    val activeIngredients: List<String> = emptyList(),
    val excipients: List<String> = emptyList(),
    val atc: List<String> = emptyList(),
    val presentations: List<String> = emptyList(),
    val administrationRoutes: List<String> = emptyList()
)

data class MedicationDocument(
    val label: String,
    val url: String
)

data class MedicationSearchResult(
    val items: List<MedicationInfo>,
    val total: Int
)
