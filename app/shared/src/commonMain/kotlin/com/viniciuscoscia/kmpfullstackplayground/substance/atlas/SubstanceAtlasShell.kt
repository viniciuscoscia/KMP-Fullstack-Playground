package com.viniciuscoscia.kmpfullstackplayground.substance.atlas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceLevel
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.RiskProfile
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SubstanceSummary

@Composable
fun SubstanceAtlasShell(
    viewModel: SubstanceAtlasViewModel,
    onOpenAndroidBasics: (() -> Unit)?,
) {
    val state by viewModel.state.collectAsState()
    MaterialTheme {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxWidth < 760.dp
            if (compact) {
                CompactShell(state, viewModel::dispatch, onOpenAndroidBasics)
            } else {
                WideShell(state, viewModel::dispatch, onOpenAndroidBasics)
            }
        }
    }
}

@Composable
private fun WideShell(
    state: SubstanceAtlasState,
    dispatch: (SubstanceAtlasAction) -> Unit,
    onOpenAndroidBasics: (() -> Unit)?,
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail(Modifier.fillMaxHeight()) {
            Text(
                text = "SA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 18.dp),
            )
            atlasDestinations.forEach { destination ->
                NavigationRailItem(
                    selected = state.destination == destination,
                    onClick = { dispatch(SubstanceAtlasAction.Navigate(destination)) },
                    icon = { Icon(destination.icon(), destination.label(state.locale)) },
                    label = { Text(destination.label(state.locale)) },
                )
            }
        }
        AtlasContent(state, dispatch, onOpenAndroidBasics, Modifier.weight(1f))
    }
}

@Composable
private fun CompactShell(
    state: SubstanceAtlasState,
    dispatch: (SubstanceAtlasAction) -> Unit,
    onOpenAndroidBasics: (() -> Unit)?,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                atlasDestinations.filter { it != AtlasDestination.DSA }.take(5).forEach { destination ->
                    NavigationBarItem(
                        selected = state.destination == destination,
                        onClick = { dispatch(SubstanceAtlasAction.Navigate(destination)) },
                        icon = { Icon(destination.icon(), destination.label(state.locale)) },
                        label = { Text(destination.label(state.locale), maxLines = 1) },
                    )
                }
            }
        },
    ) { padding ->
        AtlasContent(state, dispatch, onOpenAndroidBasics, Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AtlasContent(
    state: SubstanceAtlasState,
    dispatch: (SubstanceAtlasAction) -> Unit,
    onOpenAndroidBasics: (() -> Unit)?,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Substance Atlas", fontWeight = FontWeight.Bold)
                        Text(
                            "Evidence Workbench",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { dispatch(SubstanceAtlasAction.ToggleLocale) }) {
                        Icon(Icons.Default.Language, "Change language")
                    }
                    IconButton(onClick = { dispatch(SubstanceAtlasAction.Refresh) }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.destination) {
                AtlasDestination.DASHBOARD -> DashboardScreen(state, dispatch)
                AtlasDestination.CATALOG -> CatalogScreen(state, dispatch)
                AtlasDestination.COMPARE -> CompareScreen(state, dispatch)
                AtlasDestination.RESEARCH_QUEUE -> ResearchQueueScreen(state)
                AtlasDestination.SOURCES -> SourcesScreen(state)
                AtlasDestination.DSA -> DsaScreen(onOpenAndroidBasics)
            }
            if (state.isLoading) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                    shape = RoundedCornerShape(6.dp),
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(localized("Loading evidence", "Carregando evidências", state.locale))
                    }
                }
            }
            state.error?.let { error -> ErrorBanner(error) { dispatch(SubstanceAtlasAction.ClearError) } }
            state.selectedSubstance?.let { substance ->
                SubstanceDetailDialog(substance, state.locale) { dispatch(SubstanceAtlasAction.ClearSelection) }
            }
        }
    }
}

@Composable
private fun DashboardScreen(state: SubstanceAtlasState, dispatch: (SubstanceAtlasAction) -> Unit) {
    val dashboard = state.dashboard
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(localized("Evidence overview", "Visão geral das evidências", state.locale), style = MaterialTheme.typography.headlineSmall)
            Text(
                localized("Published assessments and review workflow", "Avaliações publicadas e fluxo de revisão", state.locale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(localized("Products", "Produtos", state.locale), dashboard?.productCount?.toString() ?: "-", Modifier.weight(1f))
                MetricCard(localized("Substances", "Substâncias", state.locale), dashboard?.substanceCount?.toString() ?: "-", Modifier.weight(1f))
                MetricCard(localized("High risk", "Alto risco", state.locale), dashboard?.highRiskCount?.toString() ?: "-", Modifier.weight(1f))
                MetricCard(localized("Review queue", "Fila de revisão", state.locale), dashboard?.reviewQueueCount?.toString() ?: "-", Modifier.weight(1f))
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(localized("Evidence distribution", "Distribuição de evidência", state.locale), fontWeight = FontWeight.SemiBold)
                    EvidenceDistribution(dashboard?.evidenceDistribution.orEmpty())
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { dispatch(SubstanceAtlasAction.Navigate(AtlasDestination.CATALOG)) }) {
                    Icon(Icons.Default.Dataset, null)
                    Spacer(Modifier.width(8.dp))
                    Text(localized("Browse catalog", "Abrir catálogo", state.locale))
                }
                Button(onClick = { dispatch(SubstanceAtlasAction.ExportAnalytics) }) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text(localized("Prepare workbook", "Preparar planilha", state.locale))
                }
            }
        }
    }
}

@Composable
private fun CatalogScreen(state: SubstanceAtlasState, dispatch: (SubstanceAtlasAction) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(localized("Catalog", "Catálogo", state.locale), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = { dispatch(SubstanceAtlasAction.ChangeQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(localized("Search substances", "Buscar substâncias", state.locale)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(onClick = { dispatch(SubstanceAtlasAction.Search) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Search") }
            },
            singleLine = true,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.tags.forEach { tag ->
                FilterChip(
                    selected = tag.slug in state.selectedTagSlugs,
                    onClick = { dispatch(SubstanceAtlasAction.ToggleTag(tag.slug)) },
                    label = { Text(tag.label) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (state.substances.isEmpty() && !state.isLoading) {
            EmptyState(localized("No published assessments match this filter.", "Nenhuma avaliação publicada corresponde a este filtro.", state.locale))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.substances, key = { it.id }) { substance ->
                    SubstanceRow(
                        substance = substance,
                        selected = substance.id in state.compareSelection,
                        locale = state.locale,
                        onOpen = { dispatch(SubstanceAtlasAction.SelectSubstance(substance.id)) },
                        onCompare = { dispatch(SubstanceAtlasAction.ToggleCompare(substance.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubstanceRow(
    substance: SubstanceSummary,
    selected: Boolean,
    locale: String,
    onOpen: () -> Unit,
    onCompare: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(substance.name, fontWeight = FontWeight.SemiBold)
                Text(substance.description, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    substance.tags.take(3).forEach { tag -> AssistChip(onClick = onOpen, label = { Text(tag.label) }) }
                }
            }
            ScoreBadge(localized("Effect", "Efeito", locale), substance.efficacyScore, MaterialTheme.colorScheme.primary)
            ScoreBadge(localized("Risk", "Risco", locale), substance.riskScore, MaterialTheme.colorScheme.error)
            IconButton(onClick = onCompare, colors = IconButtonDefaults.iconButtonColors(containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)) {
                Icon(Icons.Default.CompareArrows, localized("Compare", "Comparar", locale))
            }
        }
    }
}

@Composable
private fun CompareScreen(state: SubstanceAtlasState, dispatch: (SubstanceAtlasAction) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(localized("Compare", "Comparar", state.locale), style = MaterialTheme.typography.headlineSmall)
        Text(localized("Select two to five substances in the catalog.", "Selecione de duas a cinco substâncias no catálogo.", state.locale), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { dispatch(SubstanceAtlasAction.LoadComparison) }, enabled = state.compareSelection.size >= 2) {
            Icon(Icons.Default.CompareArrows, null)
            Spacer(Modifier.width(8.dp))
            Text(localized("Compare selected", "Comparar selecionadas", state.locale))
        }
        Spacer(Modifier.height(14.dp))
        if (state.compareItems.isEmpty()) {
            EmptyState(localized("No comparison is loaded.", "Nenhuma comparação foi carregada.", state.locale))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.compareItems, key = { it.summary.id }) { substance ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(substance.summary.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            substance.indications.forEach { indication ->
                                Text("${indication.indication}: ${scoreText(indication.efficacyScore)} · ${indication.evidenceLevel}")
                            }
                            RiskSummary(substance.riskProfile, state.locale)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchQueueScreen(state: SubstanceAtlasState) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(localized("Research queue", "Fila de pesquisa", state.locale), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (state.researchJobs.isEmpty()) EmptyState(localized("No research jobs are available.", "Não há jobs de pesquisa disponíveis.", state.locale))
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.researchJobs, key = { it.id }) { job ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(job.status.label(state.locale), fontWeight = FontWeight.SemiBold)
                        Text("${job.items.size} ${localized("items", "itens", state.locale)} · ${job.updatedAt}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        job.items.take(4).forEach { item -> Text(item.normalizedTerm ?: item.requestedTerm) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourcesScreen(state: SubstanceAtlasState) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(localized("Sources", "Fontes", state.locale), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (state.sources.isEmpty()) EmptyState(localized("No sources are available.", "Nenhuma fonte disponível.", state.locale))
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.sources, key = { it.id }) { source ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(source.title, fontWeight = FontWeight.SemiBold)
                        Text(source.sourceType, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(source.url, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun DsaScreen(onOpenAndroidBasics: (() -> Unit)?) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Android Basics", style = MaterialTheme.typography.headlineSmall)
        Text("The existing playground remains available from the Android host.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onOpenAndroidBasics != null) Button(onClick = onOpenAndroidBasics) { Text("Open Android Basics") }
    }
}

@Composable
private fun SubstanceDetailDialog(substance: Substance, locale: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(localized("Close", "Fechar", locale)) } },
        title = { Text(substance.summary.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(substance.summary.description)
                Text(localized("Indications", "Indicações", locale), fontWeight = FontWeight.SemiBold)
                substance.indications.ifEmpty { listOf() }.forEach { assessment ->
                    Text("${assessment.indication}: ${scoreText(assessment.efficacyScore)} · ${assessment.evidenceLevel}")
                    Text(assessment.rationale, style = MaterialTheme.typography.bodySmall)
                }
                Text(localized("Risks", "Riscos", locale), fontWeight = FontWeight.SemiBold)
                RiskSummary(substance.riskProfile, locale)
                if (substance.adverseEffects.isNotEmpty()) {
                    Text(substance.adverseEffects.joinToString { it.name })
                }
                Text(localized("Regulatory", "Regulatório", locale), fontWeight = FontWeight.SemiBold)
                substance.regulatoryStatuses.forEach { status -> Text("${status.jurisdiction}: ${status.status}") }
                Text(localized("Evidence", "Evidência", locale), fontWeight = FontWeight.SemiBold)
                substance.evidence.forEach { Text(it.claim) }
            }
        },
    )
}

@Composable
private fun RiskSummary(risk: RiskProfile?, locale: String) {
    if (risk == null) {
        Text(localized("No published risk profile.", "Sem perfil de risco publicado.", locale), color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Text("${localized("Overall risk", "Risco geral", locale)}: ${scoreText(risk.overallScore)}")
        Text(risk.rationale, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EvidenceDistribution(distribution: Map<EvidenceLevel, Long>) {
    val total = distribution.values.sum().coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        val entries = EvidenceLevel.entries
        val width = size.width / entries.size
        entries.forEachIndexed { index, level ->
            val ratio = (distribution[level] ?: 0L).toFloat() / total
            val height = size.height * ratio
            drawRect(
                color = when (level) {
                    EvidenceLevel.HIGH -> Color(0xFF0F766E)
                    EvidenceLevel.MODERATE -> Color(0xFF2563EB)
                    EvidenceLevel.LOW -> Color(0xFFD97706)
                    EvidenceLevel.VERY_LOW -> Color(0xFFB91C1C)
                    EvidenceLevel.INSUFFICIENT -> Color(0xFF64748B)
                },
                topLeft = androidx.compose.ui.geometry.Offset(index * width + 6.dp.toPx(), size.height - height),
                size = androidx.compose.ui.geometry.Size(width - 12.dp.toPx(), height),
            )
            drawRect(Color(0x33000000), style = Stroke(width = 1.dp.toPx()))
        }
    }
}

@Composable
private fun ScoreBadge(label: String, score: Int?, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 46.dp)) {
        Text(scoreText(score), color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WarningAmber, null)
            Spacer(Modifier.width(8.dp))
            Text(error, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Dismiss") }
        }
    }
}

internal fun AtlasDestination.label(locale: String): String = when (this) {
    AtlasDestination.DASHBOARD -> localized("Dashboard", "Visão geral", locale)
    AtlasDestination.CATALOG -> localized("Catalog", "Catálogo", locale)
    AtlasDestination.COMPARE -> localized("Compare", "Comparar", locale)
    AtlasDestination.RESEARCH_QUEUE -> localized("Research", "Pesquisa", locale)
    AtlasDestination.SOURCES -> localized("Sources", "Fontes", locale)
    AtlasDestination.DSA -> "DSA"
}

private fun AtlasDestination.icon() = when (this) {
    AtlasDestination.DASHBOARD -> Icons.Default.Analytics
    AtlasDestination.CATALOG -> Icons.Default.Dataset
    AtlasDestination.COMPARE -> Icons.Default.CompareArrows
    AtlasDestination.RESEARCH_QUEUE -> Icons.Default.Science
    AtlasDestination.SOURCES -> Icons.Default.Biotech
    AtlasDestination.DSA -> Icons.AutoMirrored.Filled.ArrowBack
}

private fun ResearchStatus.label(locale: String): String = localized(name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, when (this) {
    ResearchStatus.QUEUED -> "Na fila"
    ResearchStatus.RUNNING -> "Em execução"
    ResearchStatus.PARTIAL -> "Parcial"
    ResearchStatus.NEEDS_INPUT -> "Precisa de informação"
    ResearchStatus.COMPLETED -> "Concluída"
    ResearchStatus.FAILED -> "Falhou"
}, locale)

private fun scoreText(score: Int?): String = score?.let { "$it/10" } ?: "-"

private fun localized(english: String, portuguese: String, locale: String): String = if (locale == "pt-BR") portuguese else english

private val atlasDestinations = listOf(
    AtlasDestination.DASHBOARD,
    AtlasDestination.CATALOG,
    AtlasDestination.COMPARE,
    AtlasDestination.RESEARCH_QUEUE,
    AtlasDestination.SOURCES,
    AtlasDestination.DSA,
)
