package com.meds.app.presentation.patient

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.meds.app.R
import com.meds.app.core.ui.theme.LocalReduceMotion
import com.meds.app.core.ui.theme.LocalShowGraphLabels
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.navigation.Routes
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val StaticBarHeight = 68.dp
private val IconPillHeight = 62.dp
private val CenterHubSize = 62.dp
private val GraphNodeSize = 52.dp
private val GraphStageHeight = 200.dp

private data class PatientDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private data class GraphNodeSpec(
    val destination: PatientDestination,
    val targetOffsetX: Dp,
    val targetOffsetY: Dp
)

@Composable
private fun staticDestinations(): List<PatientDestination> = listOf(
    PatientDestination(Routes.Patient.HOME, "Hoy", Icons.Outlined.Home),
    PatientDestination(
        Routes.Patient.MEDICATIONS,
        "Medicamentos",
        ImageVector.vectorResource(R.drawable.ic_pill)
    ),
    PatientDestination(Routes.Patient.GARDEN, "Jardín", Icons.Outlined.Eco),
    PatientDestination(Routes.Patient.SETTINGS, "Ajustes", Icons.Outlined.Settings)
)

@Composable
private fun graphNodeSpecs(): List<GraphNodeSpec> = listOf(
    GraphNodeSpec(
        destination = PatientDestination(
            route = Routes.Patient.MEDICATIONS,
            label = "Medicamentos",
            icon = ImageVector.vectorResource(R.drawable.ic_pill)
        ),
        targetOffsetX = (-76).dp,
        targetOffsetY = (-76).dp
    ),
    GraphNodeSpec(
        destination = PatientDestination(
            route = Routes.Patient.GARDEN,
            label = "Jardín",
            icon = Icons.Outlined.Eco
        ),
        targetOffsetX = 0.dp,
        targetOffsetY = (-102).dp
    ),
    GraphNodeSpec(
        destination = PatientDestination(
            route = Routes.Patient.COMPANIONS,
            label = "Acompañante",
            icon = Icons.Outlined.Group
        ),
        targetOffsetX = 76.dp,
        targetOffsetY = (-76).dp
    )
)

/**
 * Barra de navegación inferior del paciente.
 *
 * Permite intercambiar desde Ajustes entre:
 * - [PatientNavBarStyle.STATIC]: barra estática con las 4 pestañas siempre visibles.
 * - [PatientNavBarStyle.GRAPH_ICONS]: barra de solo iconos (Hoy, botón central con hoja,
 *   Ajustes) que al pulsar el centro despliega 3 círculos conectados en forma de grafo.
 */
@Composable
internal fun PatientBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: PatientNavBarStyle = PatientNavBarStyle.STATIC
) {
    when (style) {
        PatientNavBarStyle.STATIC -> {
            StaticPatientBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier
            )
        }

        PatientNavBarStyle.GRAPH_ICONS -> {
            GraphIconsPatientBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun StaticPatientBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StaticBarHeight)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val destinations = staticDestinations()
                destinations.forEach { destination ->
                    StaticPatientTab(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { onNavigate(destination.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticPatientTab(
    destination: PatientDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = if (reduceMotion) snap() else tween(CalmaMotion.NAV_FADE_MILLIS),
        label = "patientTabColor"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
        },
        animationSpec = if (reduceMotion) snap() else tween(CalmaMotion.NAV_FADE_MILLIS),
        label = "patientTabIndicator"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale = animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 900f),
        label = "patientTabPress"
    )

    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(indicatorColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GraphIconsPatientBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val showGraphLabels = LocalShowGraphLabels.current
    var graphExpanded by rememberSaveable { mutableStateOf(false) }
    var nodesVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        graphExpanded = false
    }

    LaunchedEffect(graphExpanded, reduceMotion) {
        if (graphExpanded) {
            nodesVisible = true
        } else {
            if (!reduceMotion) {
                delay(400)
            }
            nodesVisible = false
        }
    }

    BackHandler(enabled = graphExpanded) {
        graphExpanded = false
    }

    val nodeSpec = if (reduceMotion) {
        snap<Float>()
    } else {
        spring(
            dampingRatio = 0.86f,
            stiffness = 380f
        )
    }

    val graphNodes = graphNodeSpecs()
    val nodeProgresses = graphNodes.mapIndexed { index, _ ->
        animateFloatAsState(
            targetValue = if (graphExpanded) 1f else 0f,
            animationSpec = nodeSpec,
            label = "graphNodeProgress_$index"
        )
    }

    val isGraphDestinationActive = graphNodes.any { it.destination.route == currentRoute }

    Box(
        modifier = if (graphExpanded) {
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.10f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { graphExpanded = false }
                )
        } else {
            modifier.fillMaxWidth()
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
                .height(GraphStageHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val edgeDefaultColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

            if (nodesVisible) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val hubCenter = Offset(
                        x = size.width / 2f,
                        y = size.height - (CenterHubSize.toPx() / 2f)
                    )
                    val hubRadius = CenterHubSize.toPx() / 2f
                    val nodeRadius = GraphNodeSize.toPx() / 2f

                    graphNodes.forEachIndexed { index, spec ->
                        val progress = nodeProgresses[index].value
                        if (progress > 0.02f) {
                            val dx = spec.targetOffsetX.toPx() * progress
                            val dy = spec.targetOffsetY.toPx() * progress
                            val distance = hypot(dx, dy)
                            val minEdgeGap = hubRadius + 4.dp.toPx()
                            val maxEdgeReach = (distance - nodeRadius - 3.dp.toPx())
                                .coerceAtLeast(minEdgeGap)

                            if (distance > minEdgeGap && maxEdgeReach > minEdgeGap) {
                                val ux = dx / distance
                                val uy = dy / distance
                                val start = Offset(
                                    x = hubCenter.x + ux * minEdgeGap,
                                    y = hubCenter.y + uy * minEdgeGap
                                )
                                val end = Offset(
                                    x = hubCenter.x + ux * maxEdgeReach,
                                    y = hubCenter.y + uy * maxEdgeReach
                                )
                                val isNodeSelected = currentRoute == spec.destination.route
                                drawLine(
                                    color = (if (isNodeSelected) primaryColor else edgeDefaultColor)
                                        .copy(alpha = progress),
                                    start = start,
                                    end = end,
                                    strokeWidth = 2.4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IconPillHeight),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        PillIconAction(
                            icon = Icons.Outlined.Home,
                            contentDescription = "Hoy",
                            selected = currentRoute == Routes.Patient.HOME,
                            onClick = {
                                graphExpanded = false
                                onNavigate(Routes.Patient.HOME)
                            }
                        )
                    }

                    Spacer(Modifier.width(CenterHubSize + 12.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        PillIconAction(
                            icon = Icons.Outlined.Settings,
                            contentDescription = "Ajustes",
                            selected = currentRoute == Routes.Patient.SETTINGS,
                            onClick = {
                                graphExpanded = false
                                onNavigate(Routes.Patient.SETTINGS)
                            }
                        )
                    }
                }
            }

            if (nodesVisible) {
                graphNodes.forEachIndexed { index, spec ->
                    val selected = currentRoute == spec.destination.route
                    val progress = nodeProgresses[index]
                    GraphCircleNode(
                        destination = spec.destination,
                        selected = selected,
                        progress = progress,
                        offsetX = spec.targetOffsetX,
                        offsetY = spec.targetOffsetY,
                        onClick = {
                            graphExpanded = false
                            onNavigate(spec.destination.route)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    if (showGraphLabels) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset {
                                    IntOffset(
                                        x = (spec.targetOffsetX.toPx() * progress.value).roundToInt(),
                                        y = ((spec.targetOffsetY + 12.dp).toPx() * progress.value).roundToInt()
                                    )
                                }
                                .graphicsLayer { alpha = progress.value }
                                .clearAndSetSemantics {},
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ) {
                            Text(
                                text = spec.destination.label,
                                modifier = Modifier
                                    .width(84.dp)
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            CenterLeafHubButton(
                expanded = graphExpanded,
                activeChild = isGraphDestinationActive,
                onClick = { graphExpanded = !graphExpanded },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PillIconAction(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = if (reduceMotion) snap() else tween(CalmaMotion.NAV_FADE_MILLIS),
        label = "pillIconTint"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = if (reduceMotion) snap() else tween(CalmaMotion.NAV_FADE_MILLIS),
        label = "pillIconIndicator"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale = animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 900f),
        label = "pillIconPress"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .background(indicatorColor, CircleShape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun GraphCircleNode(
    destination: PatientDestination,
    selected: Boolean,
    progress: State<Float>,
    offsetX: Dp,
    offsetY: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseBottomOffset = (CenterHubSize - GraphNodeSize) / 2
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    }
    val iconTint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val reduceMotion = LocalReduceMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale = animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 900f),
        label = "graphNodePress"
    )

    Surface(
        modifier = modifier
            .padding(bottom = baseBottomOffset)
            .offset {
                IntOffset(
                    x = (offsetX.toPx() * progress.value).roundToInt(),
                    y = (offsetY.toPx() * progress.value).roundToInt()
                )
            }
            .size(GraphNodeSize)
            .graphicsLayer {
                val scale = lerp(0.55f, 1f, progress.value) * pressScale.value
                scaleX = scale
                scaleY = scale
                alpha = progress.value
            }
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                contentDescription = destination.label
            },
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun CenterLeafHubButton(
    expanded: Boolean,
    activeChild: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val rotation = animateFloatAsState(
        targetValue = if (expanded) 14f else 0f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "leafHubRotation"
    )
    val scale = animateFloatAsState(
        targetValue = if (expanded) 1.05f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "leafHubScale"
    )
    val borderColor = when {
        expanded || activeChild -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale = animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 900f),
        label = "leafHubPress"
    )

    val leafPath = remember {
        PathParser().parsePathString(
            "M54,20 C39,27 30,42 32,57 C34,71 45,82 54,86 C49,70 49,55 56,42 C60,34 66,28 73,24 C67,21 60,20 54,20 Z"
        ).toPath()
    }
    val veinPath = remember {
        PathParser().parsePathString(
            "M54,84 C54,69 57,54 65,41"
        ).toPath()
    }

    val containerColor = MaterialTheme.colorScheme.primary
    val leafColor = MaterialTheme.colorScheme.onPrimary

    Surface(
        modifier = modifier
            .size(CenterHubSize)
            .graphicsLayer {
                scaleX = scale.value * pressScale.value
                scaleY = scale.value * pressScale.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = if (expanded) {
                    "Cerrar menú de navegación en grafo"
                } else {
                    "Abrir menú de navegación en grafo"
                }
            },
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(2.dp, borderColor),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { rotationZ = rotation.value }
            ) {
                val factor = size.minDimension / 108f
                scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) {
                    drawPath(
                        path = leafPath,
                        color = leafColor
                    )
                    drawPath(
                        path = veinPath,
                        color = containerColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
