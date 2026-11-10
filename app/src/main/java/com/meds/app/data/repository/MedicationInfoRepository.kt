package com.meds.app.data.repository

import com.meds.app.data.model.MedicationDocument
import com.meds.app.data.model.MedicationInfo
import com.meds.app.data.model.MedicationInfoDetail
import com.meds.app.data.model.MedicationSearchResult
import com.meds.app.data.remote.CimaApi
import com.meds.app.data.remote.CimaClient
import com.meds.app.data.remote.CimaDetail
import com.meds.app.data.remote.CimaDoc
import com.meds.app.data.remote.CimaIngredient
import com.meds.app.data.remote.CimaMedication
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MedicationInfoRepository(
    private val api: CimaApi = CimaClient.api
) {
    suspend fun search(rawQuery: String): Result<MedicationSearchResult> = runCatching {
        val query = rawQuery.trim()
        require(query.length >= 2) { "Escribe al menos 2 caracteres para buscar." }

        val response = api.search(query, page = 1)
        val sorted = response.resultados
            .map { it.toMedicationInfo() }
            .sortedWith(
                compareByDescending<MedicationInfo> {
                    it.activeSubstance.trim().equals(query, ignoreCase = true)
                }
                    .thenByDescending { it.commercialized }
                    .thenBy { it.displayName.lowercase() }
            )

        MedicationSearchResult(
            items = sorted.take(MAX_RESULTS),
            total = response.totalFilas
        )
    }

    suspend fun detail(registry: String): Result<MedicationInfoDetail> = runCatching {
        require(registry.isNotBlank()) { "No se indicó el medicamento." }
        api.detail(registry).toMedicationInfoDetail()
    }

    private fun CimaMedication.toMedicationInfo(): MedicationInfo {
        return MedicationInfo(
            registry = nregistro,
            name = nombre,
            laboratory = labtitular.ifBlank { labcomercializador },
            activeSubstance = vtm?.nombre.orEmpty(),
            dose = dosis,
            form = formaFarmaceutica?.nombre
                ?: formaFarmaceuticaSimplificada?.nombre.orEmpty(),
            prescription = prescriptionLabel(),
            commercialized = comerc,
            generic = generico,
            docs = docs.toDocuments()
        )
    }

    private fun CimaDetail.toMedicationInfoDetail(): MedicationInfoDetail {
        val activeIngredients = principiosActivos
            .mapNotNull { it.toLabel() }
            .ifEmpty { pactivos.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty() }

        return MedicationInfoDetail(
            info = MedicationInfo(
                registry = nregistro,
                name = nombre,
                laboratory = labtitular.ifBlank { labcomercializador },
                activeSubstance = pactivos.ifBlank { vtm?.nombre.orEmpty() },
                dose = dosis,
                form = formaFarmaceutica?.nombre
                    ?: formaFarmaceuticaSimplificada?.nombre.orEmpty(),
                prescription = prescriptionLabel(),
                commercialized = comerc,
                generic = generico,
                docs = docs.toDocuments()
            ),
            activeIngredients = activeIngredients,
            excipients = excipientes.mapNotNull { it.toLabel() },
            atc = atcs.map { atc ->
                listOf(atc.codigo, atc.nombre)
                    .filter { it.isNotBlank() }
                    .joinToString(" - ")
            }.filter { it.isNotBlank() },
            presentations = presentaciones.map { it.nombre }.filter { it.isNotBlank() },
            administrationRoutes = viasAdministracion.map { it.nombre }.filter { it.isNotBlank() }
        )
    }

    private fun CimaMedication.prescriptionLabel(): String {
        if (cpresc.isNotBlank()) return cpresc
        return if (receta) "Requiere receta" else "Sin receta"
    }

    private fun CimaDetail.prescriptionLabel(): String {
        if (cpresc.isNotBlank()) return cpresc
        return if (receta) "Requiere receta" else "Sin receta"
    }

    private fun CimaIngredient.toLabel(): String? {
        val text = listOf(nombre, cantidad, unidad)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return text.ifBlank { null }
    }

    private fun List<CimaDoc>.toDocuments(): List<MedicationDocument> {
        return mapNotNull { doc ->
            val url = doc.urlHtml?.takeIf { it.isNotBlank() } ?: doc.url?.takeIf { it.isNotBlank() }
            if (url == null) return@mapNotNull null
            MedicationDocument(
                label = when (doc.tipo) {
                    1 -> "Ficha técnica"
                    2 -> "Prospecto"
                    3 -> "Información para el paciente"
                    else -> "Documento"
                },
                url = url
            )
        }.distinctBy { it.url }
    }

    companion object {
        private const val MAX_RESULTS = 20
    }
}

fun Throwable.toMedicationInfoMessage(): String {
    return when (this) {
        is IllegalArgumentException -> message ?: "Revisa el texto de búsqueda."
        is HttpException -> when (code()) {
            404 -> "No se encontraron medicamentos con ese nombre."
            429 -> "CIMA recibió demasiadas consultas. Espera un momento."
            in 500..599 -> "El servicio de CIMA no está disponible. Intenta más tarde."
            else -> "No se pudo consultar CIMA. Código ${code()}."
        }

        is SocketTimeoutException ->
            "La consulta tardó demasiado. Revisa tu conexión e intenta de nuevo."

        is JsonParseException ->
            "La respuesta de CIMA no se pudo interpretar. Intenta de nuevo."

        is IOException ->
            "Sin conexión. Revisa tu red e intenta de nuevo."

        else -> "No se pudo consultar la información del medicamento."
    }
}
