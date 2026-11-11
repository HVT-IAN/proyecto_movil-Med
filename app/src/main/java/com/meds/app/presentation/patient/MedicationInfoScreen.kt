package com.meds.app.presentation.patient

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.domain.model.MedicationInfo
import com.meds.app.domain.model.MedicationInfoDetail
import com.meds.app.core.ui.components.EmptyState
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton

@Composable
fun MedicationInfoScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onUseName: (String) -> Unit,
    viewModel: MedicationInfoViewModel = viewModel(factory = MedsViewModelFactory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(initialQuery) {
        viewModel.initialize(initialQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "Consultar medicamento",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Información oficial de la CIMA/AEMPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Busca por nombre comercial o principio activo. Por ejemplo: ibuprofeno, paracetamol o omeprazol.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre o principio activo") },
            placeholder = { Text("Ej. ibuprofeno") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Borrar búsqueda"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    viewModel.search()
                }
            )
        )

        Spacer(Modifier.height(12.dp))
        PrimaryActionButton(
            text = if (state.loading) "Buscando..." else "Buscar en CIMA",
            onClick = {
                focusManager.clearFocus()
                viewModel.search()
            },
            enabled = state.query.trim().length >= 2 && !state.loading,
            icon = Icons.Outlined.Search
        )

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Información de referencia de la AEMPS. No sustituye el consejo de un profesional sanitario.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Buscando en CIMA...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.error != null -> {
                MedsCard(modifier = Modifier.weight(1f)) {
                    SectionLabel(text = "No se pudo consultar")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    TextActionButton(
                        text = "Reintentar",
                        onClick = viewModel::search,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            state.searched && state.results.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "Sin resultados",
                    message = "No encontramos medicamentos con ese nombre. Prueba con el principio activo, por ejemplo ibuprofeno.",
                    actionLabel = "Buscar de nuevo",
                    onAction = viewModel::search,
                    modifier = Modifier.weight(1f)
                )
            }

            !state.searched -> {
                EmptyState(
                    icon = Icons.Outlined.Medication,
                    title = "Consulta medicamentos",
                    message = "Los datos vienen de la CIMA de la AEMPS. Puedes consultar la ficha técnica y el prospecto oficial.",
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.total > state.results.size) {
                            "Mostrando ${state.results.size} de ${state.total} resultados"
                        } else {
                            "${state.total} resultados"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.results,
                            key = { it.registry.ifBlank { it.displayName } }
                        ) { info ->
                            MedicationInfoCard(
                                info = info,
                                onDetail = { viewModel.openDetail(info.registry) },
                                onUseName = onUseName
                            )
                        }
                    }
                }
            }
        }
    }

    MedicationInfoDetailDialog(
        loading = state.detailLoading,
        detail = state.detail,
        error = state.detailError,
        onDismiss = viewModel::closeDetail
    )
}

@Composable
private fun MedicationInfoCard(
    info: MedicationInfo,
    onDetail: () -> Unit,
    onUseName: (String) -> Unit
) {
    MedsCard {
        Text(
            text = info.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (info.laboratory.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = info.laboratory,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (info.activeSubstance.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Principio activo: ${info.activeSubstance}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val details = listOf(info.dose, info.form).filter { it.isNotBlank() }
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = details.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (info.prescription.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = info.prescription,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = if (info.commercialized) "Comercializado" else "No comercializado"
            )
            if (info.generic) {
                StatusPill(text = "Genérico")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextActionButton(
            text = "Ver ficha y documentos",
            onClick = onDetail,
            modifier = Modifier.fillMaxWidth()
        )
        TextActionButton(
            text = "Usar este nombre",
            onClick = { onUseName(info.displayName) },
            modifier = Modifier.fillMaxWidth()
        )
        if (
            info.activeSubstance.isNotBlank() &&
            !info.activeSubstance.equals(info.displayName, ignoreCase = true)
        ) {
            TextActionButton(
                text = "Usar principio activo",
                onClick = { onUseName(info.activeSubstance) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun MedicationInfoDetailDialog(
    loading: Boolean,
    detail: MedicationInfoDetail?,
    error: String?,
    onDismiss: () -> Unit
) {
    if (!loading && detail == null && error == null) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ficha del medicamento",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Cargando ficha...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    error != null -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No se pudo cargar la ficha.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            TextActionButton(
                                text = "Cerrar",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    detail != null -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = detail.info.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (detail.info.laboratory.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = detail.info.laboratory,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            SectionLabel(text = "Principio activo")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = detail.info.activeSubstance.ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(12.dp))
                            SectionLabel(text = "Dosis y forma")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = listOf(detail.info.dose, detail.info.form)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · ")
                                    .ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(12.dp))
                            SectionLabel(text = "Prescripción")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = detail.info.prescription.ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            DetailList(
                                title = "Principios activos",
                                values = detail.activeIngredients
                            )
                            DetailList(
                                title = "Código ATC",
                                values = detail.atc
                            )
                            DetailList(
                                title = "Vías de administración",
                                values = detail.administrationRoutes
                            )
                            DetailList(
                                title = "Presentaciones",
                                values = detail.presentations,
                                limit = 8
                            )
                            DetailList(
                                title = "Excipientes",
                                values = detail.excipients,
                                limit = 10
                            )

                            if (detail.info.docs.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                SectionLabel(text = "Documentos oficiales")
                                Spacer(Modifier.height(4.dp))
                                detail.info.docs.forEach { doc ->
                                    TextActionButton(
                                        text = doc.label,
                                        onClick = { openDocument(context, doc.url) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Información de referencia de la AEMPS. No sustituye el consejo de un profesional sanitario.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailList(
    title: String,
    values: List<String>,
    limit: Int = 12
) {
    if (values.isEmpty()) return

    Spacer(Modifier.height(12.dp))
    SectionLabel(text = title)
    Spacer(Modifier.height(4.dp))
    values.take(limit).forEach { value ->
        Text(
            text = "• $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (values.size > limit) {
        Text(
            text = "y ${values.size - limit} más...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun openDocument(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(
            context,
            "No se pudo abrir el documento.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
