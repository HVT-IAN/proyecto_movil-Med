package com.meds.app.presentation.patient.cima

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meds.app.core.ui.components.EmptyState
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.di.MedsViewModelFactory

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
