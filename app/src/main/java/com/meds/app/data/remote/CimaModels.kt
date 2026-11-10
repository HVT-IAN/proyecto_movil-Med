package com.meds.app.data.remote

data class CimaSearchResponse(
    val totalFilas: Int = 0,
    val pagina: Int = 1,
    val tamanioPagina: Int = 20,
    val resultados: List<CimaMedication> = emptyList()
)

data class CimaMedication(
    val nregistro: String = "",
    val nombre: String = "",
    val labtitular: String = "",
    val labcomercializador: String = "",
    val cpresc: String = "",
    val comerc: Boolean = false,
    val receta: Boolean = false,
    val generico: Boolean = false,
    val dosis: String = "",
    val formaFarmaceutica: CimaNamed? = null,
    val formaFarmaceuticaSimplificada: CimaNamed? = null,
    val vtm: CimaNamed? = null,
    val viasAdministracion: List<CimaNamed> = emptyList(),
    val docs: List<CimaDoc> = emptyList()
)

data class CimaDetail(
    val nregistro: String = "",
    val nombre: String = "",
    val pactivos: String = "",
    val labtitular: String = "",
    val labcomercializador: String = "",
    val cpresc: String = "",
    val comerc: Boolean = false,
    val receta: Boolean = false,
    val generico: Boolean = false,
    val dosis: String = "",
    val formaFarmaceutica: CimaNamed? = null,
    val formaFarmaceuticaSimplificada: CimaNamed? = null,
    val vtm: CimaNamed? = null,
    val viasAdministracion: List<CimaNamed> = emptyList(),
    val docs: List<CimaDoc> = emptyList(),
    val atcs: List<CimaAtc> = emptyList(),
    val principiosActivos: List<CimaIngredient> = emptyList(),
    val excipientes: List<CimaIngredient> = emptyList(),
    val presentaciones: List<CimaPresentation> = emptyList()
)

data class CimaNamed(
    val id: Long = 0L,
    val nombre: String = ""
)

data class CimaDoc(
    val tipo: Int = 0,
    val url: String? = null,
    val urlHtml: String? = null,
    val secc: Boolean = false,
    val fecha: Long = 0L
)

data class CimaAtc(
    val codigo: String = "",
    val nombre: String = "",
    val nivel: Int = 0
)

data class CimaIngredient(
    val nombre: String = "",
    val cantidad: String = "",
    val unidad: String = ""
)

data class CimaPresentation(
    val cn: String = "",
    val nombre: String = "",
    val comerc: Boolean = false
)
